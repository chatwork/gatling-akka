package com.chatwork.gatling.akka.check

import java.util.{ HashMap => JHashMap, Map => JMap }

import akka.actor.ActorRef
import com.chatwork.gatling.akka.response.Response
import io.gatling.commons.util.DefaultClock
import io.gatling.commons.validation.Success
import io.gatling.core.CoreDsl
import io.gatling.core.check.CheckResult
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Session
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{ Matchers, WordSpecLike }

class AkkaCheckSpec extends WordSpecLike with Matchers with MockitoSugar with CoreDsl with AkkaCheckSupport {

  implicit val configuration = GatlingConfiguration.loadForTest()

  implicit def preparedCache: JMap[Any, Any] = new JHashMap
  val session                                = Session("mockSession", 0, new DefaultClock().nowMillis)

  private def mockResponse(message: Any): Response = {
    Response(message, ActorRef.noSender)
  }

  "AkkaCheck" must {
    "expectMsg" in {
      val check: AkkaCheck = expectMsg("mock")
      check.check(mockResponse("mock"), session) shouldBe Success(CheckResult(Some("mock"), None))
    }

    "expectMsgPF" in {
      def pf: PartialFunction[Any, Unit] = {
        case "mock" =>
      }
      val check: AkkaCheck = expectMsgPF(pf)
      check.check(mockResponse("mock"), session) shouldBe Success(CheckResult(Some("mock"), None))
    }
  }

}
