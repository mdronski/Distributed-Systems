import java.io.File

import actors.{LocalActor, RemoteActor}
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import database.operations.{Find, Order, StreamText}

import scala.io.{Source, StdIn}

object RemoteApp extends App{
  val configPath = getClass.getResource("remote_app.conf").getFile
  val config = ConfigFactory.parseFile(new File(configPath))

  val system: ActorSystem = ActorSystem("RemoteSystem", config)
  val remoteActor = system.actorOf(Props[RemoteActor], "remoteActor")

  Iterator.continually(StdIn.readLine())
    .takeWhile(_ != "x")
}
