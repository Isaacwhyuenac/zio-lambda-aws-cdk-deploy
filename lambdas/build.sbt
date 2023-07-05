import BuildHelper._

lazy val root = module("zio-lambda-example", "lambda-example")
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(buildInfoSettings("zio.lambda.example"))
  .settings(
    publish / skip := true,
    name := "zio-lambda-example",
    stdSettings("zio-lambda-example"),
    assembly / assemblyJarName := "zio-lambda-example.jar",
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
    )
  )
  .dependsOn(zioLambda)