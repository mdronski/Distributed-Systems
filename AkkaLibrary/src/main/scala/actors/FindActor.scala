package actors

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import database.Book
import database.operations.{Find, FindResult, Order}
import scala.concurrent.duration._

class FindActor extends Actor with ActorLogging{
  val db1 = "/database/db3"
  val db2 = "/database/db2"
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

    case book: Option[Book]
      if book.isDefined && responses == NO_RESPONSES || book.isDefined && responses == ONE_RESPONSE =>
      s ! FindResult(book)
        context.stop(self)

    case book: Option[Book] if book.isDefined && responses == ALREADY_SEND =>
      context.become(receive(s, ALREADY_SEND))

    case book: Option[Book] if book.isEmpty && responses == NO_RESPONSES =>
      context.become(receive(s, ONE_RESPONSE))

    case book: Option[Book] if book.isEmpty && responses == ONE_RESPONSE =>
      s ! FindResult(book)
      context.stop(self)

    case msg => log.info(s"Unknown message $msg")
  }

  private def createFindActor(db: String, name: String): ActorRef = {
    context.actorOf(Props(classOf[SearchDBActor], db), name)
  }

  override def postStop(): Unit = {
    context.children.foreach(context.stop)
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10 seconds) {
      case _ => Stop
    }

}


