import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.control.{Button, Label, PasswordField, TextField}
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.stage.Modality
import javax.mail.Store


class ImapLoginWindow{
  var store : Store = _
  var isConnected = false

  def display(): Unit = {
    val stage = new Stage()
    stage.setResizable(false)

    val vbox = new VBox(10)
    vbox.setAlignment(Pos.CENTER)

    val lockedImage = new ImageView(new Image("/locked-icon.png"))
    lockedImage.setFitHeight(200)
    lockedImage.setFitWidth(200)

    val errorText = new Label()
    errorText.setTextFill(Color.RED)

    val serverField = new TextField()
//    serverField.setFocusTraversable(false)
    serverField.setPromptText("Server")
    serverField.setMaxWidth(200)

    val emailField = new TextField()
//    emailField.setFocusTraversable(false)
    emailField.setPromptText("Email")
    emailField.setMaxWidth(200)

    val passwordField = new PasswordField()
//    passwordField.setFocusTraversable(false)
    passwordField.setPromptText("Password")
    passwordField.setMaxWidth(200)

    val hbox = new HBox(10)
    hbox.setAlignment(Pos.CENTER)

    val connectButton = new Button("Connect")
    connectButton.setOnAction(_ => {
      val imap = new Imap()
      val (resp, respStore) = imap.connect(serverField.getText, emailField.getText, passwordField.getText)
      errorText.setText(resp)
      if(resp == "success") {
        store = respStore
        isConnected = true
        stage.close()
      }
    })
    val cancelButton = new Button("Cancel")
    cancelButton.setOnAction(_ =>{
      stage.close()
    })

    hbox.getChildren.addAll(connectButton, cancelButton)
    vbox.getChildren.addAll(lockedImage, errorText, serverField, emailField, passwordField, hbox)

    val scene: Scene = new Scene(vbox, 350, 400)

    stage.setTitle("Connect to Server IMAP")
    stage.setScene(scene)
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.showAndWait()
  }
}
