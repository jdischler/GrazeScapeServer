name := """GrazeScape_svr"""
organization := "edu.wisc"
version := "2.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.0"

javacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-parameters",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Werror"
)

libraryDependencies += guice

// Test Database
libraryDependencies += "com.h2database" % "h2" % "1.4.199"
libraryDependencies ++= Seq(
  javaCore,
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-email" % "1.3.3"
)

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile, compile in Test)