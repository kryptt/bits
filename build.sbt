import sbtcrossproject.{crossProject, CrossType}

name := "bits root project"

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding", "utf8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint:adapted-args",
  "-Xlint:by-name-right-associative",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-override",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Xlint:unsound-match",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard")

scalacOptions in (Compile, console) ~= (
  _.filterNot(Set(
                "-Ywarn-unused:imports",
                "-Xfatal-warnings")))

val ScalaVersion  = "2.12.4"
val MonocleVersion = "1.5.0-cats-M1"
val Fs2Version    = "0.10.0-M6"
val Http4sVersion = "0.18.0-M3"
val MongoVersion  = "2.1.0"
val ScalatagsVersion = "0.6.7"

scalaVersion in ThisBuild := "2.12.4"

resolvers in ThisBuild ++= Seq(
  Resolver.bintrayRepo("j-keck", "maven"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies in ThisBuild ++= Seq(
  "com.github.julien-truffaut" %%%  "monocle-macro" % MonocleVersion,
  "co.fs2" %%% "fs2-core" % Fs2Version
)

lazy val root = project.in(file("."))
  .settings(skip in publish := true)
  .aggregate(bitsJVM, bitsJS)

lazy val bits = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    name := "bits"
  )

lazy val bitsJVM = bits.jvm
lazy val bitsJS = bits.js
