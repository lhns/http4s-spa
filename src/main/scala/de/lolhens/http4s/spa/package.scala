package de.lolhens.http4s

import org.http4s.Uri
import scalatags.Text.all._
import scalatags.generic.AttrPair
import scalatags.text.Builder

package object spa {
  private[spa] object tags {
    val integrity: Attr = attr("integrity")
    val crossorigin: Attr = attr("crossorigin")
    val async: AttrPair[Builder, String] = attr("async").empty
  }

  implicit class UriRewriteOps(val uri: Uri) extends AnyVal {
    def rewrite(from: Uri, to: Uri): Uri = {
      val splitOption =
        if (
          uri.scheme == from.scheme &&
            uri.authority == from.authority &&
            uri.path.startsWith(from.path)
        ) {
          if (from.path.isEmpty) Some(0)
          else uri.path.findSplit(from.path)
        } else {
          None
        }

      splitOption match {
        case Some(split) =>
          val newSegments = to.path.segments ++ uri.path.segments.drop(split)
          uri.copy(
            scheme = to.scheme,
            authority = to.authority,
            path = Uri.Path(
              segments = newSegments,
              absolute = to.path.absolute || (to.authority.nonEmpty && to.path.isEmpty && newSegments.nonEmpty),
              endsWithSlash = uri.path.endsWithSlash
            )
          )

        case None =>
          uri
      }
    }
  }
}
