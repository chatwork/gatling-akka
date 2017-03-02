package com.chatwork.gatling.akka.check

import com.chatwork.gatling.akka.response.Response
import io.gatling.commons.validation.Validation
import io.gatling.core.session.Session
import io.gatling.core.check.{ Check, CheckResult }
import scala.collection.mutable

case class AkkaCheck(wrapped: Check[Response]) extends Check[Response] {
  override def check(response: Response, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] =
    wrapped.check(response, session)
}
