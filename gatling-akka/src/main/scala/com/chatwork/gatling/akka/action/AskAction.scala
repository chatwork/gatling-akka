package com.chatwork.gatling.akka.action

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import com.chatwork.gatling.akka.config.AkkaProtocol
import com.chatwork.gatling.akka.request.AskRequestAttributes
import com.chatwork.gatling.akka.response.Response
import io.gatling.commons.stats.{ KO, OK, Status }
import io.gatling.commons.util.ClockSingleton._
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{ Action, ExitableAction }
import io.gatling.core.check.Check
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import scala.util.{ Failure, Success }

case class AskAction(
    attr:           AskRequestAttributes,
    coreComponents: CoreComponents,
    protocol:       AkkaProtocol,
    system:         ActorSystem,
    next:           Action
) extends ExitableAction with NameGen {

  override def name: String = genName("akkaAsk")

  override def execute(session: Session): Unit = recover(session) {
    configureAttr(session).map {
      case (requestName, sender, recipient, message) =>

        def writeData(session: Session, status: Status, _startTimeStamp: Long, logMessage: Option[String]) = {
          val requestEndTime = nowMillis
          statsEngine.logResponse(
            session,
            requestName,
            ResponseTimings(startTimestamp = _startTimeStamp, endTimestamp = requestEndTime),
            status,
            None,
            logMessage
          )
          next ! session
        }

        val requestTimestamp = nowMillis
        import system.dispatcher
        recipient.ask(message)(protocol.askTimeout, sender).onComplete {
          case Success(msg) =>
            val (checkSaveUpdate, checkError) = Check.check(Response(msg, recipient), session, attr.checks)
            val status = checkError match {
              case None => OK
              case _    => KO
            }
            writeData(checkSaveUpdate(session), status, requestTimestamp, checkError.map(_.message))
          case Failure(th) =>
            writeData(session, KO, requestTimestamp, Some(th.getMessage))
        }
    }
  }

  override def statsEngine: StatsEngine = coreComponents.statsEngine

  private def configureAttr(session: Session): Validation[(String, ActorRef, ActorRef, Any)] = {
    val messageExpr: Expression[Any] = attr.message match {
      case Some(expr) => expr
      case None       => (s: Session) => io.gatling.commons.validation.Failure("Message to send to an actor is required.")
    }
    for {
      requestName <- attr.requestName(session)
      sender <- attr.sender(session)
      recipient <- attr.recipient(session)
      message <- messageExpr(session)
    } yield (requestName, sender, recipient, message)
  }
}
