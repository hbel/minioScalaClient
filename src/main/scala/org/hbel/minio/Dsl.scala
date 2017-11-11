package org.hbel.minio

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Provides implicit conversions for futures of hosts, buckets, and objects
  * so that they can be used with a simple dsl
  */
object Dsl {

  /**
    * Access, create and delete buckets on future host objects
    *
    * @param host future of a minio host
    */
  implicit class HostOption(private val host: Future[MinioHost])(implicit ctxt: ExecutionContext) {
    def /(bucket: String): Future[MinioBucket] =
      for (h <- host; b <- h / bucket) yield b

    def /+(bucket: String): Future[MinioBucket] =
      for (h <- host; b <- h /+ bucket) yield b

    def +(bucket: String): Future[MinioBucket] =
      for (h <- host; b <- h + bucket) yield b

    def -(bucket: String): Future[Unit] =
      for (h <- host; b <- h - bucket) yield Unit
  }

  /**
    * Access, list, create and delete objects on future bucket objects
    *
    * @param bucket future of a minio bucket
    */
  implicit class BucketOption(private val bucket: Future[MinioBucket])(implicit ctxt: ExecutionContext) {
    def /(name: String): Future[MinioObject] =
      for (b <- bucket; o <- b / name) yield o

    def upload(name: String, file: String, overwrite: Boolean = false): Future[MinioObject] =
      for (b <- bucket; o <- b upload(name, file, overwrite)) yield o

    def -(name: String): Future[String] =
      for (b <- bucket; o <- b - name) yield o.name

    def ls(): Future[Seq[MinioObject]] =
      for (b <- bucket; o <- b ls) yield o
  }

  /**
    * Download, upload and delete future objects
    *
    * @param obj future of a minio object
    */
  implicit class ObjectOption(private val obj: Future[MinioObject])(implicit ctxt: ExecutionContext) {
    def apply(file: String): Future[Unit] =
      for (o <- obj; f <- o(file)) yield f

    def download(file: String): Future[Unit] =
      apply(file)

    def upload(file: String): Future[MinioObject] =
      for (o <- obj; f <- o.bucket.upload(o.name, file, overwrite = true)) yield f

    def delete: Future[Unit] =
      for (_ <- obj.delete) yield Unit
  }

}
