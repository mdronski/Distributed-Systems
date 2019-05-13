package actors

import java.nio.file.{Path, Paths}

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, IOResult, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import database.Book
import database.operations.{Find, StreamText}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source
import java.io._

class TextStreamActor extends Actor with ActorLogging{
  val system = ActorSystem("TextStream")
  private val findActor = system.actorOf(Props[FindActor], "findActor")
  implicit val materializer = ActorMaterializer()


  override def receive: Receive = receive(sender)


  def receive(sender: ActorRef): Receive = {
    case textStream: StreamText =>
      findActor ! new Find(textStream.title)
      context.become(receive(sender))

    case book: Option[Book] if book.isEmpty =>
      sender ! None

    case book: Option[Book] if book.isDefined =>
      getStream(book.get)
      sender ! getStream(book.get)

    case msg => log.info(s"Unknown message $msg")
  }

  private def getStream(book: Book): Future[Done] = {

//
//    val f2 = new File(Source.fromURL(getClass.getResource(book.db)).toString())
//    val file = Paths.get(book.db)
//    f2.r
//    val splitter = Framing.delimiter(
//      ByteString("\n"),
//      maximumFrameLength = 1024,
//      allowTruncation = true
//    )
//
//    FileIO.fromFile(f2)
//      .via(splitter)
//      .map(_.utf8String)
//      .throttle(1, 1.second, 1, ThrottleMode.shaping)
//      .runForeach(x => println(x))

  }
}
