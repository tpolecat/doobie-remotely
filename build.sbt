
organization := "org.tpolecat"

name := "doobie-remotely"

version := "0.1.0-SNAPSHOT"

licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT")))

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-encoding", "UTF-8", // 2 args
  "-feature",                
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-language:reflectiveCalls",
  "-Xlint",
  "-Yno-adapted-args",       
  "-Ywarn-dead-code",       
  "-Ywarn-value-discard"     
)

libraryDependencies ++= Seq(     
  "oncue.svc.remotely" %% "core"        % "1.1-SNAPSHOT",
  "org.tpolecat"       %% "doobie-core" % "0.2.0-SNAPSHOT",
  "org.postgresql"     %  "postgresql"  % "9.3-1102-jdbc41"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

