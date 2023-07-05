import BuildHelper._

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "layer"


val awsLambdaJavaTests = "1.1.1"

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    stdSettings("zio-lambda"),
    name := "layers",
    scalacOptions -= "-Xfatal-warnings",
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-tests" % awsLambdaJavaTests % "test"
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-json" % "0.5.0",
      "dev.zio" %% "zio-lambda" % "1.0.4",
      "dev.zio" %% "zio-lambda-event" % "1.0.4",
      "dev.zio" %% "zio-lambda-response" % "1.0.4",
      "dev.zio" %% "zio-test" % "2.0.15" % Test
    ),

    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(
    topLevelDirectory := None,
    Universal / mappings ++= Seq(file("bootstrap") -> "bootstrap"),
    Compile / mainClass := Some("zio.lambda.internal.ZLambdaAppReflective")
  )