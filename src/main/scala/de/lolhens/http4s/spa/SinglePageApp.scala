package de.lolhens.http4s.spa

import cats.effect.kernel.Sync
import de.lolhens.http4s.spa.SinglePageApp._
import org.http4s.implicits._
import org.http4s.scalatags._
import org.http4s.server.staticcontent.WebjarService.WebjarAsset
import org.http4s.{Response, Uri}
import scalatags.Text.all._

import scala.language.implicitConversions

case class SinglePageApp(
                          webjar: (Uri, WebjarAsset),
                          importMap: ImportMap = ImportMap.empty,
                          resourceBundles: Seq[ResourceBundle] = Seq.empty,
                          rootDivId: String = "root",
                        ) {
  private val resourceTags = resourceBundles.flatMap(_.toTags)

  private val bodyTag = body(
    // ES Module Shims: Import maps polyfill for modules browsers without import maps support (all except Chrome 89+)
    esModuleShims.toTag,
    /*
    JSPM Generator Import Map
    https://generator.jspm.io/
    */
    importMap,
    div(id := rootDivId),
    script(tpe := "module", src := webjarUri(webjar._1, webjar._2).renderString)
  )

  def toTag(
             title: String,
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
        metaTags ++ resourceTags
      ),
      bodyTag
    )
  }

  def apply[F[_] : Sync](
                          title: String,
                          metaAttributes: Map[String, String] = Map.empty,
                        ): Response[F] = {
    val tag = toTag(title, metaAttributes)
    Response[F]().withEntity(tag)
  }
}

object SinglePageApp {
  private def webjarUri(baseUri: Uri, asset: WebjarAsset): Uri =
    baseUri / asset.library / asset.version / asset.asset

  private val esModuleShims = Script(
    uri"https://ga.jspm.io/npm:es-module-shims@0.10.1/dist/es-module-shims.min.js",
    async = true
  )
}
