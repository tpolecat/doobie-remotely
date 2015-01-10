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

object World {

  implicit val ResponseCapture =
    new Capture[Response] {
      def apply[A](a: => A) = Response.delay(a)
    }

  val xa = DriverManagerTransactor[Response](
    "org.postgresql.Driver", "jdbc:postgresql:world", "rnorris", ""
  )

  def pop(name: String): Response[String \/ Int] = 
    sql"select population from country where name = $name"
      .query[Int]
      .unique
      .attemptSome { case UnexpectedEnd => s"No such country: $name" }
      .transact(xa)

  val env = Environment.empty
    .codec[String]
    .codec[String \/ Int]
    .populate { _
      .declare("pop", pop _)
    }

}

object WorldMain extends App {
  import World.env
  
  val addr = new InetSocketAddress("localhost", 8083)
  val pool = Executors.newCachedThreadPool
  val server = env.serveNetty(addr, pool, Monitoring.consoleLogger("[server]"))
  val transport = NettyTransport.single(addr)
  val loc: Endpoint = Endpoint.single(transport)
  
  def result[A: TypeTag: Codec](r: Remote[A]): Task[A] =
    r.runWithContext(loc, Response.Context.empty, Monitoring.consoleLogger("[client]"))

  val pop = Remote.ref[String => String \/ Int]("pop")

  try { 
    result(pop("France")).run
    result(pop("Chickenbutt")).run
    result(pop("Canada")).run 
  } finally {
    transport.shutdown()
    server()
    pool.shutdown()
  }

}
