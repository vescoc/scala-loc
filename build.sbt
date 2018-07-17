enablePlugins(JavaAppPackaging)

name := "scala-loc"
version := "0.0.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

unmanagedSources / excludeFilter := HiddenFileFilter || ".#*" || "*~"
