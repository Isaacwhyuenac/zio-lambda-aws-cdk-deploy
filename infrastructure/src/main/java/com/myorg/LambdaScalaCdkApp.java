package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.DefaultStackSynthesizer;

public final class LambdaScalaCdkApp {
    public static void main(final String[] args) {
        DefaultStackSynthesizer synthesizer = DefaultStackSynthesizer
                .Builder.create()
                .bucketPrefix("zio-lambda-scala-cdk")
                .build();

        App app = App.Builder.create()
                .defaultStackSynthesizer(synthesizer)
                .build();

        new LambdaScalaCdkStack(app, "LambdaScalaCdkStack" );

        app.synth();
    }
}
