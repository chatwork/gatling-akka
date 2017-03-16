package com.chatwork.gatling.akka.check

import com.chatwork.gatling.akka.response.Response
import io.gatling.core.check._
import io.gatling.commons.validation._
import io.gatling.core.check.extractor._
import io.gatling.core.session.{ Expression, Session }

private[akka] trait AkkaCheckSupport {

  private val responseExtender: Extender[AkkaCheck, Response] = (wrapped: Check[Response]) => AkkaCheck(wrapped)

  private val passThroughResponsePreparer: Preparer[Response, Response] = (r: Response) => r.success

  private def messageExtractor(session: Session): Validation[Extractor[Response, Any]] = new Extractor[Response, Any] with SingleArity {
    val name = "message"
    def apply(prepared: Response) = Some(prepared.message).success
  }.success

  private val validatorCheckBuilder: ValidatorCheckBuilder[AkkaCheck, Response, Response, Any] = ValidatorCheckBuilder(
    responseExtender,
    passThroughResponsePreparer,
    messageExtractor
  )

  def expectMsg(message: Expression[Any]) = validatorCheckBuilder.validate(message.map(m => new ExpectMsgValidator(m)))

  def expectMsgPF(pf: Expression[PartialFunction[Any, Unit]]) = {
    validatorCheckBuilder.validate(pf.map(f => new ExpectMsgPFValidator(f)))
  }
}

private[check] class ExpectMsgValidator(expected: Any) extends Validator[Any] {
  override def name: String = s"expectMsg($expected)"

  override def apply(actual: Option[Any]): Validation[Option[Any]] = {
    if (actual.contains(expected)) actual.success else Failure(s"$expected expected but got ${actual.getOrElse("")}.")
  }
}

private[check] class ExpectMsgPFValidator(pf: PartialFunction[Any, Unit]) extends Validator[Any] {
  override def name: String = s"expectMsgPf($pf)"

  override def apply(actual: Option[Any]): Validation[Option[Any]] = actual match {
    case Some(message) =>
      if (pf.isDefinedAt(message)) {
        pf(message)
        actual.success
      } else Failure(s"Unexpected message: $message.")
    case None => Validator.FoundNothingFailure
  }
}