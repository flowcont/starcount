package com.starcount.akka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.starcount.akka.ProcessorActor.{Ko, Ok}
import com.starcount.akka.SinkActor.{CloseSink, Send, SendSeq}
import com.starcount.akka.SupervisorActor.SinkClosed
import com.starcount.common.writers.Sink
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.util.{Failure, Success, Try}

class SinkActorTest() extends TestKit(ActorSystem("SinkActor")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A SinkActor actor" must {

    "send an Ok for sinking one element or a List of elements and close the sink" in {
      val sinkActor = system.actorOf(SinkActor.props(SinkTest(Success()), 1))
      sinkActor ! Send(1)
      expectMsg(Ok)
      sinkActor ! SendSeq(List(1, 2))
      expectMsg(Ok)
      sinkActor ! CloseSink()
      expectMsg(SinkClosed())
    }

    "send a Ko for sinking one element or a List of elements and close the sink" in {
      val t = new Throwable
      val sinkActor = system.actorOf(SinkActor.props(SinkTest(Failure(t)), 1))
      sinkActor ! Send(1)
      expectMsg(Ko(List(1), List(t)))
      sinkActor ! SendSeq(List(1, 2))
      expectMsg(Ko(List(1,2), List(t, t)))
      sinkActor ! CloseSink()
      expectMsg(SinkClosed())
    }

  }
}

case class SinkTest(t: Try[Unit]) extends Sink[Int] {
  override def sink(s: Int): Try[Unit] = t

  override def close(): Try[Unit] = t
}