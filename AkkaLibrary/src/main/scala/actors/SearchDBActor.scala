package actors

import akka.actor.{Actor, ActorLogging}
import database.Book
import database.operations.Find
import utils.DatabasePathParser

import scala.io.Source

class SearchDBActor(val db: String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case find: Find =>
      sender ! search(find.title)
      context.stop(self)
    case msg => log.info(s"Unknown message $msg")
      context.stop(self)
  }

  private def search(title: String): Option[Book] = {
    val resourcePath = getClass.getResource(DatabasePathParser.getTitlesBasePath(db))
    Source.fromFile(resourcePath.getFile)
      .getLines()
      .find(_.split(':')(0) == title)
      .flatMap(line => {
        val params = line.split(':')
        Some(new Book(params(0), params(1).toDouble, db))
    })
  }

  override def postStop(): Unit = {
    val response: Option[Book] = None
    context.parent ! response
  }
}