package demo

import doobie.imports._
import doobie.util.invariant.UnexpectedEnd
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import remotely._
import remotely.codecs._
import remotely.Remote.implicits._
import remotely.transport.netty.NettyTransport
import scala.reflect.runtime.universe.TypeTag
import scalaz.\/
import scalaz.concurrent.Task
import scodec.Codec

// N.B. you can't factor out the Protocol here because everything needs to be available to the
// compiler; it's just passing the AST. So in order to do this it needs to be defined in another
// project that doesn't depend on this one. See https://github.com/oncue/remotely/issues/13

@GenServer(remotely.Protocol
    .empty
    .codec[String]
    .codec[scalaz.\/[String, Int]]
    .specify1[String, scalaz.\/[String, Int]]("pop")) 
abstract class World2Server

@GenClient(remotely.Protocol
  .empty
  .codec[String]
  .codec[scalaz.\/[String, Int]]
  .specify1[String, scalaz.\/[String, Int]]("pop")
  .signatures)
object World2Client

class World2ServerImpl extends World2Server {

  implicit val ResponseCapture =
    new Capture[Response] {
      def apply[A](a: => A) = Response.delay(a)
    }

  val xa = DriverManagerTransactor[Response](
    "org.postgresql.Driver", "jdbc:postgresql:world", "rnorris", ""
  )

  val pop: String => Response[String \/ Int] = name =>
    sql"select population from country where name = $name"
      .query[Int]
      .unique
      .attemptSome { case UnexpectedEnd => s"No such country: $name" }
      .transact(xa)

}

object World2Main extends App {

  val addr = new InetSocketAddress("localhost", 8083)

  // Server
  val server = new World2ServerImpl
  val serverMon = Monitoring.consoleLogger("[server]")
  val stop = server.environment.serveNetty(addr, monitoring = serverMon)

  // Client
  val transport = NettyTransport.single(addr)
  val loc: Endpoint = Endpoint.single(transport)
  val clientMon = Monitoring.consoleLogger("[client]")

  // Try a few
  try { 
    List("France", "Chickenbutt", "Canada").foreach { s =>
      World2Client.pop(s).runWithContext(loc, Response.Context.empty, clientMon).run
    }
  } finally {
    transport.shutdown()
    stop.run
  }

}





