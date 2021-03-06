lazy val scalaVersions = Seq("3.1.3", "2.13.8", "2.12.16")

ThisBuild / scalaVersion := scalaVersions.head
ThisBuild / versionScheme := Some("early-semver")

lazy val commonSettings: SettingsDefinition = Def.settings(
  organization := "de.lolhens",
  version := {
    val Tag = "refs/tags/(.*)".r
    sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
      .getOrElse("0.0.1-SNAPSHOT")
  },

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),

  homepage := scmInfo.value.map(_.browseUrl),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/LolHens/http4s-spa"),
      "scm:git@github.com:LolHens/http4s-spa.git"
    )
  ),
  developers := List(
    Developer(id = "lhns", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/lhns/"))
  ),

  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.11" % Test,
    "de.lolhens" %%% "munit-tagless-final" % "0.2.0" % Test,
    "org.scalameta" %%% "munit" % "0.7.29" % Test,
  ),

  testFrameworks += new TestFramework("munit.Framework"),

  libraryDependencies ++= virtualAxes.?.value.getOrElse(Seq.empty).collectFirst {
    case VirtualAxis.ScalaVersionAxis(version, _) if version.startsWith("2.") =>
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  },

  Compile / doc / sources := Seq.empty,

  publishMavenStyle := true,

  publishTo := sonatypePublishToBundle.value,

  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )).toList
)

name := (core.projectRefs.head / name).value

val V = new {
  val circe = "0.14.2"
  val http4s = "0.23.14"
  val http4sScalatags = "0.24.0"
}

lazy val root: Project =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(
      publishArtifact := false,
      publish / skip := true
    )
    .aggregate(core.projectRefs: _*)

lazy val core = projectMatrix.in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "http4s-spa",

    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % V.circe,
      "io.circe" %%% "circe-generic" % V.circe,
      "org.http4s" %%% "http4s-circe" % V.http4s,
      "org.http4s" %%% "http4s-scalatags" % V.http4sScalatags,
      "org.http4s" %%% "http4s-server" % V.http4s,
    ),
  )
  .jvmPlatform(scalaVersions)
//.jsPlatform(scalaVersions)
