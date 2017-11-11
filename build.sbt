import Dependencies._

lazy val commonSettings = Seq(
  organization := "org.hbel",
  scalaVersion := "2.12.4",
  version := "0.1.1",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  javaOptions in Test += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.conf",
  fork in Test := true,

  publishArtifact in Test := false,
  publishMavenStyle := true,
  pomIncludeRepository := (_ => false),

  sonatypeProfileName := "hendrik.belitz",

  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/hbel/minioScalaClient")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/hbel/minioScalaClient"),
      "scm:git@github.com:hbel/minioScalaClient.git"
    )
  ),
  developers := List(
    Developer(id = "hbel", name = "Hendrik Belitz", email = "hendrik@hendrikbelitz.de", url = url("https://hendrikbelitz.de"))
  ),

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    val profileM = sonatypeStagingRepositoryProfile.?.value

    if (isSnapshot.value) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
      val staged = profileM map { stagingRepoProfile =>
        "releases" at nexus +
          "service/local/staging/deployByRepositoryId/" +
          stagingRepoProfile.repositoryId
      }

      staged.orElse(Some("releases" at nexus + "service/local/staging/deploy/maven2"))
    }
  }
)

lazy val root = (project in file(".")).
  settings(
    commonSettings,
    name := "minioScalaClient",
    libraryDependencies ++= Seq(scalaTest % Test,
      scalaCheck % Test,
      scalaLogging,
      scConfigDsl,
      minio)
  )
