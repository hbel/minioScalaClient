# minioScalaClient

A scala async wrapper and DSL for the minio java client. This client 
can be used to access minio servers or other S3-compatible resources.

m̀`minioScalaClient` also provides a convenient dsl that wraps all inner Futures for you so
that you only have to deal with a single future representing your overall result:

You can use this library for simple access to your buckets:

1. Connect to your host using `MinioHost`:
    * `val host = MinioHost("https://play.minio.io:9000","Q3AM3UQ867SPQQA43P2F","zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")`
2. Access buckets and upload or download files from there:
    * `val allMyFiles = host / "bucket" ls()`
    * `val myFileObject = host / "bucket" / "myFile"`
    * `val download = host / "bucket" / "myFile" download "/tmp/test/file"`

*Note that this is just a wrapper around the official java client! 
Although this client returns Futures for all operations that run asynchronously, the underlying
functions are still synchronous and don't allow fine-granular access
to their state.*

## Planned future improvements

* Add additional tests
* Also allow direct access to input and output streams (so far, only reading and writing real files is supported)
* Provide Akka Stream Sources and Sinks 
