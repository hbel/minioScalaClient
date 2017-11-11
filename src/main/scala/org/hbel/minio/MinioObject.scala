package org.hbel.minio

import io.minio.MinioClient

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * A minio object, usually created by a MinioBucket
  *
  * @param client minio client
  * @param bucket bucket containing the object
  * @param name   object name
  */
class MinioObject(private val client: MinioClient, val bucket: MinioBucket, val name: String)(implicit ctxt: ExecutionContext) {
  private[minio] var invalidated = false

  /**
    * Apply function that will download the object to the given filename
    *
    * @param file target filename (full path)
    * @return future
    */
  def apply(file: String): Future[Unit] = {
    if (invalidated) throw new Exception("Minio object was deleted and is invalid!")
    val p = Promise[Unit]
    Future {
      try {
        client.getObject(bucket.name, name, file)
        p.success(Unit)
      }
      catch {
        case x: Throwable => p.failure(x)
      }
    }
    p.future
  }

  /**
    * Deletes the object
    *
    * @return future
    */
  def delete: Future[Unit] = Future {
    client.removeObject(bucket.name, name)
    invalidated = true
  }
}
