package com.chatwork.gatling.akka

import akka.actor.{ Actor, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.core.check.CheckResult

import scala.concurrent.duration._
import io.gatling.commons.validation._

class BasicSimulation extends Simulation {

  import Predef._

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("BasicSimulation", config)

  val akkaConfig = akka.askTimeout(3 seconds)
  val ponger = system.actorOf(PingerPonger.ponger)
  val receivePong: Any => Boolean = {
    case PingerPonger.Pong => true
    case _                 => false
  }

  val s = scenario("ping-pong-ping-pong")
    .exec(akka("ping-1").to(ponger) ? PingerPonger.Ping check expectMsg(PingerPonger.Pong))
    .exec(akka("ping-2").to(ponger) ? PingerPonger.Ping check expectMsgPF {
      case PingerPonger.Pong => CheckResult(Some(PingerPonger.Pong), None).success
    })

  setUp(
    s.inject(constantUsersPerSec(10) during (10 seconds))
  ).protocols(akkaConfig)
}

object PingerPonger {

  def ponger: Props = Props(new Ponger)

  class Ponger extends Actor {
    override def receive: Receive = {
      case Ping => sender() ! Pong
    }
  }

  case object Ping

  case object Pong

}