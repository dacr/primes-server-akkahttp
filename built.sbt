name := "primes-server-akkahttp"
organization :="fr.janalyse"
homepage := Some(new URL("https://github.com/dacr/primes-server-akkahttp"))
licenses += "Apache 2" -> url(s"http://www.apache.org/licenses/LICENSE-2.0.txt")
scmInfo := Some(ScmInfo(url(s"https://github.com/dacr/primes-server-akkahttp.git"), s"git@github.com:dacr/primes-server-akkahttp.git"))

scalaVersion := "2.13.1"
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

lazy val versions = new {
  // client side dependencies
  val bootstrap        = "4.4.1"
  val jquery           = "3.4.1"
  val popperjs         = "1.16.0"

  // server side dependencies
  val pureConfig       = "0.12.3"
  val akka             = "2.6.4"
  val akkaHttp         = "10.1.11"
  val akkaHttpJson4s   = "1.31.0"
  val json4s           = "3.6.7"
  val logback          = "1.2.3"
  val slf4j            = "1.7.30"
  val scalatest        = "3.1.1"
  val webjarsLocator   = "0.39"
  val scalate          = "1.9.5"
  val primes           = "1.2.2"
}

// client side dependencies
libraryDependencies ++= Seq(
  "org.webjars" % "bootstrap" % versions.bootstrap,
  "org.webjars" % "jquery"    % versions.jquery,
  "org.webjars" % "popper.js" % versions.popperjs,
)

// server side dependencies
libraryDependencies ++= Seq(
  "com.github.pureconfig"  %% "pureconfig"          % versions.pureConfig,
  "org.json4s"             %% "json4s-native"       % versions.json4s,
  "org.json4s"             %% "json4s-ext"          % versions.json4s,
  "com.typesafe.akka"      %% "akka-http"           % versions.akkaHttp,
  "com.typesafe.akka"      %% "akka-stream"         % versions.akka,
  "com.typesafe.akka"      %% "akka-slf4j"          % versions.akka,
  "com.typesafe.akka"      %% "akka-testkit"        % versions.akka % Test,
  "com.typesafe.akka"      %% "akka-stream-testkit" % versions.akka % Test,
  "com.typesafe.akka"      %% "akka-http-testkit"   % versions.akkaHttp % Test,
  "de.heikoseeberger"      %% "akka-http-json4s"    % versions.akkaHttpJson4s,
  "org.slf4j"              %  "slf4j-api"           % versions.slf4j,
  "ch.qos.logback"         %  "logback-classic"     % versions.logback,
  "org.webjars"            %  "webjars-locator"     % versions.webjarsLocator,
  "org.scalatra.scalate"   %% "scalate-core"        % versions.scalate,
  "org.scalatest"          %% "scalatest"           % versions.scalatest % Test,
  "fr.janalyse"            %% "primes"              % versions.primes
)
