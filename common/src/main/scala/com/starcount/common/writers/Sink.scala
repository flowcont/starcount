package com.starcount.common.writers

import com.starcount.common._

import scala.util.Try

/**
  * Trait to be used when sinking data
  * @tparam T Type of data to sink
  */

trait Sink[T] {
  def sink(s: T): Try[Unit]
  def close(): Try[Unit]
}


/**
  * Case class to sink userItem to a Tcp Socket
  */

case class UITcpSink(hn: String, port: Int) extends Sink[schema.UserItem] with writers.UserItem {

  override protected val hostName: String = hn
  override protected val portNumber: Int = port

  override def sink(s: schema.UserItem): Try[Unit] = Try(outputStream.write(s))

  override def close(): Try[Unit] = Try(outputStream.close())

}