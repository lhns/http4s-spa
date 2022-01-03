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

  override def transformUris(f: Uri => Uri): Self = withUri(f(uri))

  def uri: Uri

  def withUri(uri: Uri): Self
}

object SpaDependencies {
  def apply(dependencies: SpaDependency*): SpaDependencies = new SpaDependencies {
    override type Self = SpaDependencies

    override protected def self: SpaDependencies = this

    override def transformUris(f: Uri => Uri): SpaDependencies = SpaDependencies(dependencies.map(_.transformUris(f)): _*)

    override def recurse: Seq[SpaDependency] = dependencies.flatMap(_.recurse)
  }

  val esModuleShims: Script = Script(
    uri"https://ga.jspm.io/npm:es-module-shims@0.12.8/dist/es-module-shims.min.js",
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
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css",
      integrity = "sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3".some,
      crossorigin = "anonymous".some,
    ),
    Script(
      uri"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js",
      integrity = "sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p".some,
      crossorigin = "anonymous".some,
    )
  )

  val bootstrapIcons1: SpaDependencies = SpaDependencies(
    Stylesheet(uri"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.1/font/bootstrap-icons.css")
  )

  val uikit3: SpaDependencies = SpaDependencies(
    Stylesheet(uri"https://cdn.jsdelivr.net/npm/uikit@3.9.4/dist/css/uikit.min.css"),
    Script(uri"https://cdn.jsdelivr.net/npm/uikit@3.9.4/dist/js/uikit.min.js"),
    Script(uri"https://cdn.jsdelivr.net/npm/uikit@3.9.4/dist/js/uikit-icons.min.js")
  )

  val mainCss: Stylesheet = Stylesheet(uri"main.css")
}
