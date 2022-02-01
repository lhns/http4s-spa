package de.lolhens.http4s.spa

import cats.data.NonEmptySeq
import cats.effect.kernel.Sync
import org.http4s.scalatags._
import org.http4s.server.staticcontent.WebjarService.WebjarAsset
import org.http4s.{Response, Uri}
import scalatags.Text.all._

import scala.language.implicitConversions

case class SinglePageApp(
                          title: String,
                          webjar: WebjarAsset,
                          dependencies: Seq[SpaDependencies] = Seq.empty,
                          metaAttributes: Map[String, String] = Map.empty,
                          rootDivId: String = "root",
                        ) {
  private val jsCss: Seq[SpaDependency] = dependencies.flatMap(_.recurse).collect {
    case script: Script => script
    case inlineScript: InlineScript => inlineScript
    case stylesheet: Stylesheet => stylesheet
    case inlineStylesheet: InlineStylesheet => inlineStylesheet
  }

  private val importMaps: Seq[ImportMap] = dependencies.flatMap(_.recurse).collect {
    case importMap: ImportMap => importMap
  }

  private val metaTags = metaAttributes.map {
    case (key, value) => meta(name := key, content := value)
  }.toSeq

  def toTag(assetBaseUri: Uri): Tag = {
    html(
      head(
        meta(charset := "utf-8"),
        tag("title")(title),
        meta(name := "viewport", content := "width=device-width, initial-scale=1"),
        metaTags,
        // ES Module Shims: Import maps polyfill for modules browsers without import maps support (all except Chrome 89+)
        SpaDependencies.esModuleShims.toTag(assetBaseUri),
        //importMaps.map(_.toTag(assetBaseUri)), TODO: multiple import maps not supported
        NonEmptySeq.fromSeq(importMaps).map(_.reduce.toTag(assetBaseUri)),
        jsCss.map(_.toTag(assetBaseUri)),
      ),
      body(
        div(id := rootDivId),
        script(tpe := "module", src := webjar.uri(assetBaseUri).renderString),
      )
    )
  }

  def apply[F[_] : Sync](assetBaseUri: Uri): Response[F] =
    Response[F]().withEntity(toTag(assetBaseUri))
}