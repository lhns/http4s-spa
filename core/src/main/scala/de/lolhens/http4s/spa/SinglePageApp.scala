package de.lolhens.http4s.spa

import cats.data.NonEmptyList
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
                          esModuleShims: Option[SpaDependency] = Some(SpaDependencies.esModuleShims),
                        ) {
  private val css: Seq[SpaDependency] = dependencies.flatMap(_.recurse).collect {
    case stylesheet: Stylesheet => stylesheet
    case inlineStylesheet: InlineStylesheet => inlineStylesheet
  }

  private val js: Seq[SpaDependency] = dependencies.flatMap(_.recurse).collect {
    case script: Script => script
    case inlineScript: InlineScript => inlineScript
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
        css.map(_.toTag(assetBaseUri)),
      ),
      body(
        // ES Module Shims: Import maps polyfill for modules browsers without import maps support (all except Chrome 89+)
        esModuleShims.map(_.toTag(assetBaseUri)),
        //importMaps.map(_.toTag(assetBaseUri)), TODO: multiple import maps not supported
        NonEmptyList.fromList(importMaps.toList).map(_.reduce.toTag(assetBaseUri)),
        js.map(_.toTag(assetBaseUri)),
        div(id := rootDivId),
        script(tpe := "module", src := webjar.uri(assetBaseUri).renderString),
      )
    )
  }

  def apply[F[_] : Sync](assetBaseUri: Uri): Response[F] =
    Response[F]().withEntity(toTag(assetBaseUri))
}