package de.lolhens.http4s.spa

import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.functor._
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent.{ResourceServiceBuilder, WebjarServiceBuilder}

case class SinglePageAppController[F[_] : Async](
                                                  mountPoint: Uri,
                                                  controller: Kleisli[F, Request[F], SinglePageApp],
                                                  resourceServiceBuilder: Option[ResourceServiceBuilder[F]],
                                                  webjarServiceBuilder: WebjarServiceBuilder[F] = WebjarServiceBuilder[F],
                                                  assetPath: Uri = uri"assets",
                                                ) {
  val toRoutes: HttpRoutes[F] = Router[F](
    assetPath.rebaseRelative(Uri.Root).renderString -> {
      webjarServiceBuilder.toRoutes <+>
        resourceServiceBuilder.fold(HttpRoutes.empty)(_.toRoutes)
    },

    "/" -> {
      val mountedAssetPath = assetPath.rebaseRelative(mountPoint)
      HttpRoutes.of[F] {
        case request if request.method == Method.GET =>
          controller(request).map { spa =>
            spa(mountedAssetPath)
          }
      }
    }
  )
}