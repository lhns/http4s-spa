package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import scalatags.Text
import scalatags.Text.all._

case class Stylesheet(
                       uri: Uri,
                       integrity: Option[String] = none,
                       crossorigin: Option[String] = none
                     ) extends Resource {
  override type Self = Stylesheet

  override def withUri(uri: Uri): Stylesheet = copy(uri = uri)

  override def toTag: Text.all.Tag = link(
    rel := "stylesheet",
    href := uri.renderString,
    integrity.map(tags.integrity := _),
    crossorigin.map(tags.crossorigin := _),
  )
}