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
  private val storeList = new ArrayBuffer[Store]()

  override def start(primaryStage: Stage) {
    primaryStage.setTitle("Sup!")

    primaryStage.setScene(initWindowScene())
    primaryStage.show()

  }

  def updateFolders(leftPane : HBox, midPane : HBox, mailStore : Store): Unit = {
    val foldersListView = new ListView[String]()
    val foldersObservable = FXCollections.observableArrayList[String]
    for (name <- mailStore.getDefaultFolder.list()) {
      foldersObservable.add(name.getName)
    }
    foldersListView.setItems(foldersObservable)

    foldersListView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        updateMailsFromFolder(mailStore.getFolder(newValue), midPane)

      }
    })

    leftPane.getChildren.clear()
    leftPane.getChildren.add(foldersListView)
  }

  def updateMailsFromFolder(folder : Folder, midPane : HBox): Unit = {
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)
    val system = ActorSystem("MainUIActor")

    val mailsTitleObservable = FXCollections.observableArrayList[String]
    val mailsTitleListView = new ListView[String]()

    folder.open(Folder.READ_ONLY)
    val messages = folder.getMessages

    if (messages.nonEmpty) {
      val getMessagesActor = system.actorOf(Props(new GetMailsActor(messages, messages.size - 51, messages.size - 1)), name = "imapActor")
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
      }
    })

    mailsTitleListView.setItems(mailsTitleObservable)
    mailsTitleListView.setMinWidth(650)
    midPane.getChildren.clear()
    midPane.getChildren.add(mailsTitleListView)
  }

  def initWindowScene(): Scene = {

    val borderPane = new BorderPane()
    val topPane = new TabPane()
    val leftPane = new HBox
    val midPane = new HBox

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

    val vbox = new VBox()
    val menuBar = new MenuBar
    val menuNew = new Menu("New")

    val imapItem = new MenuItem("Imap", new ImageView(new Image("/imap.png")))
    imapItem.setOnAction(_ => {
      val loginWindow = new ImapLoginWindow()
      loginWindow.display()
      if (loginWindow.isConnected) {
        storeList.append(loginWindow.store)
        print(storeList.size)
        val tab = new Tab()
        tab.setText(s"${loginWindow.store.getURLName}")
        tab.setGraphic(new ImageView(new Image("/imap.png")))
        topPane.getTabs.add(tab)
      }
    })

    val pop3Item = new MenuItem("Pop3", new ImageView(new Image("/pop3.png")))
    val stmpItem = new MenuItem("Stmp", new ImageView(new Image("/stmp.png")))

    menuNew.getItems.addAll(imapItem, pop3Item, stmpItem)
    menuBar.getMenus.add(menuNew)
    vbox.getChildren.addAll(menuBar, borderPane)

    new Scene(vbox, 800, 600)
  }
}
