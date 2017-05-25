package com.starcount.common.readers

import scala.util.Try
import com.sksamuel.avro4s.AvroInputStream

trait Avro[T] {

  protected val inputStream: AvroInputStream[T]

  lazy private val inputIterator: Iterator[T] = inputStream.iterator

  def read(): Try[T] = Try { inputIterator.next }

  def hasNext: Boolean = inputIterator.hasNext

  def close(): Try[Unit] = Try { inputStream.close() }

}
