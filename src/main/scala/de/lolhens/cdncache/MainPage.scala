package de.lolhens.cdncache

import org.http4s.Uri
import org.http4s.server.staticcontent.WebjarService.WebjarAsset
import scalatags.Text.all._

import scala.language.implicitConversions

object MainPage {
  private val integrity = attr("integrity")
  private val crossorigin = attr("crossorigin")
  private val async = attr("async").empty

  def apply(title: String,
            importMap: ImportMap,
            webjar: WebjarAsset,
            webjarUri: WebjarAsset => Uri,
            metaAttributes: Map[String, String] = Map.empty): Tag = html(
    head(
      meta(charset := "utf-8"),
      tag("title")(title),
      meta(name := "viewport", content := "width=device-width, initial-scale=1"),
    )(
      metaAttributes.map {
        case (key, value) => meta(name := key, content := value)
      }.toSeq: _*
    ),
    body(
      link(
        href := "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css",
        rel := "stylesheet",
        integrity := "sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC",
        crossorigin := "anonymous",
      ),
      link(
        rel := "stylesheet",
        href := "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css"
      ),
      script(
        src := "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js",
        integrity := "sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM",
        crossorigin := "anonymous",
      ),
      link(
        rel := "stylesheet",
        href := "/assets/main.css"
      ),
      // ES Module Shims: Import maps polyfill for modules browsers without import maps support (all except Chrome 89+)
      script(
        async,
        src := "https://ga.jspm.io/npm:es-module-shims@0.10.1/dist/es-module-shims.min.js"
      ),
      /*
      JSPM Generator Import Map
      https://generator.jspm.io/
      */
      importMap,
      div(id := "root"),
      script(tpe := "module", src := webjarUri(webjar).renderString)
    )
  )
}
