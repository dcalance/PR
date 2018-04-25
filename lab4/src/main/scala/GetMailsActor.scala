import java.util
import javafx.collections.FXCollections
import javax.mail.{Folder, Message}

import akka.actor.Actor

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}



class GetMailsActor(messages : Array[Message], startIndex : Int, endIndex : Int) extends Actor{
  override def receive = {
    case "start" => {
      val result = new util.ArrayList[String]
        for (index <- endIndex to startIndex by -1) {
          val fromString = {
            val getFrom = messages(index).getFrom()(0).toString
            if (getFrom.length > 40) {
              getFrom.substring(0, 36) + "..."
            } else {
              getFrom
            }
          }
          val spaces = " " * (60 - fromString.length)
          result.add(s"$fromString$spaces${messages(index).getSubject}")
        }
      sender() ! result
    }
  }
}
