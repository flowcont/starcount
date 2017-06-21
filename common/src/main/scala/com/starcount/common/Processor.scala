package com.starcount.common

import com.starcount.common.schema.{Item, UserItem, UserItemIds}
import org.slf4j.{Logger, LoggerFactory}
import scala.util.{Failure, Success, Try}


trait Processor[T, S] {
  def process(elem: T): List[Option[S]]
}

case class SCProcessor(cache: Map[Long, Item]) extends Processor[UserItemIds, UserItem] {

  val log: Logger = LoggerFactory.getLogger(getClass)
  val debug: Boolean = log.isDebugEnabled

  override def process(elem: UserItemIds): List[Option[UserItem]] = {
    elem.itemIds.map{ itemId =>
      log.debug(s"Getting Item for $itemId")
      Try(cache(itemId)) match {
        case Success(item) =>
          log.debug(s"Item retrieved")
          Some(UserItem(elem.user, item))
        case Failure(e) =>
          log.warn(e.getMessage)
          None
      }
    }
  }

}