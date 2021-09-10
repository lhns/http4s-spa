package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import scalatags.Text
import scalatags.Text.all._

case class Script(
                   uri: Uri,
                   integrity: Option[String] = none,
                   crossorigin: Option[String] = none,
                   async: Boolean = false
                 ) extends Resource {
  override type Self = Script

  override def withUri(uri: Uri): Script = copy(uri = uri)

  override def toTag: Text.all.Tag = script(
    src := uri.renderString,
    integrity.map(tags.integrity := _),
    crossorigin.map(tags.crossorigin := _),
    Option.when(async)(tags.async),
  )
}
