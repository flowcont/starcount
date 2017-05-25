# Starcount Datasytems Interview Technical Test

## Overview

The task is to create an application that extracts data from the local file system, transforms it and
loads it into a remote system via TCP.

Schemas for the input and output data cant be found in the common subproject, under
`com.starcount.common.schema`, alongside reader and writer traits, under
`com.starcount.common.{ readers, writers }`.

Read `./data/items.avro` (Item) and `./data/user_item_ids.avro` (UserItemIds) from the local file
system, join each UserItemIds with Item on UserItemIds.itemIds == Item.id  to give a collection
of UserItem, and write to the local TCP socket.

For example:

```scala

// Input
val items = List(
	Item(0, "Item 0"),
	Item(1, "Item 1"))

val userItemIds = List(
	UserItemIds(User(0, "User 0") List(0, 1, 2))
	UserItemIds(User(1, "User 1"), List(1)))


// Output
val userItems = List(
	UserItem(User(0, "User 0"), Item(0, "Item 0"))
	UserItem(User(0, "User 0"), Item(1, "Item 1"))
	UserItem(User(1, "User 1"), Item(1, "Item 1")))

```

## Getting Started

### Dependencies

* sbt (see `./project/build.properties`)
* Java (we use OpenJDK 8)
* pip
* Docker (we use 17.04)
* Docker Compose (see below)

To install Docker Compose, run: `pip install -r requirements.txt`

### The Remote System

To start the remote system, run `docker-compose up`

A TCP port from the remote system will be forwarded to your loopback address on port number 9999,
for convenience.

The server on the remote system accepts upto 10 concurrent connections, each independently rate
limited to 10MB/s.

### Project Structure

We have provided a skeleton project to be built upon.

```
./build.sbt | Multi project build configuration
./common/src/main/scala/com/starcount/common | Common traits for reading and writing to and from datasources
./data | Sample Avro files to be read from the local filesystem
./lander | Docker context for the remote system
./docker-compose.yml | Docker Compose configuration for running the remote system
./lander-volume | Remote system data directory mount point
```

Please extend this project with your implementation.

## Deliverable

The deliverable should be implemented in Scala, using any libraries you see fit. Supporting scripts
should be implemented for POSIX shell or bash.

The deliverable should not be implemented using the Apache Spark engine.

We should be able to build and run the application in three commands or less.

### Mid-level Applicants

After a working application, these are the types of things we'll be looking for:

* Meaningful logging
* Graceful error handling and shutdown
* Integration and/or unit tests
* Clean code

### Senior Applicants

The deliverable should fulfill the requirements of the mid-level applicant
deliverable and additionally implement one or both of the following functionalities:

* Error recovery
* Concurrent loading to the remote system, to acheive maximum throughput.

