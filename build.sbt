name := """GrazeScape_svr"""
organization := "edu.wisc"
version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.0"

resolvers += "os geo" at ("https://download.osgeo.org/webdav/geotools/")

javacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-parameters",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Werror"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"  % "2.6.3",
  guice,
  javaCore,
  "commons-io" % "commons-io" % "2.6",
  "com.h2database" % "h2" % "1.4.200",
  "org.apache.commons" % "commons-email" % "1.5",
  "org.gdal" % "gdal" % "3.0.0",
  "org.geotools" % "gt-main" % "22.3" ,
  "org.postgresql" % "postgresql" % "42.2.10",  
  "io.ebean" % "ebean" % "11.45.1",
  "io.ebean" % "ebean-agent" % "11.45.1"
)

lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
 
// pomOnly()
// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile, compile in Test)