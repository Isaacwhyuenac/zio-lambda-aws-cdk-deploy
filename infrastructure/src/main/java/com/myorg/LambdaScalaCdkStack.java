package com.myorg;

import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.BundlingOutput;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.DockerImage;
import software.amazon.awscdk.DockerVolume;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class LambdaScalaCdkStack extends Stack {
    public LambdaScalaCdkStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public LambdaScalaCdkStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        BundlingOptions bundlingOptions = BundlingOptions
                .builder()
                .command(new ArrayList<>(Arrays.asList("bash", "-c", "apt-get update -y && apt-get install zip -y && " +
                                                                     "sbt assembly" +
                                                                     " && zip -jr /asset-output/result.zip /asset-input/target/scala-2.13/lambdas-assembly-0.1.0-SNAPSHOT.jar")))
//                .image(Runtime.JAVA_17.getBundlingImage())
                .image(DockerImage.fromRegistry("sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.5_8_1.9.1_2.13.11"))
                .user("root")
                .volumes(
                        Collections.singletonList(
                                DockerVolume.builder().hostPath(System.getProperty("user.home") + "/.m2").containerPath("/root/.m2")
                                        .hostPath(System.getProperty("user.home") + "/.ivy2").containerPath("/root/.ivy2")
                                        .hostPath(System.getProperty("user.home") + "/.sbt").containerPath("/root/.sbt")
                                        .hostPath(System.getProperty("user.home") + "/.cache").containerPath("/root/.cache")
                                        .build()
                        )
                )
                .outputType(BundlingOutput.ARCHIVED)
                .build();

        AssetOptions assetOptions = AssetOptions.builder().bundling(bundlingOptions).build();

        Function lambda = Function.Builder
                .create(this, "LambdaScalaCdkFunction")
                .functionName("test-function")
                .runtime(Runtime.PROVIDED)
                .logRetention(RetentionDays.ONE_DAY)
                .code(Code.fromAsset("../lambdas", assetOptions)).handler("zio.lambda.example.SimpleHandler").build();

        BundlingOptions layerBundlingOptions = BundlingOptions
                .builder()
                .command(new ArrayList<>(
                        Arrays.asList("bash", "-c", "apt-get update -y && apt-get install zip -y && " +
                                                    "sbt universal:packageBin && mv /asset-input/target/universal/layers-0.1.0-SNAPSHOT.zip /asset-output/scala-layer.jar"))
                )
//                .image(Runtime.JAVA_17.getBundlingImage())
                .image(DockerImage.fromRegistry("sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.5_8_1.9.1_2.13.11"))
                .user("root")
                .volumes(
                        Collections.singletonList(
                                // @formatter:off
                                DockerVolume.builder()
                                        .hostPath(System.getProperty("user.home") + "/.m2").containerPath("/root/.m2")
                                        .hostPath(System.getProperty("user.home") + "/.ivy2").containerPath("/root/.ivy2")
                                        .hostPath(System.getProperty("user.home") + "/.sbt").containerPath("/root/.sbt")
                                        .hostPath(System.getProperty("user.home") + "/.cache").containerPath("/root/.cache")
                                        .build()
                                // @formatter:on
                        )
                )
                .outputType(BundlingOutput.ARCHIVED)
                .build();

        AssetOptions layerAssetOptions = AssetOptions.builder().bundling(layerBundlingOptions).build();

        lambda.addLayers(
                LayerVersion.Builder.create(this, "LambdaScalaCdkLayer")
                        .removalPolicy(RemovalPolicy.RETAIN)
                        .layerVersionName("scala-layer")
                        .code(Code.fromAsset("../layers", layerAssetOptions))
                        .compatibleRuntimes(new ArrayList<>(Collections.singletonList(Runtime.PROVIDED)))
                        .build()
        );

        CfnOutput.Builder.create(this, "LambdaScalaCdkFunctionArn")
                .value(lambda.getFunctionArn())
                .build();
    }
}
