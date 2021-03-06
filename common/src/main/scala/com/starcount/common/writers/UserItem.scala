package com.starcount.common.writers

import java.net.Socket

import com.sksamuel.avro4s.{AvroDataOutputStream, AvroOutputStream}
import com.starcount.common.schema.{UserItem => Schema}

trait UserItem extends Avro[Schema] {

  protected val hostName: String

  protected val portNumber: Int

  private def socket = new Socket(hostName, portNumber)

  lazy protected val outputStream: AvroDataOutputStream[Schema] = AvroOutputStream.data[Schema](socket.getOutputStream())

}

