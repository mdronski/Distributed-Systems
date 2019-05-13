package actors

import akka.actor.{Actor, ActorLogging}
import database.Book
import database.operations.Find

import scala.io.Source

class SearchDBActor(val db: String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case find: Find => {
      sender ! search(find.title)
    }
    case msg => log.info(s"Unknown message $msg")
  }

  private def search(title: String): Option[Book] = {
    val resorcePath = getClass.getResource(db)
    Source.fromURL(resorcePath).getLines() collectFirst {
      case line if line.split(':')(0) == title =>
        val params = line.split(':')
        new Book(params(0), params(1).toDouble, db)
    }
  }


}