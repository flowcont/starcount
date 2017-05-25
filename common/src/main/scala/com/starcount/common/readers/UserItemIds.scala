package com.starcount.common.readers

import scala.util.{ Try, Failure }
import java.io.File
import com.sksamuel.avro4s.AvroInputStream
import com.starcount.common.schema.{
  UserItemIds => Schema
}

trait UserItemIds extends Avro[Schema] {

  protected val inputPath: String

  lazy protected val inputStream = AvroInputStream.data[Schema](new File(inputPath))

}

