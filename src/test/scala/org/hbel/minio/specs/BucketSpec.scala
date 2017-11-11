package org.hbel.minio.specs

import org.hbel.minio.Dsl._
import org.hbel.minio.{MinioBucket, MinioHost, MinioObject}
import org.hbel.scConfigDsl.Config
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class BucketSpec extends AsyncFlatSpec with Matchers {

  private val testFilePath = getClass.getResource("/test.txt").toString.substring(5)

  private val bucketFixture: Future[MinioBucket] = {
    val hostCfg: Option[Config] = new Config() / "minio" / "play"
    MinioHost(hostCfg) /+ "scalabucket"
  }

  "A MinioBucket" should "return its contents" in {
    val dir: Future[Seq[MinioObject]] = bucketFixture ls()
    dir map { d => assert(d != null) }
  }

  "A MinioBucket" should "upload a file" in {
    val obj: Future[MinioObject] = bucketFixture upload("test.txt", testFilePath, true)
    obj map { o => assert(o.name == "test.txt") }
  }

  "A MinioBucket" should "download a file" in {
    val obj: Future[Unit] = for {
      upl <- bucketFixture upload("test.txt", testFilePath, true)
      dwn <- bucketFixture / "test.txt" download "/tmp/test2.txt"
    } yield dwn
    obj map { _ =>
      assert(new java.io.File(
        "/tmp/test2.txt").exists())
    }
  }
}
