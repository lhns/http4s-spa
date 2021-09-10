package de.lolhens.http4s

import scalatags.Text.all._
import scalatags.generic.AttrPair
import scalatags.text.Builder

package object spa {
  private[spa] object tags {
    val integrity: Attr = attr("integrity")
    val crossorigin: Attr = attr("crossorigin")
    val async: AttrPair[Builder, String] = attr("async").empty
  }
}
