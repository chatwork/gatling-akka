package com.chatwork.gatling.akka.request

import akka.actor.{ Actor, ActorRef }
import com.chatwork.gatling.akka.action.AskAction
import com.chatwork.gatling.akka.config.{ AkkaProtocol, AkkaProtocolComponents }
import io.gatling.commons.validation.Success
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.structure.ScenarioContext

case class AkkaRequestBuilder(requestName: Expression[String]) {
  def to(recipient: Expression[ActorRef]) = AkkaActionBuilder(requestName, recipient)
}

sealed trait AkkaRequestAttributes {
  val sender: Expression[ActorRef]
  val recipient: Expression[ActorRef]
}

case class AskRequestAttributes[+M](
  requestName: Expression[String],
  sender: Expression[ActorRef],
  recipient: Expression[ActorRef],
  message: Expression[M],
  expected: Expression[Any => Boolean]
) extends AkkaRequestAttributes

case class AkkaActionBuilder(requestName: Expression[String], recipient: Expression[ActorRef]) {
  private val noSender: Expression[ActorRef] = (_: Session) => Success(Actor.noSender)
  private def defaultExpect = (s: Session) => Success((_: Any) => true)
  def ?[M](message: Expression[M]) = AskActionBuilder(AskRequestAttributes(
    requestName,
    noSender,
    recipient,
    message,
    defaultExpect
  ))
}

case class AskActionBuilder[+M](attr: AskRequestAttributes[M]) extends ActionBuilder {
  def sender(sender: Expression[ActorRef]) = copy(attr = attr.copy(sender = sender))

  def expect(expect: Expression[Any => Boolean]) = copy(attr = attr.copy(expected = expect))

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val akkaComponents: AkkaProtocolComponents = protocolComponentsRegistry.components(AkkaProtocol.protocolKey)
    AskAction(attr, coreComponents, akkaComponents.protocol, system, next)
  }
}
