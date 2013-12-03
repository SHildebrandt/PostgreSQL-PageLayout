name := "PostgreSQL PageLayout"

scalaVersion := "2.10.3"

initialCommands in console := """
    import main.scala._
"""

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "com.github.scopt" %% "scopt" % "3.2.0"
)

resolvers += Resolver.sonatypeRepo("public")