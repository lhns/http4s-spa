package de.lolhens.http4s.spa

import org.http4s.Uri
import org.http4s.server.staticcontent.WebjarService.WebjarAsset
import scalatags.Text.all._

import scala.language.implicitConversions

object SinglePageApp {
  private def webjarUri(baseUri: Uri, asset: WebjarAsset): Uri =
    baseUri / asset.library / asset.version / asset.asset

  def apply(
             title: String,
             webjar: (Uri, WebjarAsset),
             importMap: ImportMap = ImportMap.empty,
             resourceBundles: Seq[ResourceBundle] = Seq.empty,
             metaAttributes: Map[String, String] = Map.empty,
           ): Tag = {
    val metaTags = metaAttributes.map {
      case (key, value) => meta(name := key, content := value)
    }.toSeq

    html(
      head(
        meta(charset := "utf-8"),
        tag("title")(title),
        meta(name := "viewport", content := "width=device-width, initial-scale=1"),
      )(
        metaTags ++ resourceBundles.flatMap(_.toTags)
      ),
      body(
        // ES Module Shims: Import maps polyfill for modules browsers without import maps support (all except Chrome 89+)
        script(
          tags.async,
          src := "https://ga.jspm.io/npm:es-module-shims@0.10.1/dist/es-module-shims.min.js"
        ),
        /*
        JSPM Generator Import Map
        https://generator.jspm.io/
        */
        importMap,
        div(id := "root"),
        script(tpe := "module", src := webjarUri(webjar._1, webjar._2).renderString)
      )
    )
  }
}
