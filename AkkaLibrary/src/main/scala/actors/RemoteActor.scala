package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import database.operations._

class RemoteActor extends Actor with ActorLogging{
  val system = ActorSystem("MainActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = receive(sender)

  def receive(s: ActorRef): Receive = {
    case find: Find =>
      handleFind(find)
      context.become(receive(sender))
    case order: Order =>
      handleOrder(order)
      context.become(receive(sender))
    case streamText: StreamText=>
      handleStream(streamText)
      context.become(receive(sender))

    case findResult: FindResult =>
      s ! findResult
    case orderResult: OrderResult =>
      s ! orderResult
    case streamResult: StreamTextResult =>
      s ! streamResult

    case msg => log.info(s"Unknown message $msg")
  }

  def handleFind(find: Find): Any = {
      val findActor = system.actorOf(Props[FindActor], "findActor")
      findActor ! find
  }

  def handleOrder(order: Order): Any = {
    val orderActor = system.actorOf(Props[OrderActor], "orderActor")
    orderActor ! order
  }

  def handleStream(streamText: StreamText): Any = {
    val streamActor = system.actorOf(Props[TextStreamActor], "streamActor")
    streamActor ! streamText
  }



}
