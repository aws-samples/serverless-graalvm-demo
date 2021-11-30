// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.infrastructure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegrationProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.core.BundlingOutput.ARCHIVED;

public class InfrastructureStack extends Stack {

    public InfrastructureStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public InfrastructureStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Table productsTable = Table.Builder.create(this, "Products")
                .tableName("Products")
                .partitionKey(Attribute.builder()
                        .type(AttributeType.STRING)
                        .name("PK")
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        List<String> functionOnePackagingInstructions = Arrays.asList(
                "-c",
                "cd products " +
                        "&& mvn clean install -P native-image "
                       + "&& cp /asset-input/products/target/function.zip /asset-output/"
        );

        BundlingOptions builderOptions = BundlingOptions.builder()
                .command(functionOnePackagingInstructions)
                .image(DockerImage.fromRegistry("marksailes/al2-graalvm:17-21.3.0"))
//                .image(DockerImage.fromRegistry("marksailes/arm64-al2-graalvm:17-21.3.0"))
                .volumes(singletonList(
                        DockerVolume.builder()
                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                .containerPath("/root/.m2/")
                                .build()
                ))
                .user("root")
                .outputType(ARCHIVED)
                .build();

        Map<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put("PRODUCT_TABLE_NAME", productsTable.getTableName());

        Function getProductFunction = Function.Builder.create(this, "GetProductFunction")
                .runtime(Runtime.PROVIDED_AL2)
                .code(Code.fromAsset("../software/", AssetOptions.builder()
                        .bundling(builderOptions)
                        .build()))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayGetProductRequestHandler")
                .memorySize(256)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
//                .architecture(Architecture.ARM_64)
                .build();

        Function getallProductFunction = Function.Builder.create(this, "GetAllProductFunction")
                .runtime(Runtime.PROVIDED_AL2)
                .code(Code.fromAsset("../software/", AssetOptions.builder()
                        .bundling(builderOptions)
                        .build()))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayGetAllProductRequestHandler")
                .memorySize(256)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
//                .architecture(Architecture.ARM_64)
                .build();

        Function putProductFunction = Function.Builder.create(this, "PutProductFunction")
                .runtime(Runtime.PROVIDED_AL2)
                .code(Code.fromAsset("../software/", AssetOptions.builder()
                        .bundling(builderOptions)
                        .build()))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayPutProductRequestHandler")
                .memorySize(256)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
//                .architecture(Architecture.ARM_64)
                .build();

        Function deleteProductFunction = Function.Builder.create(this, "DeleteProductFunction")
                .runtime(Runtime.PROVIDED_AL2)
                .code(Code.fromAsset("../software/", AssetOptions.builder()
                        .bundling(builderOptions)
                        .build()))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayDeleteProductRequestHandler")
                .memorySize(256)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
//                .architecture(Architecture.ARM_64)
                .build();

        productsTable.grantReadData(getProductFunction);
        productsTable.grantReadData(getallProductFunction);
        productsTable.grantWriteData(putProductFunction);
        productsTable.grantWriteData(deleteProductFunction);

        HttpApi httpApi = HttpApi.Builder.create(this, "ProductsApi")
                .apiName("ProductsApi")
                .build();

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/{id}")
                .methods(singletonList(HttpMethod.GET))
                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                        .handler(getProductFunction)
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/")
                .methods(singletonList(HttpMethod.GET))
                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                        .handler(getallProductFunction)
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/{id}")
                .methods(singletonList(HttpMethod.PUT))
                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                        .handler(putProductFunction)
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/{id}")
                .methods(singletonList(HttpMethod.DELETE))
                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                        .handler(deleteProductFunction)
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()))
                .build());

        CfnOutput apiUrl = CfnOutput.Builder.create(this, "ApiUrl")
                .exportName("ApiUrl")
                .value(httpApi.getApiEndpoint())
                .build();
    }
}
