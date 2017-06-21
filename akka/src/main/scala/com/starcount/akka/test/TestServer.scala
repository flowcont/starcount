package com.starcount.akka.test

import java.io.{File, InputStream}
import java.net.{ServerSocket, Socket}

import com.sksamuel.avro4s.{AvroDataInputStream, AvroInputStream, AvroOutputStream}
import com.starcount.common.readers.{Avro => readAvro}
import com.starcount.common.schema.{Item, UserItem, UserItemIds}
import com.starcount.common.writers.{Avro => writeAvro}
import org.apache.commons.io.IOUtils
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

case class TestServer(port: Int) {

  val server: ServerSocket = new ServerSocket(port)
  var socket: Socket = _
  var ui: TestUserItemReader = _

  def run(): Future[Unit] = Future {
    socket = server.accept()
    ui = TestUserItemReader(socket.getInputStream)
  }

  def readUserItem(): Try[UserItem] = ui.read()

}

case class TestUserItemReader(in: InputStream) extends readAvro[UserItem] {

  protected val inputPath: String = ""

  lazy protected val inputStream: AvroDataInputStream[UserItem] = AvroInputStream.data[UserItem](IOUtils.toByteArray(in))

  val stream: Stream[UserItem] = inputStream.iterator().toStream
}

case class TestItemWriter(file: String) extends writeAvro[Item] {
  override val outputStream: AvroOutputStream[Item] = AvroOutputStream.data(new File(file))
}

case class TestUserItemIdWriter(file: String) extends writeAvro[UserItemIds] {
  override val outputStream: AvroOutputStream[UserItemIds] = AvroOutputStream.data(new File(file))
}