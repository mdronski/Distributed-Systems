import java.util.concurrent.TimeUnit

import actors.{FindActor, TextStreamActor}
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import database.operations.{Find, StreamText}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object Main extends App {
  val system: ActorSystem = ActorSystem("HelloSystem")

  val helloActor = system.actorOf(Props[TextStreamActor], "findActor")
  helloActor ! new StreamText("commonTitle")
  Await.ready(system.whenTerminated, Duration(1, TimeUnit.MINUTES))
  system.stop(helloActor)

}
