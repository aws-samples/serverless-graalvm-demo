// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Function;

import java.util.List;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        InfrastructureStack infrastructureStack = new InfrastructureStack(app, "GraalVMPerfTestStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());

        List<Function> functions = infrastructureStack.getFunctions();

        new DashboardStack(app, "GraalVMDashboard", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build(),
                functions);

        app.synth();
    }
}
