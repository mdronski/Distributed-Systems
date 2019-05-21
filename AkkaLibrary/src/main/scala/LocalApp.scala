import java.io.File

import actors.{LocalActor, RemoteActor}
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import database.operations.{Find, Order, StreamText}

import scala.io.StdIn

object LocalApp extends App {
  val configPath = getClass.getResource("remote_app2.conf").getFile
  val config = ConfigFactory.parseFile(new File(configPath))

  val system: ActorSystem = ActorSystem("LocalSystem", config)
  val localActor = system.actorOf(Props[LocalActor])
  val search = "f (.+)".r
  val order = "o (.+)".r
  val textStream = "s (.+)".r


  println("Operations:")
  println("f <title>     //finds title in db and print its price")
  println("o <title>     //place an order for a given title")
  println("s <title>     //streams content of a given title, line by line in 1 second intervals")
  println("x             // shutdown application")

  Iterator.continually(StdIn.readLine())
    .takeWhile(_ != "x")
    .foreach {
      case search(title) => localActor ! new Find(title)
      case order(title) => localActor ! new Order(title)
      case textStream(title) => localActor ! new StreamText(title)
      case _ => println("Incorrect command")
    }
}
