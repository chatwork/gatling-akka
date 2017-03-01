package com.chatwork.gatling.akka

import akka.actor.ActorRef
import com.chatwork.gatling.akka.config.AkkaProtocol
import com.chatwork.gatling.akka.request.AkkaRequestBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

object Predef {
  def akka(implicit configuration: GatlingConfiguration) = AkkaProtocol()

  def akka(requestName: Expression[String], sender: Expression[ActorRef], recipient: Expression[ActorRef]) = AkkaRequestBuilder(requestName, sender, recipient)
}
