package com.starcount.common.readers

import java.io.File
import com.sksamuel.avro4s.AvroInputStream
import com.starcount.common.schema.{
  Item => Schema
}

trait Item extends Avro[Schema] {

  protected val inputPath: String

  lazy protected val inputStream = AvroInputStream.data[Schema](new File(inputPath))

}

