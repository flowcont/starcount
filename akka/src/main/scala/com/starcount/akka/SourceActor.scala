package com.starcount.akka

import akka.actor.{Actor, Props}
import akka.routing.{Broadcast, Router}
import com.starcount.akka.ProcessorActor.{CloseProcessor, Process}
import com.starcount.common.readers.Source
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}

object SourceActor {
  def props(processors: Router): Props = Props(SourceActor(processors))

  sealed trait Message
  final case class StartProcess[T](s: Source[T]) extends Message
  final case class ProcessStream[T](s: Stream[T]) extends Message
  final case class CloseSource() extends Message

  sealed trait Reply
  final case class Ok() extends Reply
  final case class Ko(t: Seq[Throwable]) extends Reply

}

case class SourceActor[T](processors: Router) extends Actor {

  import SourceActor._

  val log: Logger = LoggerFactory.getLogger(getClass)
  log.info(s"Creating SourceActor: ${self.toString()}")
  var UIid: Source[T] = _

  override def receive: Receive = {
    case StartProcess(uid: Source[T]) =>
      UIid = uid
      log.info(s"$self: Starting processing of ${uid.path}")
      self ! ProcessStream(uid.stream)
    case ProcessStream(s) =>
      s.headOption match {
        case Some(uid) =>
          processors.route(Process(uid), self)
          self ! ProcessStream(s.tail)
        case None =>
          UIid.close() match {
          case Success(_) =>
            log.info(s"$self: Terminated processing ${UIid.path}.")
            context.parent ! SupervisorActor.Ok()
          case Failure(e) => log.warn(s"$self: Error while closing InputStream for ${UIid.path}: ${e.getMessage}" )
        }
      }
    case CloseSource() =>
      UIid.close()
      Thread.sleep(3000L) //Wait for delivery of messages as ordered delivery is not guaranteed
      processors.route(Broadcast(CloseProcessor()), sender())
  }

}