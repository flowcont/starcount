package com.starcount.common

import com.starcount.common.schema.{Item, User, UserItem, UserItemIds}
import org.scalatest.{FlatSpec, Matchers}

case class ProcessorTest() extends FlatSpec with Matchers {

  behavior of "SCProcessor"

  val cache: Map[Long, Item] = Map(0L -> Item(0, "Item 0"), 1L -> Item(1, "Item 1"))
  val processor: SCProcessor = SCProcessor(cache)

  "Processor" should "join together Users and existing items" in {
    val userItemIds = List(
      UserItemIds(User(0, "User 0"), List(0, 1, 2)),
      UserItemIds(User(1, "User 1"), List(1)))

    val userItems = userItemIds.flatMap(processor.process).flatten

    userItems shouldBe List(UserItem(User(0, "User 0"), Item(0, "Item 0")),
      UserItem(User(0, "User 0"), Item(1, "Item 1")),
      UserItem(User(1, "User 1"), Item(1, "Item 1")))
  }

  it should "not join items that do not exist" in {
    val userItemIds = List(
      UserItemIds(User(0, "User 0"), List(3, 4, 5)),
      UserItemIds(User(1, "User 1"), List(3)))

    val userItems = userItemIds.flatMap(processor.process).flatten

    userItems shouldBe List()
  }

}
