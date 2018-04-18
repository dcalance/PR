import java.util
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import javafx.stage.Stage
import javafx.scene.control._
import javafx.scene.effect.Glow
import javafx.geometry.Pos
import javafx.scene.image.{Image, ImageView}
import javax.imageio.ImageIO
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javax.mail.Store


class UI extends Application {
  private val storeList = new util.ArrayList[Store]()

  override def start(primaryStage: Stage) {
    primaryStage.setTitle("Sup!")

    val borderPane = new BorderPane()
    val topPane = new TabPane()

    topPane.getSelectionModel.selectedIndexProperty.addListener(new ChangeListener[Number]() {
      override def changed(ov: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        // do something...
      }
    })

    val middlePane = new StackPane
    val button = new Button("Oh Boi")
    button.setOnAction((e : ActionEvent) => {
      val tab = new Tab()
      tab.setText("Dildo")
      tab.setClosable(false)
      topPane.getTabs.add(tab)
    })

    middlePane.getChildren.add(button)
    borderPane.setCenter(middlePane)
    borderPane.setTop(topPane)

    val menuBar = initMenu()
    val hbox = new VBox()
    hbox.getChildren.addAll(menuBar, borderPane)

    val scene = new Scene(hbox, 500, 500)
    primaryStage.setScene(scene)
    primaryStage.show()

  }

  def initMenu(): MenuBar = {
    val menuBar = new MenuBar
    val menuNew = new Menu("New")

    val imapItem = new MenuItem("Imap", new ImageView(new Image("/imap.png")))
    imapItem.setOnAction(_ => {
      val loginWindow = new ImapLoginWindow()
      loginWindow.display()
      if (loginWindow.isConnected) {
        storeList.add(loginWindow.store)
      }
    })

    val pop3Item = new MenuItem("Pop3", new ImageView(new Image("/pop3.png")))
    val stmpItem = new MenuItem("Stmp", new ImageView(new Image("/stmp.png")))

    menuNew.getItems.addAll(imapItem, pop3Item, stmpItem)
    menuBar.getMenus.add(menuNew)
    menuBar
  }
}
