package com.example

import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import gigahorse.FullResponse

object Main {

  private val logger = LoggerFactory.getLogger(getClass)

  private def testScalajHttp(): Unit = {
    import com.example.scalaj.Gigahorse
    val http = Gigahorse.http(Gigahorse.config)
    val request = Gigahorse.url("https://httpbin.org/get").get
    val response = http.processFull(request).await
    logger.info(response.bodyAsString)
  }

  private def testAkkaHttp() = {
    import gigahorse.support.akkahttp.Gigahorse
    Gigahorse.withHttp(Gigahorse.config) { http =>
      val request = Gigahorse.url("https://httpbin.org/get").get
      val response = http.processFull(request).await
      logger.info(response.bodyAsString)
    }
  }

  def testOkHttp() = {
    import gigahorse.support.okhttp.Gigahorse
    Gigahorse.withHttp(Gigahorse.config) { http =>
      val request = Gigahorse.url("https://httpbin.org/get").get
      val response = http.processFull(request).await
      logger.info(response.bodyAsString)
    }
  }

  def main(args: Array[String]): Unit = {
    testAkkaHttp()
    testOkHttp()
    testScalajHttp()
  }

  implicit class AwaitableGigahorseResponseFuture(f: Future[FullResponse]) {
    def await = Await.result(f, Duration(10, TimeUnit.SECONDS))
  }

}
