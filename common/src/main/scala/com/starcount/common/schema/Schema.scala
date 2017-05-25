package com.starcount.common.schema

case class User(
  id: Long,
  description: String)

case class UserItemIds(
  user: User,
  itemIds: List[Long])

case class Item(
  id: Long,
  description: String)

case class UserItem(
  user: User,
  item: Item)

