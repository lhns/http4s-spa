package de.lolhens.http4s.spa

import org.http4s.Uri
import scalatags.Text.all.{script => scriptTag, _}

case class InlineScript(
                         script: String,
                         module: Boolean = false,
                       ) extends SpaDependency {
  override type Self = InlineScript

  override def self: InlineScript = this

  override def transformUris(f: Uri => Uri): InlineScript = this

  override def toTag(baseUri: Uri): Tag =
    scriptTag(
      Some(tpe := "module").filter(_ => module),
      script
    )
}