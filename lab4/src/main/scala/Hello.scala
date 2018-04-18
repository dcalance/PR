import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

import javax.mail._
import javax.mail.internet._
import javax.mail.search._
import java.util.Properties

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.scene.control.Label

object Hello {
  def main(args: Array[String]) {
    val system = ActorSystem("HelloSystem")

    Application.launch(classOf[UI], args: _*)

  }
}

class HelloActor extends Actor {
  def receive = {
    case "hello" =>
      printf("\033[H\033[2J")
      println("hello back at you")
    case _ => println("huh?")
  }
}