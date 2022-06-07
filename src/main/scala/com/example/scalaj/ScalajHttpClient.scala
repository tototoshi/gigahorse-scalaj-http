package com.example.scalaj

import gigahorse.Config
import gigahorse.FullResponse
import gigahorse.FutureLifter
import gigahorse.GigahorseSupport
import gigahorse.HttpClient
import gigahorse.Request
import gigahorse.StatusError
import gigahorse.support.okhttp
import gigahorse.{WebSocket, WebSocketEvent}
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse

object Gigahorse extends GigahorseSupport {
  def http(config: Config): HttpClient = new ScalajHttpClient(config)
}

object OkHandler {
  def handle(response: FullResponse): FullResponse = {
    response.status match {
      case s if s / 100 == 2 => response
      case _ => throw StatusError(response.status)
    }
  }
}

class ScalajHttpClient(config: Config) extends HttpClient {

  override def underlying[A]: A =
    throw new UnsupportedOperationException()

  override def close(): Unit = {}

  override def run(request: Request): Future[FullResponse] = {
    processFull(request, OkHandler.handle)
  }

  override def run[A](request: Request, f: FullResponse => A): Future[A] =
    processFull(request, (OkHandler.handle _) andThen f)

  override def run[A](request: Request, lifter: FutureLifter[A])(implicit
      ec: ExecutionContext
  ): Future[Either[Throwable, A]] = {
    lifter.run(run(request))
  }

  override def download(request: Request, file: File): Future[File] = {
    run(
      request,
      response => {
        Files.write(file.toPath(), response.bodyAsString.getBytes(StandardCharsets.UTF_8))
        file
      }
    )
  }

  override def processFull(request: Request): Future[FullResponse] = {
    processFull(request, identity)
  }

  override def processFull[A](request: Request, f: FullResponse => A): Future[A] = {
    val response = buildRequest(request).asBytes
    val fullResponse = toFullResponse(response)
    Future.successful(f(fullResponse))
  }

  override def processFull[A](request: Request, lifter: FutureLifter[A])(implicit
      ec: ExecutionContext
  ): Future[Either[Throwable, A]] = {
    lifter.run(run(request))
  }

  override def websocket(request: Request)(handler: PartialFunction[WebSocketEvent, Unit]): Future[WebSocket] =
    throw new UnsupportedOperationException()

  private def buildRequest(request: Request): HttpRequest = {
    Http(request.url)
      .timeout(config.connectTimeout.toMillis.toInt, config.readTimeout.toMillis.toInt)
      .method(request.method)
      .params(multiMapToTuple2Seq(request.queryString))
      .headers(multiMapToTuple2Seq(request.headers))
  }

  private def multiMapToTuple2Seq[A, B](m: Map[A, List[B]]): Seq[(A, B)] = {
    (for {
      e <- m
      k = e._1
      v <- e._2
    } yield (k, v)).toSeq
  }

  private def toFullResponse(response: HttpResponse[Array[Byte]]): FullResponse = {
    new FullResponse {
      override def underlying[A]: A = response.asInstanceOf[A]
      override def close(): Unit = {}
      override def allHeaders: Map[String, List[String]] = response.headers.mapValues(_.toList).toMap
      override def bodyAsString: String = new String(response.body, StandardCharsets.UTF_8)
      override def bodyAsByteBuffer: ByteBuffer = ByteBuffer.wrap(response.body)
      override def status: Int = response.code
      override def statusText: String = response.statusLine
      override def header(key: String): Option[String] = response.header(key)
    }
  }

}
