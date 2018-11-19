package com.dounine.scala.flink

import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.slf4j.LoggerFactory

import scala.collection.Map

object FlinkLoginFail {

  val LOGGER = LoggerFactory.getLogger(classOf[App])

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val loginEventStream = env.fromCollection(List(
      LoginEvent("1", "192.168.0.1", "fail"),
      LoginEvent("1", "192.168.0.2", "fail"),
      LoginEvent("1", "192.168.0.3", "fail"),
      LoginEvent("2", "192.168.10,10", "success")
    ))

    val loginFailPattern = Pattern.begin[LoginEvent]("begin")
      .where(_.`type`.equals("fail"))
      .times(2)
      .within(Time.seconds(1))

    val patternStream = CEP.pattern(loginEventStream, loginFailPattern)

    val loginFailDataStream = patternStream
      .select((pattern: Map[String, Iterable[LoginEvent]]) => {
        val first = pattern.getOrElse("begin", null).iterator.next()

        LoginWarning(first.userId, first.ip, first.`type`)
      })

    loginFailDataStream.print

    env.execute

    LOGGER.info("finish")
  }

}

case class LoginEvent(
                       userId: String,
                       ip: String,
                       `type`: String
                     )

case class LoginWarning(
                         userId: String,
                         ip: String,
                         `type`: String
                       )

