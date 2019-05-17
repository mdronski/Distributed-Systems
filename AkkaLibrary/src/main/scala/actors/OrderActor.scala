package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import database.{Book, OrderManager}
import database.operations._

class OrderActor extends Actor with ActorLogging{
  val system = ActorSystem("TextStream")
  private val findActor = system.actorOf(Props[FindActor], "ordersFindActor")
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = receive(sender)

  def receive(s: ActorRef): Receive = {
    case order: Order =>
      findActor ! new Find(order.title)
      context.become(receive(sender))

    case findResult: FindResult =>
      findResult.book match {
        case Some(book) =>
          makeOrder(book)
          s ! new OrderResult(true)
        case None =>
          s ! new OrderResult(false)
      }
      context.stop(self)

    case msg => log.info(s"Unknown message $msg")
  }

  private def makeOrder(book: Book): Unit ={
    OrderManager.makeOrder(book)
  }


}
