package org.hbel.minio

import com.typesafe.scalalogging.LazyLogging
import io.minio.MinioClient
import org.hbel.scConfigDsl.Config

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * A single host attached to a minio server
  *
  * @param url    full url of server
  * @param key    access
  * @param secret access secret
  */
class MinioHost(val url: String, private val key: String, private val secret: String)(implicit ctxt: ExecutionContext)
  extends LazyLogging {

  logger.debug(s"Creating new minio client for host $url")
  private val client = new MinioClient(url, key, secret)

  /**
    * Alternative constructor for using scConfigDsl
    *
    * @param hostCfg
    * It is assumed the given config has the following structure
    * {
    * server: "localhost"
    * port: 9000
    * ssl: false
    * key : "MYACCESSKEY"
    * secret : "MYACCESSSECRET"
    * }
    */
  private def this(hostCfg: Config)(implicit ctxt: ExecutionContext) = {
    this((if (hostCfg % "ssl" getOrElse false) "https" else "http")
      + "://" + (hostCfg % "server" getOrElse "localhost")
      + ":" + (hostCfg % "port" getOrElse 9000).toString,
      hostCfg % "key" orNull,
      hostCfg % "secret" orNull)
  }

  /**
    * Access a bucket of the host
    *
    * @param bucket name of the bucket
    * @return Future of the bucket (will fail if the bucket does not exist)
    */
  def /(bucket: String): Future[MinioBucket] = Future {
    if (client.bucketExists(bucket))
      new MinioBucket(client, bucket)
    else throw new Exception(s"No such bucket $bucket")
  }

  /**
    * Access a bucket of the host. If it does not exist, create it.
    *
    * @param bucket name of the bucket
    * @return Future of the bucket (will fail if the bucket does not exist)
    */
  def /+(bucket: String): Future[MinioBucket] = Future {
    if (!client.bucketExists(bucket))
      client.makeBucket(bucket)
    new MinioBucket(client, bucket)
  }

  /**
    * Add a bucket to the server
    *
    * @param bucket name of the new bucket
    * @return Future of the bucket (will fail if the bucket already exists)
    */
  def +(bucket: String): Future[MinioBucket] = Future {
    if (!client.bucketExists(bucket)) {
      client.makeBucket(bucket)
      new MinioBucket(client, bucket)
    }
    else throw new Exception("Bucket already exists")
  }

  /**
    * Remove a bucket from the server.
    * Idempotent operation, this will succeed if the bucket does not exist.
    *
    * @param bucket name of the bucket
    * @return empty future (will fail if the bucket is not empty)
    */
  def -(bucket: String): Future[Unit] = Future {
    if (client.bucketExists(bucket))
      client.removeBucket(bucket)
  }

  /**
    * Remove a bucket from the server.
    * Idempotent operation, this will succeed if the bucket does not exist.
    *
    * @param bucket to delete
    * @return empty future (will fail if the bucket is not empty)
    */
  def -(bucket: MinioBucket): Future[Unit] = Future {
    if (client.bucketExists(bucket.name))
      client.removeBucket(bucket.name)
    bucket.invalidated = true
  }
}

/**
  * Factory functions for minio hosts
  */
object MinioHost {
  def apply(cfg: Option[Config])(implicit ctxt: ExecutionContext): Future[MinioHost] = Future {
    cfg match {
      case None => new MinioHost("http://localhost:9000", null, null)
      case Some(c) => new MinioHost(c)
    }
  }

  def apply(cfg: Config)(implicit ctxt: ExecutionContext): Future[MinioHost] = Future {
    new MinioHost(cfg)
  }

  def apply(url: String, key: String, secret: String)(implicit ctxt: ExecutionContext): Future[MinioHost] = Future {
    new MinioHost(url, key, secret)
  }
}