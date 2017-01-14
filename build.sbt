name := "imdb-filmweb-sync"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  // Log4j2
  "org.apache.logging.log4j" % "log4j-api" % "2.7",
  "org.apache.logging.log4j" % "log4j-core" % "2.7",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.7",
  // filmweb-api v0.3.3 dependencies
  "commons-codec" % "commons-codec" % "1.10",
  "com.google.code.gson" % "gson" % "2.5",
  "com.google.guava" % "guava" % "19.0"
)