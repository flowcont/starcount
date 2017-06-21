package com.starcount.common.readers

import com.starcount.common.schema
import scala.util.Try

trait Source[+T] {
  val path: String
  val stream: Stream[T]
  def close(): Try[Unit]
}

case class ItemSource(path: String) extends Item with Source[schema.Item]{
  override protected val inputPath: String = path
  override val stream: Stream[schema.Item] = inputStream.iterator().toStream
  val cache: Map[Long, schema.Item] = stream.map{ it => it.id -> it}.toMap
}

case class UserItemIdSource(path: String) extends UserItemIds with Source[schema.UserItemIds]{
  override protected val inputPath: String = path
  override val stream: Stream[schema.UserItemIds] = inputStream.iterator().toStream
}