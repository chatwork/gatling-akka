package com.chatwork.gatling.akka

import akka.actor.{ Actor, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import com.chatwork.gatling.akka.Predef._

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("BasicSimulation", config)

  // gatling-akka protocol configuration
  val akkaConfig = akkaActor.askTimeout(3 seconds)

  // recipient actorRef
  val ponger = system.actorOf(PingerPonger.ponger)

  // scenario definition
  val s = scenario("ping-pong-ping-pong")
    .exec {
      // ask a request to ponger actor and check its response with expectMsg and then save it in session
      akkaActor("ping-1").to(ponger) ? PingerPonger.Ping check expectMsg(PingerPonger.Pong).saveAs("pong")
    }
    .exec {
      val pf: PartialFunction[Any, Unit] = {
        case PingerPonger.Pong =>
      }

      akkaActor("ping-2").to(ponger).ask { session =>
        // ask based on session value
        session("pong").as[PingerPonger.Pong.type] match {
          case PingerPonger.Pong => PingerPonger.Ping
        }
      }.check(expectMsgPF(pf)) // check a response with partial function with expectMsgPF
    }

  // inject configurations
  setUp(
    s.inject(constantUsersPerSec(10) during (10 seconds))
  ).protocols(akkaConfig).maxDuration(10 seconds)
}

// simple ping-pong actor definition
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