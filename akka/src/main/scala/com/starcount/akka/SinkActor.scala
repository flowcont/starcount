package com.starcount.akka

import akka.actor.{Actor, Props}
import com.starcount.akka.ProcessorActor.{Ko, Ok}
import com.starcount.akka.SupervisorActor.SinkClosed
import com.starcount.common.writers.Sink
import org.slf4j.{Logger, LoggerFactory}
import scala.util.{Failure, Success}

object SinkActor{
  def props[T](sink: Sink[T], processors: Int): Props = Props(SinkActor[T](sink, processors))

  sealed trait Message
  final case class Send[S](s: S) extends Message
  final case class SendSeq[S](seq: Seq[S]) extends Message
  final case class CloseSink() extends Message
}

case class SinkActor[T](sink: Sink[T], var processors: Int) extends Actor {

  import SinkActor._

  val log: Logger = LoggerFactory.getLogger(getClass)
  log.info(s"Creating SinkActor: ${self.toString()}")
  val debug: Boolean = log.isDebugEnabled
  //var processors: Int = context.system.settings.config.getInt("config.processors")

  override def receive: Receive = {
    case Send(s: T) => sink.sink(s) match {
      case Success(_) =>
        if(debug) log.debug(s"${self.toString()} Sink ${s.toString}")
        sender() ! Ok
      case Failure(t) =>
        log.warn(s"${self.toString()} Sink FAILED for ${s.toString}")
        sender() ! Ko(List(s), List(t))
    }
    case SendSeq(seq: Seq[T]) =>
      val sending = seq.map(sink.sink)
      if (sending.forall(_.isSuccess)) {
        if(debug) {
          seq.foreach(s => log.debug(s"${self.toString()} Sink ${s.toString} worked"))
        }
        sender() ! Ok
      }
      else {
        log.warn(s"$self Sink Head ${seq.headOption.toString} failed!")
        sender() ! Ko(seq, sending.map {
          case Failure(t) => t
        })
      }
    case CloseSink() =>
      processors -= 1
      if(processors == 0) {
        sink.close() match {
          case Success(_) =>
            log.info(s"$self Successfully closed output stream")
          case Failure(t) =>
            log.warn(s"$self Error while closing output stream: ${t.getMessage}")
        }
        sender() ! SinkClosed()
        context.stop(self)
      }
  }

}
