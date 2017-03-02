package com.chatwork.gatling.akka.request

import akka.actor.{ Actor, ActorRef }
import com.chatwork.gatling.akka.action.AskAction
import com.chatwork.gatling.akka.check.AkkaCheck
import com.chatwork.gatling.akka.config.{ AkkaProtocol, AkkaProtocolComponents }
import io.gatling.commons.validation.Success
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.structure.ScenarioContext

class AkkaRequestBuilder(requestName: Expression[String]) {

  def to(recipient: Expression[ActorRef]): AskRequestBuilder = {

    AskRequestBuilder(AskRequestAttributes(requestName, recipient))
  }

}

case class AskRequestBuilder(askAttributes: AskRequestAttributes) {
  def to(recipient: Expression[ActorRef]): AskRequestBuilder = copy(askAttributes = askAttributes.copy(recipient = recipient))

  def withSender(sender: Expression[ActorRef]): AskRequestBuilder = copy(askAttributes = askAttributes.copy(sender = sender))

  def ask(message: Expression[Any]): AskRequestBuilder = copy(askAttributes = askAttributes.copy(message = Some(message)))

  def ?(message: Expression[Any]): AskRequestBuilder = ask(message)

  def check(checks: AkkaCheck*): AskRequestBuilder = copy(askAttributes = askAttributes.copy(checks = askAttributes.checks ::: checks.toList))
}

object AskRequestBuilder {
  implicit def toActionBuilder(requestBuilder: AskRequestBuilder): AskActionBuilder = AskActionBuilder(requestBuilder)
}

sealed trait AkkaRequestAttributes {
  val sender: Expression[ActorRef]
  val recipient: Expression[ActorRef]
}

case class AskRequestAttributes(
  requestName: Expression[String],
  recipient: Expression[ActorRef],
  sender: Expression[ActorRef] = AskRequestAttributes.noSender,
  message: Option[Expression[Any]] = None,
  checks: List[AkkaCheck] = Nil
) extends AkkaRequestAttributes

object AskRequestAttributes {
  private val noSender: Expression[ActorRef] = (_: Session) => Success(Actor.noSender)
}

case class AskActionBuilder(requestBuilder: AskRequestBuilder) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val akkaComponents: AkkaProtocolComponents = protocolComponentsRegistry.components(AkkaProtocol.protocolKey)
    AskAction(requestBuilder.askAttributes, coreComponents, akkaComponents.protocol, system, next)
  }
}
