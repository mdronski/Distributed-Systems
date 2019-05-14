import actors.MainActor
import akka.actor.{ActorSystem, Props}
import database.operations.{Find, Order, StreamText}

import scala.io.StdIn

object Main extends App {
  val system: ActorSystem = ActorSystem("HelloSystem")
  val mainActor = system.actorOf(Props[MainActor], "mainActor")
  val search = "s (.+)".r
  val order = "o (.+)".r
  val textStream = "t (.+)".r


  println("Operations:")
  println("s <title>     //searches for given title in db and print its price")
  println("o <title>     //place an order for a given title")
  println("t <title>     //print content of a given title, line by line in 1 second intervals")
  println("x             // shutdown application")

  Iterator.continually(StdIn.readLine())
    .takeWhile(_ != "x")
    .foreach {
      case search(title) => mainActor ! new Find(title)
      case order(title) => mainActor ! new Order(title)
      case textStream(title) => mainActor ! new StreamText(title)
      case _ => println("Incorrect command")
    }
}
