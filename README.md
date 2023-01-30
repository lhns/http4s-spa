# http4s-spa
[![Test Workflow](https://github.com/lhns/http4s-spa/workflows/test/badge.svg)](https://github.com/lhns/http4s-spa/actions?query=workflow%3Atest)
[![Release Notes](https://img.shields.io/github/release/lhns/http4s-spa.svg?maxAge=3600)](https://github.com/lhns/http4s-spa/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/de.lhns/http4s-spa_2.13)](https://search.maven.org/artifact/de.lhns/http4s-spa_2.13)
[![Apache License 2.0](https://img.shields.io/github/license/lhns/http4s-spa.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)

Helpers for building a [http4s](https://github.com/http4s/http4s) Single Page Application with [Scala.js](https://www.scala-js.org/) using ES-Modules and [Import Maps](https://github.com/WICG/import-maps).

## Features
- Simple setup
- Very customizable
- Hot-reload using debug-mode in IntelliJ

### build.sbt
```sbt
libraryDependencies += "de.lhns" %% "http4s-spa" % "0.6.2"
```

## Example Setup with scalajs-react
### plugins.sbt
```sbt
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("de.lolhens" % "sbt-scalajs-webjar" % "0.4.0")
```

### build.sbt
```sbt
val V = new {
  val http4s = "0.23.16"  
  val http4sSpa = "0.6.0"
  val scalajsDom = "2.0.0"
  val scalajsReact = "2.0.0"
}

lazy val frontend = project
  .enablePlugins(ScalaJSWebjarPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % V.scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "extra" % V.scalajsReact,
      "org.scala-js" %%% "scalajs-dom" % V.scalajsDom,
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    scalaJSUseMainModuleInitializer := true,
  )

lazy val frontendWebjar = frontend.webjar
  .settings(
    webjarAssetReferenceType := Some("http4s"),
    libraryDependencies += "org.http4s" %% "http4s-server" % V.http4s,
  )

lazy val server = project
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(frontendWebjar)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "de.lhns" %% "http4s-spa" % V.http4sSpa,
      "org.http4s" %% "http4s-circe" % V.http4s,
      "org.http4s" %% "http4s-dsl" % V.http4s,
      "org.http4s" %% "http4s-ember-server" % V.http4s,
    ),
  )
```

### Server.scala
```scala
import cats.data.Kleisli
import cats.effect._
import cats.syntax.option._
import com.comcast.ip4s._
import de.lolhens.http4s.spa._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.staticcontent.ResourceServiceBuilder
import org.http4s.{HttpApp, Uri}
import org.log4s.getLogger

import scala.concurrent.duration._

object Main extends IOApp {
  private val logger = getLogger

  private val app = SinglePageApp(
    title = "SPA Example",
    webjar = webjars.frontend.webjarAsset,
    dependencies = Seq(
      SpaDependencies.react17,
      SpaDependencies.bootstrap5,
      SpaDependencies.bootstrapIcons1,
      SpaDependencies.mainCss
    )
  )

  private val appController = SinglePageAppController[IO](
    mountPoint = Uri.Root,
    controller = Kleisli.pure(app),
    resourceServiceBuilder = ResourceServiceBuilder[IO]("/assets").some
  )

  override def run(args: List[String]): IO[ExitCode] =
    serverResource(
      SocketAddress(host"0.0.0.0", port"8080"),
      appController.toRoutes.orNotFound
    ).use(_ => IO.never)

  def serverResource[F[_]: Async](socketAddress: SocketAddress[Host], http: HttpApp[F]): Resource[F, Server] =
    EmberServerBuilder.default[F]
      .withHost(socketAddress.host)
      .withPort(socketAddress.port)
      .withHttpApp(ErrorAction.log(
        http = http,
        messageFailureLogAction = (t, msg) => Async[F].delay(logger.debug(t)(msg)),
        serviceErrorLogAction = (t, msg) => Async[F].delay(logger.error(t)(msg))
      ))
      .withShutdownTimeout(1.second)
      .build
}
```

### Frontend.scala
```scala
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.ScalaComponent.BackendScope
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object Main {
  def main(args: Array[String]): Unit = {
    MainComponent.Component()
      .renderIntoDOM(dom.document.getElementById("root"))
  }
}

object MainComponent {
  class Backend($: BackendScope[Unit, Unit]) {
    def render: VdomElement = {
      val state = $.state.unsafeRunSync()

      <.div(
        ^.cls := "container my-4 d-flex flex-column",
        <.h1("Hello World"),
      )
    }
  }

  val Component =
    ScalaComponent.builder[Unit]
      .backend(new Backend(_))
      .render(_.backend.render)
      .build
}
```

## License
This project uses the Apache 2.0 License. See the file called LICENSE.
