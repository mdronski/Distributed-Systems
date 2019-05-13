package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import database.Book
import database.operations.{Find, Order}

class OrderActor extends Actor with ActorLogging{
  override def receive: Receive = receive(null)

  def receive(s: ActorRef): Receive = {
    case order: Order =>
      val request = new Find(order.title)
      context.become(receive(sender))
    case book: Option[Book] => s ! book
    case msg => log.info(s"Unknown message $msg")
  }


}
