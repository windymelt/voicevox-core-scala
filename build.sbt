import Dependencies._
import ReleaseTransformations._

import xerial.sbt.Sonatype.sonatypeCentralHost

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / organization := "dev.capslock"
ThisBuild / organizationName := "windymelt"
ThisBuild / startYear := Some(2024)
ThisBuild / licenses += License.MIT
ThisBuild / homepage := Some(
  url(
    "https://github.com/windymelt/scala-new-maven-central-exercise"
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/windymelt/scala-new-maven-central-exercise"),
    "https://github.com/windymelt/scala-new-maven-central-exercise.git"
  )
)
ThisBuild / developers += Developer(
  "windymelt",
  "windymelt",
  "windymelt@3qe.us",
  url("https://www.3qe.us/")
)
Global / useGpgPinentry := true
usePgpKeyHex("067CA1B7F7EF25BDB29E7EC285008CAC8263E794")
releasePublishArtifactsAction := PgpKeys.publishSigned.value

lazy val downloadCore =
  taskKey[Unit]("Download libcore zip and extract it ./voicevox_core-*")

Compile / compile := (Compile / compile).dependsOn(downloadCore).value

buildInfoPackage := "dev.capslock.voicevoxcore4s"

lazy val common = project
  .settings(
    name := "voicevoxcore4s",
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.12.1",
      "net.java.dev.jna" % "jna-platform" % "5.12.1",
      "com.lihaoyi" %% "os-lib" % "0.7.2", // for extracting resources
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4" // wrapper for SLF4J
    )
  )

lazy val dehydrated = (project in file("."))
  .settings(
    name := "voicevoxcore4s",
    buildInfoKeys := Seq[BuildInfoKey](
      "libcoreFile" -> "you don't need to extract library",
      "libonnxFile" -> "you don't need to extract library"
    ),
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.12.1",
      "net.java.dev.jna" % "jna-platform" % "5.12.1",
      "com.lihaoyi" %% "os-lib" % "0.7.2", // for extracting resources
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4" // wrapper for SLF4J
    ),
    libraryDependencies ++= Seq(
      scalaTest % Test
    ),
    downloadCore := {},
    Compile / unmanagedResourceDirectories ++= {
      Seq(
        baseDirectory.value / "open_jtalk_dic_utf_8-1.11" // 辞書はリソースに含める
      )
    }
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies, // : ReleaseStep
      inquireVersions, // : ReleaseStep
      runClean, // : ReleaseStep
      runTest, // : ReleaseStep
      setReleaseVersion, // : ReleaseStep
      commitReleaseVersion, // : ReleaseStep, performs the initial git checks
      tagRelease, // : ReleaseStep
      releaseStepCommand("publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      releaseStepTask(assembly),
      setNextVersion, // : ReleaseStep
      commitNextVersion, // : ReleaseStep
      pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
    )
  )

// We don't provide Maven Central artifact for this build config due to huge size of library.
lazy val x8664linuxcpu = (project in file("."))
  .settings(
    name := "voicevoxcore4s-linux-x64-cpu",
    buildInfoKeys := Seq[BuildInfoKey](
      "libcoreFile" -> "libvoicevox_core.so",
      "libonnxFile" -> "libonnxruntime.so.1.13.1"
    ),
    libraryDependencies ++= Seq(
      scalaTest % Test
    ),
    downloadCore := {
      if (
        java.nio.file.Files
          .notExists(new File("voicevox_core-linux-x64-cpu-0.14.1").toPath())
      ) {
        println("[libcore] Path does not exist, downloading...")
        IO.unzipURL(
          new URL(
            "https://github.com/VOICEVOX/voicevox_core/releases/download/0.14.1/voicevox_core-linux-x64-cpu-0.14.1.zip"
          ),
          new File("voicevox_core-linux-x64-cpu-0.14.1")
        )
      } else {
        println("[libcore] Path exists, no need to download.")
      }
    },
    Compile / unmanagedResourceDirectories ++= {
      Seq(
        baseDirectory.value / "open_jtalk_dic_utf_8-1.11",
        baseDirectory.value / "voicevox_core-linux-x64-cpu-0.14.1/voicevox_core-linux-x64-cpu-0.14.1/model"
      )
    },
    Compile / unmanagedResources ++= {
      Seq(
        file(
          "voicevox_core-linux-x64-cpu-0.14.1/voicevox_core-linux-x64-cpu-0.14.1/libvoicevox_core.so"
        ),
        file(
          "voicevox_core-linux-x64-cpu-0.14.1/voicevox_core-linux-x64-cpu-0.14.1/libonnxruntime.so.1.13.1"
        )
      )
    }
  )
  .dependsOn(common)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies, // : ReleaseStep
      inquireVersions, // : ReleaseStep
      runClean, // : ReleaseStep
      runTest, // : ReleaseStep
      setReleaseVersion, // : ReleaseStep
      commitReleaseVersion, // : ReleaseStep, performs the initial git checks
      tagRelease, // : ReleaseStep
      // publishArtifacts, // : ReleaseStep, checks whether `publishTo` is properly set up
      releaseStepTask(assembly),
      setNextVersion, // : ReleaseStep
      commitNextVersion, // : ReleaseStep
      pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
    )
  )
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
