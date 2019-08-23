gatling-akka
--------------

gatling-akka is [gatling](https://github.com/gatling/gatling) plugin for stress tests of Akka application.

gatling-akka enables to simulate message passing between actors and measure latency and throughput of the requests and responses.

## Installation

For sbt users, add following lines to build.sbt.

### Scala 2.11

```scala
libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.5",
  "io.gatling" % "gatling-test-framework" % "2.2.5",
  "com.chatwork" %% "gatling-akka" % "0.1.14"
)
```

### Scala 2.12

```scala
libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.1.3",
  "io.gatling" % "gatling-test-framework" % "3.1.3",
  "com.chatwork" %% "gatling-akka" % "0.1.15"
)
```

## Example

```scala
import _root_.io.gatling.core.Predef._
import com.chatwork.gatling.akka.Predef._

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
      // ask a request to ponger actor and check its response with expectMsg and then save it in session if the check passes
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
    .exec {
      // message content check, same as expectMsg
      akkaActor("ping-3").to(ponger) ? PingerPonger.Ping check message.is(PingerPonger.Pong)
    }
    .exec {
      // find recipient and save it in session
      akkaActor("ping-4").to(ponger) ? PingerPonger.Ping check recipient.find.exists.saveAs("recipient")
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
```
