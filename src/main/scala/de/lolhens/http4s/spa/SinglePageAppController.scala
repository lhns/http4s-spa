package de.lolhens.http4s.spa

import cats.effect.Async
import cats.syntax.functor._
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent.{ResourceServiceBuilder, WebjarServiceBuilder}

case class SinglePageAppController[F[_] : Async](
                                                  mountPoint: Uri,
                                                  controller: Request[F] => F[SinglePageApp],
                                                  webjarServiceBuilder: WebjarServiceBuilder[F] = WebjarServiceBuilder[F],
                                                  resourceServiceBuilder: Option[ResourceServiceBuilder[F]] = None,
                                                  assetPath: Uri = uri"assets",
                                                ) {
  val toRoutes: HttpRoutes[F] = Router[F](
    Uri.Root.resolve(assetPath).renderString -> {
      webjarServiceBuilder.toRoutes <+>
        resourceServiceBuilder.fold(HttpRoutes.empty)(_.toRoutes)
    },

    "/" -> HttpRoutes.of[F] {
      case request if request.method == Method.GET =>
        controller(request).map { spa =>
          spa(mountPoint.resolve(assetPath))
        }
    }
  )
}
