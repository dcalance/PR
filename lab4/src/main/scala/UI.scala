import java.util
import java.util.concurrent.TimeUnit
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import javafx.stage.Stage
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javax.mail.{Folder, Store}
import javafx.collections.FXCollections
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.util.Timeout

import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import akka.pattern.ask

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global


class UI extends Application {
  private val storeList = new ArrayBuffer[StoreStruct]()

  override def start(primaryStage: Stage) {
    primaryStage.setTitle("Sup!")

    primaryStage.setScene(initWindowScene())
    primaryStage.show()

  }

  def updateFolders(leftPane : HBox, midPane : HBox, mailStruct : StoreStruct): Unit = {
    val foldersListView = new ListView[String]()
    val foldersObservable = FXCollections.observableArrayList[String]
    for (name <- mailStruct.store.getDefaultFolder.list()) {
      foldersObservable.add(name.getName)
    }
    foldersListView.setItems(foldersObservable)

    foldersListView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        mailStruct.currentFolder = mailStruct.store.getFolder(newValue)
        val folder = mailStruct.store.getFolder(newValue)
        if (!folder.isOpen) {
          folder.open(Folder.READ_ONLY)
        }
        mailStruct.mailsNumber = folder.getMessageCount
        mailStruct.currentPage = 1
        updateMailsFromFolder(mailStruct.store.getFolder(newValue), midPane, mailStruct.currentPage)

      }
    })

    leftPane.getChildren.clear()
    leftPane.getChildren.add(foldersListView)

    if(mailStruct.currentFolder == null) {
      foldersListView.getSelectionModel.select(0)
    } else {
      updateMailsFromFolder(mailStruct.currentFolder, midPane, mailStruct.currentPage)
    }
  }

  def updateMailsFromFolder(folder : Folder, midPane : HBox, page : Int): Unit = {
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)
    val system = ActorSystem("MainUIActor")

    val mailsTitleObservable = FXCollections.observableArrayList[String]
    val mailsTitleListView = new ListView[String]()
    mailsTitleListView.setStyle("-fx-font-family:consolas")

    if(!folder.isOpen)
      folder.open(Folder.READ_ONLY)
    val messages = folder.getMessages

    if (messages.nonEmpty) {
      val getMessagesActor = system.actorOf(Props(new GetMailsActor(messages, if (messages.size - (50 * page) > 0) messages.size - (50 * page) else 0, messages.size - 1 - (50 * (page - 1)))), name = "imapActor")
      val future = getMessagesActor ? "start"

      future.onComplete({
        case Success(resultArray) => {
          mailsTitleObservable.addAll(resultArray.asInstanceOf[util.ArrayList[String]])
          getMessagesActor ! PoisonPill
        }
      })
    }

    mailsTitleListView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        println(newValue)
        val messageWindow = new DisplayMessage()
        val selectedIndex = mailsTitleListView.getSelectionModel.getSelectedIndex
        val mailNr = messages.size - 1 - (50 * page) + (50 - selectedIndex)
        println(s"Mail number: $mailNr")
        messageWindow.createWindow(messages(mailNr))
      }
    })

    mailsTitleListView.setItems(mailsTitleObservable)
    mailsTitleListView.setMinWidth(600)
    midPane.getChildren.clear()
    midPane.getChildren.add(mailsTitleListView)
  }

  def initWindowScene(): Scene = {

    val borderPane = new BorderPane()
    val topPane = new TabPane()
    val leftPane = new HBox
    val midPane = new HBox
    val rightPane = new HBox

    val buttonNext = new Button(">")
    val buttonPrev = new Button("<")

    buttonNext.setOnAction(_ => {
      val selectedTab = topPane.getSelectionModel.getSelectedIndex
      if (storeList(selectedTab).currentPage * 50 < storeList(selectedTab).mailsNumber) {
        storeList(selectedTab).currentPage += 1
        updateMailsFromFolder(storeList(selectedTab).currentFolder, midPane, storeList(selectedTab).currentPage)
      }
    })
    buttonPrev.setOnAction(_ => {
      val selectedTab = topPane.getSelectionModel.getSelectedIndex
      if (storeList(selectedTab).currentPage > 1) {
        storeList(selectedTab).currentPage -= 1
        updateMailsFromFolder(storeList(selectedTab).currentFolder, midPane, storeList(selectedTab).currentPage)
      }
    })

    leftPane.setMaxWidth(150)
    borderPane.getCenter

    topPane.getSelectionModel.selectedIndexProperty.addListener(new ChangeListener[Number]() {
      override def changed(ov: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        updateFolders(leftPane, midPane, storeList(newValue.intValue()))
      }
    })

    topPane.setTabMaxHeight(200)
    topPane.setTabMaxWidth(150)
    borderPane.setCenter(midPane)
    borderPane.setTop(topPane)
    borderPane.setLeft(leftPane)
    borderPane.setRight(rightPane)


    val vbox = new VBox()
    val menuBar = new MenuBar
    val menuNew = new Menu("New")

    val imapItem = new MenuItem("Imap", new ImageView(new Image("/imap.png")))
    imapItem.setOnAction(_ => {
      val loginWindow = new ImapLoginWindow()
      loginWindow.display()
      if (loginWindow.isConnected) {
        storeList.append(new StoreStruct(loginWindow.store, 1, null, 0))
        val tab = new Tab()
        tab.setText(s"${loginWindow.store.getURLName}")
        tab.setGraphic(new ImageView(new Image("/imap.png")))
        topPane.getTabs.add(tab)
        rightPane.getChildren.addAll(buttonPrev, buttonNext)
      }
    })

    val pop3Item = new MenuItem("Pop3", new ImageView(new Image("/pop3.png")))
    val stmpItem = new MenuItem("Stmp", new ImageView(new Image("/stmp.png")))
    stmpItem.setOnAction(_ => {
      val sendMessageWindow = new SendMessageWindow
      sendMessageWindow.createWindow()
    })

    menuNew.getItems.addAll(imapItem, pop3Item, stmpItem)
    menuBar.getMenus.add(menuNew)
    vbox.getChildren.addAll(menuBar, borderPane)

    new Scene(vbox, 800, 600)
  }
}
