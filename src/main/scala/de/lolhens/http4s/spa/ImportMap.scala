package de.lolhens.http4s.spa

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s.Uri
import org.http4s.implicits._
import scalatags.Text.all._

import scala.language.implicitConversions

case class ImportMap(
                      imports: Map[String, Uri],
                      scopes: Map[Uri, Map[String, Uri]] = Map.empty
                    ) {
  def withImports(imports: Map[String, Uri]): ImportMap = copy(imports = imports)

  def withScopes(scopes: Map[Uri, Map[String, Uri]]): ImportMap = copy(scopes = scopes)

  def ++(importMap: ImportMap): ImportMap = ImportMap(
    imports = imports ++ importMap.imports,
    scopes = scopes ++ importMap.scopes
  )

  override def toString: String = ImportMap.codec(this).spaces2

  def rewrite(from: Uri, to: Uri): ImportMap = {
    def f(uri: Uri): Uri = ImportMap.rewrite(uri, from, to)

    ImportMap(
      imports = imports.map { case (name, uri) => (name, f(uri)) },
      scopes = scopes.map { case (uri, imports) => (f(uri), imports.map { case (name, uri) => (name, f(uri)) }) }
    )
  }
}

object ImportMap {
  val empty: ImportMap = ImportMap(imports = Map.empty)

  private def rewrite(uri: Uri, from: Uri, to: Uri): Uri = {
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

  implicit val codec: Codec[ImportMap] = {
    implicit val uriCodec: Codec[Uri] = Codec.from(
      Decoder[String].emapTry(Uri.fromString(_).toTry),
      Encoder[String].contramap(_.renderString)
    )

    implicit val uriKeyDecoder: KeyDecoder[Uri] = KeyDecoder.instance(Uri.fromString(_).toOption)
    implicit val uriKeyEncoder: KeyEncoder[Uri] = KeyEncoder.instance(_.renderString)

    deriveCodec
  }

  implicit def importMap2Tag(importMap: ImportMap): Tag = script(
    tpe := "importmap",
    raw(importMap.asJson.spaces2)
  )

  val react17: ImportMap = ImportMap(
    imports = Map(
      "react" -> uri"https://ga.jspm.io/npm:react@17.0.2/dev.index.js",
      "react-dom" -> uri"https://ga.jspm.io/npm:react-dom@17.0.2/dev.index.js",
    ),
    scopes = Map(
      uri"https://ga.jspm.io/" -> Map(
        "object-assign" -> uri"https://ga.jspm.io/npm:object-assign@4.1.1/index.js",
        "scheduler" -> uri"https://ga.jspm.io/npm:scheduler@0.20.2/dev.index.js",
        "scheduler/tracing" -> uri"https://ga.jspm.io/npm:scheduler@0.20.2/dev.tracing.js",
      )
    )
  )
}
