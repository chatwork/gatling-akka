package com.chatwork.gatling.akka.check

import akka.actor.ActorRef
import com.chatwork.gatling.akka.response.Response
import io.gatling.commons.validation.Success
import io.gatling.core.CoreDsl
import io.gatling.core.check.CheckResult
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Session
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{ Matchers, WordSpecLike }

import scala.collection.mutable

class AkkaCheckSpec extends WordSpecLike with Matchers with MockitoSugar with CoreDsl with AkkaCheckSupport {

  implicit val configuration = GatlingConfiguration.loadForTest()

  implicit def cache: mutable.Map[Any, Any] = mutable.Map.empty
  val session                               = Session("mockSession", 0)

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
