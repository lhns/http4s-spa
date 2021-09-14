package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import org.http4s.implicits._
import scalatags.Text.all.Tag

trait SpaDependencies {
  type Self <: SpaDependencies

  protected def self: Self

  def transformUris(f: Uri => Uri): Self

  private[spa] def rebaseRelative(baseUri: Uri): Self =
    if (baseUri == Uri.empty) self
    else transformUris(_.rebaseRelative(baseUri))

  def recurse: Seq[SpaDependency]
}

trait SpaDependency extends SpaDependencies {
  override type Self <: SpaDependency

  override def recurse: Seq[SpaDependency] = Seq(this)

  def toTag(baseUri: Uri): Tag
}

trait SpaUriDependency extends SpaDependency {
  override type Self <: SpaUriDependency

  def uri: Uri

  def withUri(uri: Uri): Self

  override def transformUris(f: Uri => Uri): Self = withUri(f(uri))
}

case class SpaDependencyBundle(dependencies: SpaDependency*) extends SpaDependencies {
  override type Self = SpaDependencyBundle

  override protected def self: SpaDependencyBundle = this

  override def transformUris(f: Uri => Uri): SpaDependencyBundle = SpaDependencyBundle(dependencies.map(_.transformUris(f)): _*)

  override def recurse: Seq[SpaDependency] = dependencies.flatMap(_.recurse)
}

object SpaDependencies {
  val esModuleShims: Script = Script(
    uri"https://ga.jspm.io/npm:es-module-shims@0.10.1/dist/es-module-shims.min.js",
    async = true
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

  val bootstrap5: SpaDependencies = SpaDependencyBundle(
    Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css",
      integrity = "sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU".some,
      crossorigin = "anonymous".some,
    ),
    Script(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js",
      integrity = "sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ".some,
      crossorigin = "anonymous".some,
    )
  )

  val bootstrapIcons1: SpaDependencies = SpaDependencyBundle(
    Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css"
    )
  )

  val mainCss: Stylesheet = Stylesheet(uri"main.css")
}