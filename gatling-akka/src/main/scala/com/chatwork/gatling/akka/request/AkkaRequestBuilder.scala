package com.chatwork.gatling.akka.request

import akka.actor.ActorRef
import com.chatwork.gatling.akka.action.AskAction
import com.chatwork.gatling.akka.config.{ AkkaProtocol, AkkaProtocolComponents }
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

case class AkkaRequestBuilder(
  requestName: Expression[String],
    sender: Expression[ActorRef],
    recipient: Expression[ActorRef]
) {
  def ask[M, N](message: Expression[M], expected: Expression[Any => Boolean]) = AskActionBuilder(AskRequestAttributes(
    requestName,
    sender,
    recipient,
    message,
    expected
  ))
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

trait AkkaActionBuilder extends ActionBuilder

case class AskActionBuilder[+M](attr: AskRequestAttributes[M]) extends AkkaActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val akkaComponents: AkkaProtocolComponents = protocolComponentsRegistry.components(AkkaProtocol.protocolKey)
    AskAction(attr, coreComponents, akkaComponents.protocol, system, next)
  }
}
