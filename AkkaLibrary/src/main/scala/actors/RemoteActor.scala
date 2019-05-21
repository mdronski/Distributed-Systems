package actors

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import database.operations._
import scala.concurrent.duration._

class RemoteActor extends Actor with ActorLogging{
  val system = ActorSystem("MainActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = receive(sender)

  def receive(s: ActorRef): Receive = {
    case find: Find =>
      handleFind(find, sender)
      context.become(receive(sender))
    case order: Order =>
      handleOrder(order, sender)
      context.become(receive(sender))
    case streamText: StreamText=>
      handleStream(streamText, sender)
      context.become(receive(sender))

    case msg => log.info(s"Unknown message $msg")
  }

  def handleFind(find: Find, sender: ActorRef): Any = {
      val findActor = system.actorOf(Props[FindActor], "findActor")
      findActor.tell(find, sender)
  }

  def handleOrder(order: Order, sender: ActorRef): Any = {
    val orderActor = system.actorOf(Props[OrderActor], "orderActor")
    orderActor.tell(order, sender)
  }

  def handleStream(streamText: StreamText, sender: ActorRef): Any = {
    val streamActor = system.actorOf(Props[TextStreamActor], "streamActor")
    streamActor.tell(streamText, sender)
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10 seconds) {
      case _ => Stop
    }

}
