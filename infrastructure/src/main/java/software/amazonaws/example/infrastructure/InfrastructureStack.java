// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.infrastructure;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.*;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import java.util.*;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.BundlingOutput.ARCHIVED;

public class InfrastructureStack extends Stack {

    List<Function> functions = new ArrayList<>();

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
                //Use local Dockerfile in infrastructure folder with GraalVM build tools
                .image(DockerImage.fromBuild("."))
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

        Function getAllProductFunction = Function.Builder.create(this, "GetAllProductFunction")
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
        productsTable.grantReadData(getAllProductFunction);
        productsTable.grantWriteData(putProductFunction);
        productsTable.grantWriteData(deleteProductFunction);

        HttpApi httpApi = HttpApi.Builder.create(this, "ProductsApi")
                .apiName("ProductsApi")
                .build();

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/{id}")
                .methods(singletonList(HttpMethod.GET))
                .integration(new HttpLambdaIntegration("HttpApiGatewayGetProductFunction", getProductFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/")
                .methods(singletonList(HttpMethod.GET))
                .integration(new HttpLambdaIntegration("HttpApiGatewayGetAllProductsFunction", getAllProductFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/{id}")
                .methods(singletonList(HttpMethod.PUT))
                .integration(new HttpLambdaIntegration("HttpApiGatewayPutProductFunction", putProductFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/{id}")
                .methods(singletonList(HttpMethod.DELETE))
                .integration(new HttpLambdaIntegration("HttpApiGatewayDeleteProductFunction", deleteProductFunction))
                .build());

        functions.add(getAllProductFunction);
        functions.add(getProductFunction);
        functions.add(putProductFunction);
        functions.add(deleteProductFunction);

        CfnOutput apiUrl = CfnOutput.Builder.create(this, "ApiUrl")
                .exportName("ApiUrl")
                .value(httpApi.getApiEndpoint())
                .build();
    }

    public List<Function> getFunctions() {
        return Collections.unmodifiableList(functions);
    }
}
