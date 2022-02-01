package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import scalatags.Text.all._

case class Stylesheet(
                       uri: Uri,
                       integrity: Option[String] = none,
                       crossorigin: Option[String] = none,
                     ) extends SpaUriDependency {
  override type Self = Stylesheet

  override protected def self: Stylesheet = this

  override def withUri(uri: Uri): Stylesheet = copy(uri = uri)

  override def toTag(baseUri: Uri): Tag =
    link(
      rel := "stylesheet",
      href := uri.rebaseRelative(baseUri).renderString,
      integrity.map(tags.integrity := _),
      crossorigin.map(tags.crossorigin := _),
    )
}