package com.starcount.common.writers

import scala.util.Try
import com.sksamuel.avro4s.{ AvroOutputStream }

trait Avro[T] {

  protected val outputStream: AvroOutputStream[T]

  def write(value: T): Try[Unit] = Try { outputStream.write(value) }

  def flush(): Try[Unit] = Try { outputStream.flush() }

  def close(): Try[Unit] = Try { outputStream.close() }

}
