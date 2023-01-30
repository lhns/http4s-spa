package de.lolhens.http4s.spa

import cats.syntax.option._
import org.http4s.Uri
import org.http4s.implicits._
import scalatags.Text.all.Tag

trait SpaDependencies {
  type Self <: SpaDependencies

  def self: Self

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

  override def transformUris(f: Uri => Uri): Self = withUri(f(uri))

  def uri: Uri

  def withUri(uri: Uri): Self
}

object SpaDependencies {
  def apply(dependencies: SpaDependency*): SpaDependencies = new SpaDependencies {
    override type Self = SpaDependencies

    override def self: SpaDependencies = this

    override def transformUris(f: Uri => Uri): SpaDependencies = SpaDependencies(dependencies.map(_.transformUris(f)): _*)

    override def recurse: Seq[SpaDependency] = dependencies.flatMap(_.recurse)
  }

  val esModuleShims: Script = Script(
    uri"https://ga.jspm.io/npm:es-module-shims@1.6.3/dist/es-module-shims.js",
    async = true
  )

  val react17: ImportMap = ImportMap(
    imports = Map(
      "react" -> uri"https://ga.jspm.io/npm:react@17.0.2/index.js",
      "react-dom" -> uri"https://ga.jspm.io/npm:react-dom@17.0.2/index.js",
    ),
    scopes = Map(
      uri"https://ga.jspm.io/" -> Map(
        "object-assign" -> uri"https://ga.jspm.io/npm:object-assign@4.1.1/index.js",
        "scheduler" -> uri"https://ga.jspm.io/npm:scheduler@0.20.2/index.js",
      )
    )
  )

  val react17Dev: ImportMap = ImportMap(
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

  val bootstrap5: SpaDependencies = SpaDependencies(
    Stylesheet(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css",
      integrity = "sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi".some,
      crossorigin = "anonymous".some,
    ),
    Script(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js",
      integrity = "sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3".some,
      crossorigin = "anonymous".some,
    )
  )

  val bootstrapIcons1: SpaDependencies = SpaDependencies(
    Stylesheet(uri"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.9.1/font/bootstrap-icons.css")
  )

  val uikit3: SpaDependencies = SpaDependencies(
    Stylesheet(uri"https://cdn.jsdelivr.net/npm/uikit@3.15.12/dist/css/uikit.min.css"),
    Script(uri"https://cdn.jsdelivr.net/npm/uikit@3.15.12/dist/js/uikit.min.js"),
    Script(uri"https://cdn.jsdelivr.net/npm/uikit@3.15.12/dist/js/uikit-icons.min.js")
  )

  val mainCss: Stylesheet = Stylesheet(uri"main.css")
}
