package com.chatwork.gatling.akka.action

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import com.chatwork.gatling.akka.config.AkkaProtocol
import com.chatwork.gatling.akka.request.AskRequestAttributes
import io.gatling.commons.stats.{ KO, OK }
import io.gatling.commons.util.ClockSingleton._
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{ Action, ExitableAction }
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen

import scala.util.{ Failure, Success }

case class AskAction[+M](
    attr: AskRequestAttributes[M],
    coreComponents: CoreComponents,
    protocol: AkkaProtocol,
    system: ActorSystem,
    next: Action
) extends ExitableAction with NameGen {

  override def name: String = genName("akkaAsk")

  override def execute(session: Session): Unit = recover(session) {
    configureAttr(session).map {
      case (requestName, sender, recipient, message, expected) =>

        def writeData(isSuccess: Boolean, _startTimeStamp: Long, logMessage: Option[String]) = {
          val requestEndTime = nowMillis
          statsEngine.logResponse(
            session,
            requestName,
            ResponseTimings(startTimestamp = _startTimeStamp, endTimestamp = requestEndTime),
            if (isSuccess) OK else KO,
            None,
            logMessage
          )
          next ! session
        }

        val requestTimestamp = nowMillis
        import system.dispatcher
        recipient.ask(message)(protocol.askTimeout, sender).onComplete {
          case Success(msg) => msg match {
            case msg if expected(msg) =>
              writeData(isSuccess = true, requestTimestamp, None)
            case msg =>
              writeData(isSuccess = false, requestTimestamp, Some(s"received message($msg) is not equal to expected($expected)"))
          }
          case Failure(th) =>
            writeData(isSuccess = false, requestTimestamp, Some(th.getMessage))
        }
    }
  }

  override def statsEngine: StatsEngine = coreComponents.statsEngine

  private def configureAttr(session: Session): Validation[(String, ActorRef, ActorRef, M, Any => Boolean)] = {
    for {
      requestName <- attr.requestName(session)
      sender <- attr.sender(session)
      recipient <- attr.recipient(session)
      message <- attr.message(session)
      expected <- attr.expected(session)
    } yield (requestName, sender, recipient, message, expected)
  }
}
