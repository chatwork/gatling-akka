package com.chatwork.gatling.akka.check

import com.chatwork.gatling.akka.response.Response
import io.gatling.commons.validation.{ Failure, Validation }
import io.gatling.core.check.{ Check, CheckResult }
import io.gatling.core.session.Session
import scala.collection.mutable
import io.gatling.commons.validation._

trait AkkaCheckSupport {
  def expectMsg(message: Any) = AkkaCheck(new Check[Response] {
    override def check(response: Response, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
      if (message == response.message) CheckResult(Some(response), None).success else Failure(s"${response.message} expected but got $message.")
    }
  })
}
