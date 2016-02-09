package kz.rio

trait DomainMessage

case class Ping(ping: String) extends DomainMessage
case class Pong(pong: String) extends DomainMessage
case class Echo(echo: String) extends DomainMessage

case class Error(message: String)

case class Validation(message: String)

// Exceptions
case object BagRequestException extends Exception("This is Bad Request, Man")


// Rabbit routing
case class EcoService(system: String,subSystem: String, microService: String) extends DomainMessage {

  def serviceEndpoint = s"$microService-$subSystem-$system"
}

case class Endpoint(instanceId: String, ecoService: EcoService) extends DomainMessage {

  val serviceEndpoint = ecoService.serviceEndpoint

  val instanceEndpoint = s"$instanceId-$serviceEndpoint"

  def queue = s"Q:$instanceEndpoint"

  def exchange = s"X:$instanceEndpoint"
}
