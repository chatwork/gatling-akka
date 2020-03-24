package com.chatwork.gatling.akka.check

import akka.actor.ActorRef
import com.chatwork.gatling.akka.response.Response
import io.gatling.core.check.{ DefaultFindCheckBuilder, _ }
import io.gatling.commons.validation._
import io.gatling.core.check.extractor._
import io.gatling.core.session.{ Expression, Session }

import scala.annotation.implicitNotFound

private[akka] trait AkkaCheckSupport {
  private object AkkaCheckMaterializer extends CheckMaterializer[AkkaCheck, AkkaCheck, Response, Response] {
    override val specializer: Specializer[AkkaCheck, Response] = (wrapped: Check[Response]) => AkkaCheck(wrapped)
    override val preparer: Preparer[Response, Response]        = (r: Response) => r.success
  }

  implicit val akkaCheckMaterializer: CheckMaterializer[AkkaCheck, AkkaCheck, Response, Response] =
    AkkaCheckMaterializer

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for Akka.")
  implicit def checkBuilder2AkkaCheck[A, P, X](
      checkBuilder: CheckBuilder[A, P, X]
  )(implicit materializer: CheckMaterializer[A, AkkaCheck, Response, P]): AkkaCheck =
    checkBuilder.build(materializer)

  private def messageExtractor(session: Session): Validation[Extractor[Response, Any]] =
    new Extractor[Response, Any] with SingleArity {
      val name                      = "message"
      def apply(prepared: Response) = Some(prepared.message).success
    }.success

  private def recipientExtractor(session: Session): Validation[Extractor[Response, ActorRef]] =
    new Extractor[Response, ActorRef] with SingleArity {
      val name                      = "recipient"
      def apply(prepared: Response) = Some(prepared.recipient).success
    }.success

  private val validatorCheckBuilder: ValidatorCheckBuilder[AkkaCheck, Response, Any] = ValidatorCheckBuilder(
    messageExtractor,
    displayActualValue = true
  )

  val message = new DefaultFindCheckBuilder[AkkaCheck, Response, Any](
    messageExtractor,
    displayActualValue = true
  )

  val recipient = new DefaultFindCheckBuilder[AkkaCheck, Response, ActorRef](
    recipientExtractor,
    displayActualValue = true
  )

  def expectMsg(message: Expression[Any]) = validatorCheckBuilder.validate(message.map(m => new ExpectMsgValidator(m)))

  def expectMsgPF(pf: Expression[PartialFunction[Any, Unit]]) = {
    validatorCheckBuilder.validate(pf.map(f => new ExpectMsgPFValidator(f)))
  }
}

private[check] class ExpectMsgValidator(expected: Any) extends Validator[Any] {
  override def name: String = s"expectMsg($expected)"

  override def apply(actual: Option[Any], displayActualValue: Boolean): Validation[Option[Any]] = {
    if (actual.contains(expected)) actual.success
    else
      Failure(
        if (displayActualValue) s"$expected expected but got ${actual.getOrElse("")}."
        else s"$expected expected."
      )
  }
}

private[check] class ExpectMsgPFValidator(pf: PartialFunction[Any, Unit]) extends Validator[Any] {
  override def name: String = s"expectMsgPf($pf)"

  override def apply(actual: Option[Any], displayActualValue: Boolean): Validation[Option[Any]] = actual match {
    case Some(message) =>
      if (pf.isDefinedAt(message)) {
        pf(message)
        actual.success
      } else
        Failure(
          if (displayActualValue) s"Unexpected message: $message."
          else "Unexpected message"
        )
    case None => Validator.FoundNothingFailure
  }
}
