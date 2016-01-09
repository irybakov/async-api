package kz.rio

// Common Messages

trait RestMessage

case class Error(message: String)

case class Validation(message: String)

// Exceptions
case object BagRequestException extends Exception("This is Bad Request, Man")
