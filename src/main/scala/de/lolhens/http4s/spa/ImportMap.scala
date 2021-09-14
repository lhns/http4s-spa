package de.lolhens.http4s.spa

import io.circe._
import io.circe.generic.semiauto._
import org.http4s.Uri
import scalatags.Text.all._

import scala.language.implicitConversions

case class ImportMap(
                      imports: Map[String, Uri],
                      scopes: Map[Uri, Map[String, Uri]] = Map.empty
                    ) extends SpaDependency {
  override type Self = ImportMap

  override protected def self: ImportMap = this

  def withImports(imports: Map[String, Uri]): ImportMap = copy(imports = imports)

  def withScopes(scopes: Map[Uri, Map[String, Uri]]): ImportMap = copy(scopes = scopes)

  def ++(importMap: ImportMap): ImportMap = ImportMap(
    imports = imports ++ importMap.imports,
    scopes = scopes ++ importMap.scopes.map {
      case (uri, imports) => uri -> (scopes.getOrElse(uri, Map.empty) ++ imports)
    }
  )

  override def transformUris(f: Uri => Uri): ImportMap = copy(
    imports = imports.map { case (name, uri) => (name, f(uri)) },
    scopes = scopes.map { case (uri, imports) => (f(uri), imports.map { case (name, uri) => (name, f(uri)) }) }
  )

  lazy val toJson: Json = ImportMap.codec(this)

  override def toTag(baseUri: Uri): Tag =
    script(
      tpe := "importmap",
      raw(rebaseRelative(baseUri).toJson.spaces2)
    )

  override def toString: String = toJson.spaces2
}

object ImportMap {
  val empty: ImportMap = ImportMap(imports = Map.empty)

  implicit val codec: Codec[ImportMap] = {
    implicit val uriCodec: Codec[Uri] = Codec.from(
      Decoder[String].emapTry(Uri.fromString(_).toTry),
      Encoder[String].contramap(_.renderString)
    )

    implicit val uriKeyDecoder: KeyDecoder[Uri] = KeyDecoder.instance(Uri.fromString(_).toOption)
    implicit val uriKeyEncoder: KeyEncoder[Uri] = KeyEncoder.instance(_.renderString)

    deriveCodec
  }
}