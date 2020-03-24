package com.chatwork.gatling.akka.check

import java.util.{ Map => JMap }

import com.chatwork.gatling.akka.response.Response
import io.gatling.commons.validation.Validation
import io.gatling.core.session.Session
import io.gatling.core.check.{ Check, CheckResult }

case class AkkaCheck(wrapped: Check[Response]) extends Check[Response] {
  override def check(response: Response,
                     session: Session)(implicit preparedCache: JMap[Any, Any]): Validation[CheckResult] =
    wrapped.check(response, session)
}
