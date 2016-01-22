package kz.rio.core

import kz.rio.{Pong, BagRequestException, Echo, Ping}

/**
 * Created by irybakov on 1/22/16.
 */
object LocalPingEchoActor

class LocalPingEchoActor extends AsyncServiceActor {

  def receive = {
    case p @ Ping(ping) => {
      log.debug("LocalPingEchoActor got Ping: {}",p)
      context.parent ! Pong("CONTAINER PONG: " + ping)
    }
    case e @ Echo(echo) => {
      log.debug("LocalPingEchoActor got Echo: {}",e)
      context.parent ! Echo("CONTAINER: " + echo)
    }

    case _ =>  throw BagRequestException

  }

}
