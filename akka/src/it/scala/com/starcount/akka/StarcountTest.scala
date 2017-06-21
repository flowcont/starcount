package com.starcount.akka

import java.io.File

import akka.actor.ActorSystem
import com.starcount.akka.SupervisorActor.Start
import com.starcount.akka.test.{TestItemWriter, TestServer, TestUserItemIdWriter}
import com.starcount.common.schema.UserItem
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.concurrent.Future
import com.starcount.common.schema.{Item, User, UserItemIds}
import org.apache.commons.io.FileUtils

class StarcountTest() extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  var server: TestServer = _
  val data: Set[UserItem] = Set(UserItem(User(0, "User 0"), Item(0, "Item 0")), UserItem(User(0, "User 0"), Item(1, "Item 1")),
    UserItem(User(0, "User 0"), Item(2, "Item 2")), UserItem(User(1, "User 1"), Item(0, "Item 0")))
  var setUp: Future[Unit] = _

  override def beforeAll(): Unit = {

    val itemWriter = TestItemWriter("item.avro")
    val uiIdWriter = TestUserItemIdWriter("useritem.avro")
    server = TestServer(9999)
    setUp = server.run()
    println("Server running")
    itemWriter.outputStream.write(List(Item(0, "Item 0"), Item(1, "Item 1"), Item(2, "Item 2")))
    uiIdWriter.outputStream.write(List(
      UserItemIds(User(0, "User 0"), List(0, 1, 2)),
      UserItemIds(User(1, "User 1"), List(0))))
    itemWriter.outputStream.close()
    uiIdWriter.outputStream.close()

  }

  "Starcount" should "write all the joined elements to a Socket" in {
    val system = ActorSystem("StarCountApp", ConfigFactory.load().getConfig("IntegrationTest"))

    val processors = system.settings.config.getInt("config.processors")

    val sinks = system.settings.config.getInt("config.sinks")

    println(s"Processors: $processors Sinks: $sinks")

    val supervisorActor = system.actorOf(SupervisorActor.props(),"Supervisor")

    supervisorActor ! Start()

    setUp.map{ _ =>
      server.socket.close()
      val col: Stream[UserItem] = server.ui.stream
      assert(col.forall{ ui =>
        data.contains(ui)
      })
      }
    }

  override def afterAll(): Unit = {
    FileUtils.forceDelete(new File("item.avro"))
    FileUtils.forceDelete(new File("useritem.avro"))
  }

}
