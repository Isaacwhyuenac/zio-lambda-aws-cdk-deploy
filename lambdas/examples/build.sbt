import BuildHelper._

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(buildInfoSettings("zio.lambda.example"))
  .settings(
    publish / skip := true,
    GraalVMNativeImage / mainClass := Some("zio.lambda.example.SimpleHandler"),
    GraalVMNativeImage / containerBuildImage := GraalVMNativeImagePlugin
      .generateContainerBuildImage(
        "hseeberger/scala-sbt:graalvm-ce-21.3.0-java17_1.6.2_3.1.1"
      )
      .value,
    graalVMNativeImageOptions := Seq(
      "--verbose",
      "--no-fallback",
      "--install-exit-handlers",
      "--enable-http",
      "--allow-incomplete-classpath",
      "--report-unsupported-elements-at-runtime",
      "-H:+StaticExecutableWithDynamicLibC",
      "-H:+RemoveSaturatedTypeFlows"
    ),
    name := "lambdas",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15" % Provided,
      "dev.zio" %% "zio-json" % "0.5.0" % Provided,
      "dev.zio" %% "zio-lambda" % "1.0.4" % Provided,
      "dev.zio" %% "zio-lambda-event" % "1.0.4" % Provided,
      "dev.zio" %% "zio-lambda-response" % "1.0.4" % Provided,
      "dev.zio" %% "zio-test" % "2.0.15" % Test
    ),

    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
