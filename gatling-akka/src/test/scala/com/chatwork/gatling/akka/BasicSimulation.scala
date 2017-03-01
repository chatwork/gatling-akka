package com.chatwork.gatling.akka

import akka.actor.{ Actor, ActorSystem, Props }
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  import Predef._

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("BasicSimulation", config)

  val akkaConfig = akka.askTimeout(3 seconds)
  val ponger = system.actorOf(PingerPonger.ponger)
  val expectation: Any => Boolean = {
    case PingerPonger.Pong => true
    case _ => false
  }
  val s = scenario("ping-pong-ping-pong").exec(akka("ping1", ponger).ask(PingerPonger.Ping, expectation)).exec(akka("ping2", ponger).ask(PingerPonger.Ping, expectation))
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