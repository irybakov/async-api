package kz.rio

trait DomainMessage

case class Ping(ping: String) extends DomainMessage
case class Pong(pong: String) extends DomainMessage
case class Echo(echo: String) extends DomainMessage

case class Error(message: String)

case class Validation(message: String)

// Exceptions
case object BagRequestException extends Exception("This is Bad Request, Man")
