// To publish to Sonatype Central:
// sbt
// ++ 2.12.19
// publishSigned
// sonatypeCentralUpload
// exit
// rm -rf target/sonatype-staging/
// sbt
// ++ 2.13.18
// publishSigned
// sonatypeCentralUpload


import xerial.sbt.Sonatype.sonatypeCentralHost

enablePlugins(ScalaJSPlugin)

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / crossScalaVersions := Seq("2.12.19", "2.13.18")
ThisBuild / organization := "com.github.ahnfelt"
ThisBuild / version := "0.11.0"

ThisBuild / organizationName := "ahnfelt"
ThisBuild / organizationHomepage := Some(url("https://github.com/Ahnfelt"))

ThisBuild / description := "A simple React wrapper for Scala.js"
ThisBuild / licenses := List("MIT" -> url("http://www.opensource.org/licenses/mit-license.php"))
ThisBuild / homepage := Some(url("https://github.com/Ahnfelt/react4s"))

ThisBuild / scmInfo := Some(ScmInfo(
    url("https://github.com/Ahnfelt/react4s"),
    "scm:git@github.com:Ahnfelt/react4s.git"
))

ThisBuild / developers := List(Developer(
    id    = "ahnfelt",
    name  = "Joakim Ahnfelt-Rønne",
    email = "",
    url   = url("https://github.com/Ahnfelt")
))

ThisBuild / Test / publishArtifact := false

sonatypeCredentialHost := sonatypeCentralHost
ThisBuild / publishTo := sonatypePublishToBundle.value

name := "react4s"
scalacOptions += "-feature"

scalaJSLinkerConfig := {
    val fastOptJSURI = (Compile / fastOptJS / artifactPath).value.toURI
    scalaJSLinkerConfig.value.withRelativizeSourceMapBase(Some(fastOptJSURI))
}
