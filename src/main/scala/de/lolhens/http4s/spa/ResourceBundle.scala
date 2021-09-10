package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.implicits._
import scalatags.Text.all._

case class ResourceBundle(
                           stylesheets: Seq[Stylesheet] = Seq.empty,
                           scripts: Seq[Script] = Seq.empty
                         ) {
  def toTags: Seq[Tag] = stylesheets.map(_.toTag) ++ scripts.map(_.toTag)
}

object ResourceBundle {
  val bootstrap5: ResourceBundle = ResourceBundle(
    stylesheets = Seq(Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css",
      integrity = "sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC".some,
      crossorigin = "anonymous".some,
    )),
    scripts = Seq(Script(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js",
      integrity = "sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM".some,
      crossorigin = "anonymous".some,
    ))
  )

  val bootstrapIcons1: ResourceBundle = ResourceBundle(
    stylesheets = Seq(Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css"
    ))
  )
}
