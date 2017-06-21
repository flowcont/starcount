package com.starcount.akka

import akka.actor.ActorSystem
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.testkit.{ImplicitSender, TestKit}
import com.starcount.akka.ProcessorActor.{CloseProcessor, Process, ProcessSeq}
import com.starcount.akka.SinkActor.{CloseSink, SendSeq}
import com.starcount.common.Processor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ProcessorActorTest() extends TestKit(ActorSystem("ProcessorActor")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A ProcessorActor actor" must {

    val testRouter = {
      val routees = Vector(ActorRefRoutee(testActor))
      Router(RoundRobinRoutingLogic(), routees)
    }

    val processorActor = system.actorOf(ProcessorActor.props(ProcessorTest(), testRouter))

    "send a message with a List of items to be sunk" in {
      processorActor ! Process(1)
      expectMsg(SendSeq(List("1")))
    }

    "send two messages with a List of items to be sunk" in {
      processorActor ! ProcessSeq(List(1, 2))
      expectMsg(SendSeq(List("1")))
      expectMsg(SendSeq(List("2")))
    }

    "send a CloseSink() message to the sinks" in {
      processorActor ! CloseProcessor()
      expectMsg(CloseSink())
    }

  }
}

case class ProcessorTest() extends Processor[Int, String] {
  override def process(elem: Int): List[Option[String]] = List(Some(elem.toString))
}