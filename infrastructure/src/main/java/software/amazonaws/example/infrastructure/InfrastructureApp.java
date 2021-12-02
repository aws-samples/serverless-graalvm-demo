// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.infrastructure;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

//        new InfrastructureStack(app, "GraalVMPerfTestStack", StackProps.builder()
//                .env(Environment.builder()
//                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
//                        .region(System.getenv("CDK_DEFAULT_REGION"))
//                        .build())
//                .build());

        new JVMInfrastructureStack(app, "JVMPerfTestStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());

        app.synth();
    }
}
