package com.starcount.akka

import java.util.concurrent.TimeUnit
import akka.actor.{Actor, Props}
import akka.routing._
import com.starcount.akka.SourceActor.{CloseSource, StartProcess}
import com.starcount.common.SCProcessor
import com.starcount.common.readers.{ItemSource, UserItemIdSource}
import com.starcount.common.schema.{Item, UserItem, UserItemIds}
import com.starcount.common.writers.UITcpSink
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}
import akka.pattern.gracefulStop
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object SupervisorActor {

  def props(): Props = Props(SupervisorActor())

  sealed trait Message
  case class Start() extends Message
  case class Ok() extends Message
  case class ProcessOk() extends Message
  case class SinkClosed() extends Message

}

case class SupervisorActor() extends Actor {

  import SupervisorActor._

  val log: Logger = LoggerFactory.getLogger(getClass)
  log.info(s"Creating processorActor: ${self.toString()}")
  val config: Config = context.system.settings.config
  var processors: Int = config.getInt("config.processors")
  var sinks: Int = config.getInt("config.sinks")
  log.info(s"Number of processors: $processors Number of Sinks: $sinks")

  val sinkRouter: Router = {
    val routees = Vector.fill(sinks) {
      val r = context
        .actorOf(SinkActor
          .props[UserItem](UITcpSink(config.getString("config.hostname"), config.getInt("config.port")),
          config.getInt("config.processors")))
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  val cache: Map[Long, Item] = ItemSource(config.getString("config.itempath")).cache

  val processorRouter: Router = {
    val routees = Vector.fill(processors) {
      val r = context.actorOf(ProcessorActor.props[UserItemIds, UserItem](SCProcessor(cache), sinkRouter))
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def postStop(): Unit = {
    super.postStop()
    context.system.terminate()
  }

  override def receive: Receive = {
    case Start() =>

      val source = context.actorOf(SourceActor.props(processorRouter))

      source ! StartProcess(UserItemIdSource(config.getString("config.useritemidpath")))

    case Ok() => // Close all resources
      log.info(s"Closing all resources")
      sender() ! CloseSource()
    case SinkClosed() =>
      sinks -= 1
      log.info(s"Sinks: $sinks")
      if(sinks == 0) {
        Thread.sleep(2000L) //Wait for termination of actors
        gracefulStop(self, FiniteDuration(2, TimeUnit.SECONDS))
          .map { b =>
            log.info(s"System shutdown")
          }
      }
  }
}