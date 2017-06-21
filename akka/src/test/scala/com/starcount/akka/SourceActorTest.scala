package com.starcount.akka

import akka.actor.ActorSystem
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.testkit.{ImplicitSender, TestKit}
import com.starcount.akka.ProcessorActor.Process
import com.starcount.akka.SourceActor.StartProcess
import com.starcount.common.readers.Source
import com.starcount.common.schema.{User, UserItemIds}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.util.{Success, Try}

class SourceActorTest() extends TestKit(ActorSystem("SourceActor")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A SourceActor actor" must {

    val testRouter = {
      val routees = Vector(ActorRefRoutee(testActor))
      Router(RoundRobinRoutingLogic(), routees)
    }

    val uid = UserItemIds(User(0, "User 0"), List(0, 1, 2))

    "send a message with UserItemIds to be processed" in {
      val sourceActor = system.actorOf(SourceActor.props(testRouter))
      sourceActor ! StartProcess(UserItemIdTest(Success(), uid))
      expectMsg(Process(uid))
    }

  }
}

case class UserItemIdTest(t: Try[Unit], uid: UserItemIds) extends Source[UserItemIds] {
  override val path: String = "test path"

  override val stream: Stream[UserItemIds] = Stream(uid)

  override def close(): Try[Unit] = t
}