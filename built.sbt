name         := "primes-server-akkahttp"
organization := "fr.janalyse"
description  := "Primes number http service with API"

licenses += "NON-AI-APACHE2" -> url(s"https://github.com/non-ai-licenses/non-ai-licenses/blob/main/NON-AI-APACHE2")

scalaVersion := "3.10.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

lazy val versions = new {
  // client side dependencies
  val bootstrap = "5.3.8"
  val jquery    = "4.1.0"
  val awesome   = "6.5.2"

  // server side dependencies
  val pureConfig      = "0.17.11"
  val pekko           = "1.7.1"
  val pekkoHttp       = "1.4.2"
  val tapir           = "1.14.1"
  val jsoniter        = "2.43.0"
  val logback         = "1.6.7"
  val slf4j           = "2.0.20"
  val scalatest       = "3.2.20"
  val commonsio       = "2.22.0"
  val webjarsLocator  = "0.52"
  val primes          = "1.2.15"
}

// client side dependencies
libraryDependencies ++= Seq(
  "org.webjars" % "bootstrap"  % versions.bootstrap,
  "org.webjars" % "jquery"     % versions.jquery
)

// server side dependencies
libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig-core"      % versions.pureConfig,
  "org.apache.pekko"      %% "pekko-actor-typed"    % versions.pekko,
  "org.apache.pekko"      %% "pekko-http"           % versions.pekkoHttp,
  "org.apache.pekko"      %% "pekko-http-caching"   % versions.pekkoHttp,
  "org.apache.pekko"      %% "pekko-stream"         % versions.pekko,
  "org.apache.pekko"      %% "pekko-slf4j"          % versions.pekko,
  "org.apache.pekko"      %% "pekko-testkit"        % versions.pekko     % Test,
  "org.apache.pekko"      %% "pekko-stream-testkit" % versions.pekko     % Test,
  "org.apache.pekko"      %% "pekko-http-testkit"   % versions.pekkoHttp % Test,
  "com.softwaremill.sttp.tapir"           %% "tapir-pekko-http-server" % versions.tapir,
  "com.softwaremill.sttp.tapir"           %% "tapir-jsoniter-scala"    % versions.tapir,
  "com.softwaremill.sttp.tapir"           %% "tapir-swagger-ui-bundle" % versions.tapir,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"     % versions.jsoniter,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"   % versions.jsoniter  % "compile-internal",
  "org.slf4j"              % "slf4j-api"            % versions.slf4j,
  "ch.qos.logback"         % "logback-classic"      % versions.logback,
  "commons-io"             % "commons-io"           % versions.commonsio,
  "org.webjars"            % "webjars-locator"      % versions.webjarsLocator,
  "org.scalatest"         %% "scalatest"            % versions.scalatest % Test,
  "fr.janalyse"           %% "primes"               % versions.primes
)

Compile / mainClass    := Some("primes.Main")
packageBin / mainClass := Some("primes.Main")

Test / testOptions += {
  val rel = scalaVersion.value.split("[.]").take(2).mkString(".")
  Tests.Argument(
    "-oDF", // -oW to remove colors
    "-u",
    s"target/junitresults/scala-$rel/"
  )
}

enablePlugins(JavaServerAppPackaging)
enablePlugins(SbtTwirl)

homepage := Some(url("https://github.com/dacr/primes-server-akkahttp"))
scmInfo  := Some(ScmInfo(url(s"https://github.com/dacr/primes-server-akkahttp.git"), s"git@github.com:dacr/primes-server-akkahttp.git"))
developers := List(
  Developer(
    id = "dacr",
    name = "David Crosson",
    email = "crosson.david@gmail.com",
    url = url("https://github.com/dacr")
  )
)

Universal / topLevelDirectory := None
Universal / packageName       := s"${name.value}"
