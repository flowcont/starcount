package com.starcount.akka

import akka.actor.{Actor, Props}
import akka.routing.{Broadcast, Router}
import com.starcount.akka.SinkActor.CloseSink
import com.starcount.common.Processor
import org.slf4j.{Logger, LoggerFactory}

object ProcessorActor{
  def props[T, S](processor: Processor[T, S], sinker: Router): Props = Props(ProcessorActor(processor, sinker))

  sealed trait Message
  final case class Process[T](s: T) extends Message
  final case class ProcessSeq[T](seq: Seq[T]) extends Message
  final case class CloseProcessor() extends Message

  sealed trait Reply
  final case class Ok() extends Reply
  final case class Ko[T](seq: Seq[T], t: Seq[Throwable]) extends Reply

}

case class ProcessorActor[T, S](processor: Processor[T, S], sinker: Router) extends Actor {

  import ProcessorActor._
  import com.starcount.akka.SinkActor.SendSeq

  val log: Logger = LoggerFactory.getLogger(getClass)
  log.info(s"Creating processorActor: ${self.toString()}")
  val debug: Boolean = log.isDebugEnabled

  override def receive: Receive = {
    case Process(t: T) =>
      if (debug) log.debug(s"$self Received Process(t): Processing elements of $t")
      sinker.route(SendSeq(processor.process(t).flatten), self)
    case ProcessSeq(seq: Seq[T]) =>
      if(debug) log.debug(s"$self Received ProcessSeq(t): Processing elements of $seq")
      val elements: Seq[List[Option[S]]] = seq.map(processor.process)
      elements.foreach(sq => sinker.route(SendSeq(sq.flatten), self))
    case Ok() => if(debug) log.debug(s"$self Element successfully sent to sink")
    case Ko(seq: Seq[T], errors: Seq[Throwable]) =>
      errors.foreach{e => log.warn(s"$self Error while sinking: ${e.getMessage}")}
    case CloseProcessor() =>
      sinker.route(Broadcast(CloseSink()), sender())
      context.stop(self)
  }

}