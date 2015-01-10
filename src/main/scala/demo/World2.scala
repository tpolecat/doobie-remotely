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

// When I try to factor out the protocol thus:
//
// object WorldProto {
//   val definition = remotely.Protocol
//     .empty
//     .codec[String]
//     .codec[scalaz.\/[String, Int]]
//     .specify1[String, scalaz.\/[String, Int]]("pop")
// }
//
// @GenServer(demo.WorldProto.definition) 
// abstract class World2Server
//
// I get the following error:
//
// [error] /Users/rnorris/Scala/doobie-remotely/src/main/scala/demo/World2.scala:26: exception during macro expansion: 
// [error] scala.tools.reflect.ToolBoxError: reflective compilation has failed: 
// [error] 
// [error] object definition is not a member of package demo.WorldProto
// [error]   at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl$ToolBoxGlobal.throwIfErrors(ToolBoxFactory.scala:315)
// [error]   at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl$ToolBoxGlobal.compile(ToolBoxFactory.scala:249)
// [error]   at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl.compile(ToolBoxFactory.scala:416)
// [error]   at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl.eval(ToolBoxFactory.scala:419)
// [error]   at scala.reflect.macros.runtime.Evals$class.eval(Evals.scala:16)
// [error]   at scala.reflect.macros.runtime.Context.eval(Context.scala:6)
// [error]   at remotely.GenServer$.impl(GenServer.scala:57)
// [error] @GenServer(demo.WorldProto.definition) 
// [error]  ^

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
  val pool = Executors.newCachedThreadPool
  val stop = server.environment.serveNetty(addr, pool, serverMon)

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
    stop()
    pool.shutdown()
  }

}





