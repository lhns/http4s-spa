package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import scalatags.Text.all._

case class Script(
                   uri: Uri,
                   integrity: Option[String] = none,
                   crossorigin: Option[String] = none,
                   async: Boolean = false,
                   module: Boolean = false,
                 ) extends SpaUriDependency {
  override type Self = Script

  override protected def self: Script = this

  override def withUri(uri: Uri): Script = copy(uri = uri)

  override def toTag(baseUri: Uri): Tag =
    script(
      src := uri.rebaseRelative(baseUri).renderString,
      integrity.map(tags.integrity := _),
      crossorigin.map(tags.crossorigin := _),
      Some(tags.async).filter(_ => async),
      Some(tpe := "module").filter(_ => module),
    )
}