name := "filmweb-to-imdb"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  // SLF4J + Log4j2
  "org.apache.logging.log4j" % "log4j-api" % "2.7",
  "org.apache.logging.log4j" % "log4j-core" % "2.7",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.7",
  // filmweb-api v0.3.3 dependencies
  "commons-codec" % "commons-codec" % "1.10",
  "com.google.code.gson" % "gson" % "2.5",
  "com.google.guava" % "guava" % "19.0",
  // IMDb API
  "com.omertron" % "API-OMDB" % "1.2",
  "com.uwetrottmann.tmdb2" % "tmdb-java" % "1.5.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  // tests
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)
