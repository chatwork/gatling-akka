package com.chatwork.gatling.akka.response

import akka.actor.ActorRef

case class Response(message: Any, recipient: ActorRef)