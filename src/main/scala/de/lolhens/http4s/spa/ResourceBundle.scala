package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import org.http4s.implicits._
import scalatags.Text.all._

case class ResourceBundle(
                           stylesheets: Seq[Stylesheet] = Seq.empty,
                           scripts: Seq[Script] = Seq.empty
                         ) {
  def transformUris(f: Uri => Uri): ResourceBundle = copy(
    stylesheets = stylesheets.map(e => e.withUri(f(e.uri))),
    scripts = scripts.map(e => e.withUri(f(e.uri))),
  )

  def toTags: Seq[Tag] = stylesheets.map(_.toTag) ++ scripts.map(_.toTag)
}

object ResourceBundle {
  val bootstrap5: ResourceBundle = ResourceBundle(
    stylesheets = Seq(Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css",
      integrity = "sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU".some,
      crossorigin = "anonymous".some,
    )),
    scripts = Seq(Script(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js",
      integrity = "sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ".some,
      crossorigin = "anonymous".some,
    ))
  )

  val bootstrapIcons1: ResourceBundle = ResourceBundle(
    stylesheets = Seq(Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css"
    ))
  )
}
