package com.chatwork.gatling.akka

import akka.actor.{ Actor, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  import Predef._

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("BasicSimulation", config)

  val akkaConfig = akkaActor.askTimeout(3 seconds)
  val ponger = system.actorOf(PingerPonger.ponger)
  val receivePong: Any => Boolean = {
    case PingerPonger.Pong => true
    case _                 => false
  }

  val pf: PartialFunction[Any, Unit] = {
    case PingerPonger.Pong =>
  }

  val s = scenario("ping-pong-ping-pong")
    .exec(akkaActor("ping-1").to(ponger) ? PingerPonger.Ping check expectMsg(PingerPonger.Pong))
    .exec((akkaActor("ping-2").to(ponger) ? PingerPonger.Ping).check(expectMsgPF(pf).saveAs("message")))
    .exec { session =>
      session("message").as[PingerPonger.Pong.type]
      session
    }

  setUp(
    s.inject(constantUsersPerSec(10) during (10 seconds))
  ).protocols(akkaConfig).maxDuration(10 seconds)
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