package com.chatwork.gatling.akka.config

import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{ Protocol, ProtocolComponents, ProtocolKey }
import io.gatling.core.session.Session

import scala.concurrent.duration._

case class AkkaProtocol(askTimeout: FiniteDuration = 10 seconds) extends Protocol {
  def askTimeout(d: FiniteDuration) = copy(askTimeout = d)
}

object AkkaProtocol {

  val protocolKey = new ProtocolKey[AkkaProtocol, AkkaProtocolComponents] {
    override def protocolClass: Class[io.gatling.core.protocol.Protocol] =
      classOf[AkkaProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): AkkaProtocol = AkkaProtocol()

    override def newComponents(coreComponents: CoreComponents): AkkaProtocol => AkkaProtocolComponents =
      protocol => AkkaProtocolComponents(protocol)
  }
}

case class AkkaProtocolComponents(protocol: AkkaProtocol) extends ProtocolComponents {
  override def onStart: Session => Session = ProtocolComponents.NoopOnStart

  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit
}
