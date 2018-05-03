package server

import java.text.SimpleDateFormat
import java.util
import java.util.Calendar

import akka.actor.ActorRef
import akka.io.TcpMessage
import akka.util.ByteString

import scala.collection.mutable.ArrayBuffer
import scala.math.min
import scala.math.max
import scala.util.Random

class ServerCommands() {
  private val comandsList = Array("/help", "/hello", "/random", "/time")

  def parse(input : String): String = {
    if (!input.startsWith("/")) {
      commandNotFound()
    } else {
      input match {
        case "/help" => help()
        case "/random" => random()
        case "/time" => time()
        case s if s.startsWith("/hello") => hello(s)
        case _ =>
          for (command <- comandsList) {
            if (stringDistance(command, input) < 2) {
              return s"Did you mean $command ?"
            }
          }
          commandNotFound()
      }
    }

  }

  def hello(s : String) : String = s.substring(6)

  def random() : String = {
    val rn = new Random()
    val i = rn.nextInt % 100000
    i.toString
  }

  def time() : String = {
    val now = Calendar.getInstance().getTime
    val timeFormat = new SimpleDateFormat("hh:mm a")
    timeFormat.format(now)
  }

  def stringDistance(s1: String, s2: String): Int = {
    def minimum(i1: Int, i2: Int, i3: Int) = min(min(i1, i2), i3)

    val dist = Array.ofDim[Int](s1.length + 1, s2.length + 1)

    for (idx <- 0 to s1.length) dist(idx)(0) = idx
    for (jdx <- 0 to s2.length) dist(0)(jdx) = jdx

    for (idx <- 1 to s1.length; jdx <- 1 to s2.length)
      dist(idx)(jdx) = minimum (
        dist(idx-1)(jdx  ) + 1,
        dist(idx  )(jdx-1) + 1,
        dist(idx-1)(jdx-1) + (if (s1(idx-1) == s2(jdx-1)) 0 else 1)
      )

    dist(s1.length)(s2.length)
  }

  def help() : String = {
    "/help - List all commands\n" +
      "/hello Text - Response with the send parameter\n" +
      "/random - Generates a random number\n" +
      "/time - Current time\n"+
      ""
  }

  def commandNotFound(): String = {
    "Command not found, type /help to get all available commands."
  }
}
