package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection}
import database.operations._

class LocalActor extends Actor with ActorLogging{
  val remoteActorPath: String = "akka.tcp://RemoteSystem@127.0.0.1:2137/user/remoteActor"
  val remoteActor: ActorSelection = context.actorSelection(remoteActorPath)


  override def receive: Receive = {
    case find: Find =>
      remoteActor ! find
    case order: Order =>
      remoteActor ! order
    case streamText: StreamText=>
      remoteActor ! streamText

    case findResult: FindResult =>
      handleFindResult(findResult)
    case orderResult: OrderResult =>
      handleOrderResult(orderResult)
    case streamResult: StreamTextResult =>
      handleStreamResult(streamResult)

    case msg => log.info(s"Unknown message $msg")
  }

  def handleFindResult(findResult: FindResult): Unit =
    findResult.book match {
      case Some(book) => println(s"${book.title} costs ${book.price}")
      case None => println("There is no book with given title in database")
    }


  def handleOrderResult(orderResult: OrderResult): Unit =
    orderResult.result match {
      case true => println("Order successfully placed")
      case false => println("Error during placing an order")
    }


  def handleStreamResult(streamResult: StreamTextResult): Unit =
    streamResult.line match {
      case Some(line) => println(line)
      case None => println("Error during streaming")
    }
}

