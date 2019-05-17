package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import database.Book
import database.operations._
import utils.DatabasePathParser

import scala.io.Source

class TextStreamActor extends Actor with ActorLogging {
  val system = ActorSystem("TextStream")
  private val findActor = system.actorOf(Props[FindActor], "textStreamfindActor")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {receive(sender)}


  def receive(s: ActorRef): Receive = {
    case textStream: StreamText =>
      findActor ! new Find(textStream.title)
      context.become(receive(sender))

    case findResult: FindResult =>
      findResult.book match {
        case Some(book) =>
          val source = getStream(book)
          source.toStream.foreach(line => {
            s ! new StreamTextResult(Option(line))
            Thread.sleep(1000)
          })
          context.stop(self)
        case None =>
          s ! new StreamTextResult(None)
          context.stop(self)
      }

    case msg => log.info(s"Unknown message $msg")
      context.stop(self)
  }

  private def getStream(book: Book) = {
    val filePath = getClass.getResource(DatabasePathParser.getBookPath(book.db, book.title)).getFile
    Source.fromFile(filePath).getLines()

  }
}
