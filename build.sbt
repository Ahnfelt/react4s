enablePlugins(ScalaJSPlugin)

name := "react4s"
organization := "com.github.ahnfelt"
version := "0.9.6-SNAPSHOT"

// Publish cross versions with: sbt +publish
crossScalaVersions := Seq("2.11.8", scalaVersion.value)
scalaVersion := "2.12.1"
scalacOptions += "-feature"


publishMavenStyle := true
publishArtifact in Test := false
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if(isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
    <url>https://github.com/Ahnfelt/react4s</url>
    <licenses>
        <license>
            <name>MIT-style</name>
            <url>http://www.opensource.org/licenses/mit-license.php</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>git@github.com:Ahnfelt/react4s.git</url>
        <connection>scm:git:git@github.com:Ahnfelt/react4s.git</connection>
    </scm>
    <developers>
        <developer>
            <id>ahnfelt</id>
            <name>Joakim Ahnfelt-Rønne</name>
            <url>https://github.com/Ahnfelt</url>
        </developer>
    </developers>
