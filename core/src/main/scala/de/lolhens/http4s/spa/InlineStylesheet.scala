package de.lolhens.http4s.spa

import org.http4s.Uri
import scalatags.Text.all._

case class InlineStylesheet(styles: String) extends SpaDependency {
  override type Self = InlineStylesheet

  override protected def self: InlineStylesheet = this

  override def transformUris(f: Uri => Uri): InlineStylesheet = this

  override def toTag(baseUri: Uri): Tag =
    tag("style")(
      styles
    )
}