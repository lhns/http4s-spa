package de.lolhens.http4s.spa

import org.http4s.Uri
import scalatags.Text.all._

trait Resource {
  type Self <: Resource

  def uri: Uri

  def withUri(uri: Uri): Self

  def toTag: Tag
}
