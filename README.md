# http4s-spa
[![Test Workflow](https://github.com/LolHens/http4s-spa/workflows/test/badge.svg)](https://github.com/LolHens/http4s-spa/actions?query=workflow%3Atest)
[![Release Notes](https://img.shields.io/github/release/LolHens/http4s-spa.svg?maxAge=3600)](https://github.com/LolHens/http4s-spa/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/de.lolhens/http4s-spa_2.13)](https://search.maven.org/artifact/de.lolhens/http4s-spa_2.13)
[![Apache License 2.0](https://img.shields.io/github/license/LolHens/http4s-spa.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)

Helpers for building a [http4s](https://github.com/http4s/http4s) Single Page Application with [Scala.js](https://www.scala-js.org/) using ES-Modules and [Import Maps](https://github.com/WICG/import-maps).

## Features
- Simple setup
- Very customizable
- Hot-reload using debug-mode in IntelliJ

### build.sbt
```sbt
libraryDependencies += "de.lolhens" %% "http4s-spa" % "0.2.0"
```

## Example Setup with scalajs-react
### plugins.sbt
```sbt
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("de.lolhens" % "sbt-scalajs-webjar" % "0.4.0")
```

### build.sbt
```sbt
lazy val frontend = project
  .enablePlugins(ScalaJSWebjarPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % "2.0.0-RC2",
      "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0-RC2",
      "org.scala-js" %%% "scalajs-dom" % "1.1.0",
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    scalaJSUseMainModuleInitializer := true,
  )

lazy val frontendWebjar = frontend.webjar
  .settings(
    webjarAssetReferenceType := Some("http4s"),
    libraryDependencies += "org.http4s" %% "http4s-server" % http4sVersion,
  )

lazy val server = project
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(frontendWebjar)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "de.lolhens" %% "http4s-spa" % "0.2.0",
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
    ),
  )
```

### Server.scala
```scala
import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.option._
import de.lolhens.http4s.spa._
import org.http4s.Uri
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.staticcontent.ResourceServiceBuilder

object Main extends IOApp {
  private val app = SinglePageApp(
    title = "SPA Example",
    webjar = webjars.frontend.webjarAsset,
    dependencies = Seq(
      SpaDependencies.react17,
      SpaDependencies.bootstrap5,
      SpaDependencies.bootstrapIcons1,
      SpaDependencies.mainCss
    ),
  )

  private val appController = SinglePageAppController[IO](
    mountPoint = Uri.Root,
    controller = Kleisli.pure(app),
    resourceServiceBuilder = ResourceServiceBuilder[IO]("/assets").some
  )

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      ec <- Resource.eval(IO.executionContext)
      _ <- BlazeServerBuilder[IO](ec)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(appController.toRoutes.orNotFound)
        .resource
    } yield ())
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
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
