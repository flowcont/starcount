package com.starcount.akka

import akka.actor.ActorSystem
import com.starcount.akka.SupervisorActor.Start
import com.typesafe.config.{ConfigFactory, ConfigValue}
import org.slf4j.LoggerFactory
import scala.collection.immutable.Map

object StarCountApp extends App {

  val system = ActorSystem("StarCountApp", ConfigFactory.load().getConfig("StarCountApp"))

  val log = LoggerFactory.getLogger(getClass)

  val config = system.settings.config.entrySet().toArray

  if(log.isDebugEnabled) {
    val properties: Map[String, String] = config.foldLeft(Map.empty[String, String]){(acc, elem) =>
      val pair = elem.asInstanceOf[java.util.Map.Entry[String, ConfigValue]]
      acc + (pair.getKey -> pair.getValue.unwrapped().toString)
    }

    properties.foreach(pair => log.info(s"${pair._1} -> ${pair._2}"))

  }

  val supervisorActor = system.actorOf(SupervisorActor.props(),"Supervisor")

  supervisorActor ! Start()

}