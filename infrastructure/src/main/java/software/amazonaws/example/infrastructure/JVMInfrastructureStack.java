// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.infrastructure;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
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
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.logs.RetentionDays;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

public class JVMInfrastructureStack extends Stack {

    public static final int MEMORY_SIZE = 2048;

    public JVMInfrastructureStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public JVMInfrastructureStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Table productsTable = Table.Builder.create(this, "JVMProducts")
                .tableName("JVMProducts")
                .partitionKey(Attribute.builder()
                        .type(AttributeType.STRING)
                        .name("PK")
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        Map<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put("PRODUCT_TABLE_NAME", productsTable.getTableName());

        Function getProductFunction = Function.Builder.create(this, "GetProductFunction")
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../software/products/target/product.jar"))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayGetProductRequestHandler")
                .memorySize(MEMORY_SIZE)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
                .architecture(Architecture.ARM_64)
                .build();

        Function getallProductFunction = Function.Builder.create(this, "GetAllProductFunction")
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../software/products/target/product.jar"))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayGetAllProductRequestHandler")
                .memorySize(MEMORY_SIZE)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
                .architecture(Architecture.ARM_64)
                .build();

        Function putProductFunction = Function.Builder.create(this, "PutProductFunction")
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../software/products/target/product.jar"))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayPutProductRequestHandler")
                .memorySize(MEMORY_SIZE)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
                .architecture(Architecture.ARM_64)
                .build();

        Function deleteProductFunction = Function.Builder.create(this, "DeleteProductFunction")
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../software/products/target/product.jar"))
                .handler("software.amazonaws.example.product.entrypoints.ApiGatewayDeleteProductRequestHandler")
                .memorySize(MEMORY_SIZE)
                .environment(environmentVariables)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
                .architecture(Architecture.ARM_64)
                .build();

        productsTable.grantReadData(getProductFunction);
        productsTable.grantReadData(getallProductFunction);
        productsTable.grantWriteData(putProductFunction);
        productsTable.grantWriteData(deleteProductFunction);

        HttpApi httpApi = HttpApi.Builder.create(this, "JVMProductsApi")
                .apiName("JVMProductsApi")
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

        CfnOutput apiUrl = CfnOutput.Builder.create(this, "JVMApiUrl")
                .exportName("JVMApiUrl")
                .value(httpApi.getApiEndpoint())
                .build();
    }
}
