package org.hbel.minio

import io.minio.MinioClient

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Representation of a minio bucket (usually created by a MinioHost)
  *
  * @param client minio client
  * @param name   bucket name
  */
class MinioBucket(private val client: MinioClient, val name: String)(implicit ctxt: ExecutionContext) {
  private[minio] var invalidated = false

  /**
    * Get an object from the bucket
    *
    * @param objName object name
    * @return future of the object. Will fail if the object does not exist.
    */
  def /(objName: String): Future[MinioObject] = {
    if (invalidated) throw new Exception("Minio bucket was deleted and is invalid!")
    Future {
      if (client.listObjects(name, objName).asScala.size == 1)
        new MinioObject(client, this, objName)
      else throw new Exception(s"No such object $objName")
    }
  }

  /**
    * List all objects in the bucket
    *
    * @return future sequence of objects, may be empty
    */
  def ls: Future[Seq[MinioObject]] = {
    if (invalidated) throw new Exception("Minio bucket was deleted and is invalid!")
    Future {
      for (o: io.minio.Result[io.minio.messages.Item] <- client.listObjects(name).asScala.toSeq)
        yield new MinioObject(client, this, o.get().objectName())
    }
  }

  /**
    * Delete an object from the bucket
    *
    * @param objName name of the object
    * @return future containing the bucket
    */
  def -(objName: String): Future[MinioBucket] = {
    if (invalidated) throw new Exception("Minio bucket was deleted and is invalid!")
    Future {
      if (client.listObjects(name, objName).asScala.size == 1)
        client.removeObject(name, objName)
      this
    }
  }

  /**
    * Delete an object from the bucket
    *
    * @param obj minio object
    * @return future containing the bucket
    */
  def -(obj: MinioObject): Future[MinioBucket] = {
    if (invalidated) throw new Exception("Minio bucket was deleted and is invalid!")
    Future {
      if (client.listObjects(name, obj.name).asScala.size == 1)
        client.removeObject(name, obj.name)
      obj.invalidated = true
      this
    }
  }

  /**
    * Upload a file as an object to the bucket
    *
    * @param objName   object name
    * @param file      file name (full path)
    * @param overwrite whether an existing object may be overwritten (default is no)
    * @return future containing the new object, will fail if the object already exists and overwrite is false
    */
  def upload(objName: String, file: String, overwrite: Boolean = false): Future[MinioObject] = {
    if (invalidated) throw new Exception("Minio bucket was deleted and is invalid!")
    val p = Promise[MinioObject]
    Future {
      try {
        if (!overwrite && client.listObjects(name, objName).asScala.size == 1)
          p.failure(new Exception(s"Object $objName already exists!"))
        else {
          client.putObject(name, objName, file)
          p.success(new MinioObject(client, this, objName))
        }
      }
      catch {
        case x: Throwable => p.failure(x)
      }
    }
    p.future
  }
}
