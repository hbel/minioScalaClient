package org.hbel.minio.specs

import org.hbel.minio.Dsl._
import org.hbel.minio.MinioHost
import org.hbel.scConfigDsl.Config
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class HostSpec extends AsyncFlatSpec with Matchers {

  val hostFixture: Future[MinioHost] = {
    val hostCfg: Option[Config] = new Config() / "minio" / "play"
    MinioHost(hostCfg)
  }

  "A MinioHost" should "eventually be instantiated when a valid host, port, keys are given" in {
    val host = MinioHost("https://play.minio.io:9000", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
    host map { h => assert(h.url == "https://play.minio.io:9000") }
  }

  it should "eventually be instantiated when a config is given" in {
    hostFixture map { h => assert(h.url == "https://play.minio.io:9000") }
  }

  it should "eventually add, return and delete a bucket if it (doesn't) exist" in {
    val bucket = for {
      bucket <- hostFixture + "minioscalaclient"
      bucket2 <- hostFixture / "minioscalaclient"
    } yield bucket2
    bucket map { b => assert(b.name == "minioscalaclient") }
  }

  it should "eventually delete a bucket regardless of its existence" in {
    val bucket = hostFixture - "minioscalaclient"
    bucket map { b => assert(b == (())) }
  }
}
