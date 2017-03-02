package com.chatwork.gatling.akka

import com.chatwork.gatling.akka.config.AkkaProtocol
import com.chatwork.gatling.akka.request.{ AkkaRequestBuilder, AskRequestAttributes, AskRequestBuilder }
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

object Predef {
  def akka(implicit configuration: GatlingConfiguration) = AkkaProtocol()

  def akka(requestName: Expression[String]): AkkaRequestBuilder = new AkkaRequestBuilder(requestName)

}
