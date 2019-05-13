package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import database.Book
import database.operations.{Find, Order}

class FindActor extends Actor with ActorLogging{
  val db1 = "/database/db1/titles.txt"
  val db2 = "/database/db2/titles.txt"
  val system = ActorSystem("order")
  val db1Actor: ActorRef = createFindActor(db1, "db1Actor")
  val db2Actor: ActorRef = createFindActor(db2, "db2Actor")
  val NO_RESPONSES = 0
  val ONE_RESPONSE = 1
  val ALREADY_SEND = 2

  override def receive: Receive = receive(sender, NO_RESPONSES)

  def receive(s: ActorRef, responses: Int): Receive = {
    case find: Find =>
      db1Actor ! find
      db2Actor ! find
      context.become(receive(sender, NO_RESPONSES))

    case book: Option[Book] if book.isDefined && responses == NO_RESPONSES =>
      s ! book
      println("Found")
      context.become(receive(s, ALREADY_SEND))

    case book: Option[Book] if book.isDefined && responses == ALREADY_SEND =>
      context.become(receive(s, ALREADY_SEND))

    case book: Option[Book] if book.isEmpty && responses == NO_RESPONSES =>
      context.become(receive(s, ONE_RESPONSE))

    case book: Option[Book] if book.isEmpty && responses == ONE_RESPONSE =>
      println("Not Found")
      s ! book
      context.become(receive(s, ONE_RESPONSE))

    case msg => log.info(s"Unknown message $msg")
  }

  private def createFindActor(db: String, name: String): ActorRef = {
    system.actorOf(Props(classOf[SearchDBActor], db), name)
  }
}


