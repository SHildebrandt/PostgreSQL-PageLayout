name := "scopt"

version := "3.2.0"

organization := "com.github.scopt"

homepage := Some(url("https://github.com/scopt/scopt"))

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

description := """a command line options parsing library"""

scalaVersion := "2.10.3"

crossScalaVersions := Seq("2.10.3", "2.9.1", "2.9.2", "2.9.3")

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
	val versionMap = Map(
    "2.9.3" -> "1.12.5-SNAPSHOT",
    "2.9.2" -> "1.12.3",
    "2.9.1" -> "1.12.4", 
    "2.10.3" -> "2.3.3")
	val testVersion = versionMap.getOrElse(sv, error("Unsupported Scala version " + sv))
	deps :+ ("org.specs2" %% "specs2" % testVersion % "test")
}

resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public"

// scaladoc fix
unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))
