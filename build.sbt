organization := "de.lolhens"
name := "http4s-spa"
version := {
  val Tag = "refs/tags/(.*)".r
  sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
    .getOrElse("0.0.1-SNAPSHOT")
}

scalaVersion := "2.13.6"
crossScalaVersions := Seq("2.12.14", scalaVersion.value)

ThisBuild / versionScheme := Some("early-semver")

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/LolHens/http4s-spa"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/LolHens/http4s-spa"),
    "scm:git@github.com:LolHens/http4s-spa.git"
  )
)
developers := List(
  Developer(id = "LolHens", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/LolHens/"))
)

val circeVersion = "0.14.1"
val http4sVersion = "0.23.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-scalatags" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

Compile / doc / sources := Seq.empty

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  username,
  password
)).toList
