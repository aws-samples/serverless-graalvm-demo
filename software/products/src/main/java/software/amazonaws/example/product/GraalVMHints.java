package software.amazonaws.example.product;

import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.amazonaws.services.lambda.runtime.LambdaRuntimeInternal;
import com.amazonaws.services.lambda.runtime.api.client.runtimeapi.InvocationRequest;
import com.amazonaws.services.lambda.runtime.api.client.runtimeapi.LambdaRuntimeClientException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.goodforgod.graalvm.hint.annotation.JniHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.annotation.ResourceHint;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.SimpleLog;
import software.amazonaws.example.product.entrypoints.ApiGatewayDeleteProductRequestHandler;
import software.amazonaws.example.product.entrypoints.ApiGatewayGetAllProductRequestHandler;
import software.amazonaws.example.product.entrypoints.ApiGatewayGetProductRequestHandler;
import software.amazonaws.example.product.entrypoints.ApiGatewayPutProductRequestHandler;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.model.Products;

@ReflectionHint(value = {ReflectionHint.AccessType.ALL_DECLARED, ReflectionHint.AccessType.ALL_PUBLIC},
        types = {
                ApiGatewayGetProductRequestHandler.class,
                ApiGatewayGetAllProductRequestHandler.class,
                ApiGatewayPutProductRequestHandler.class,
                ApiGatewayDeleteProductRequestHandler.class,
                Product.class,
                Products.class,
                LogFactoryImpl.class,
                LogFactory.class,
                SimpleLog.class,
                APIGatewayV2HTTPEvent.class,
                APIGatewayV2HTTPEvent.RequestContext.class,
                APIGatewayV2HTTPEvent.RequestContext.Http.class,
                APIGatewayV2HTTPEvent.RequestContext.Authorizer.class,
                APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT.class,
                APIGatewayV2HTTPEvent.RequestContext.RequestContextBuilder.class,
                APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class,
                APIGatewayV2HTTPEvent.RequestContext.IAM.class,
                APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class,
                APIGatewayV2HTTPEvent.APIGatewayV2HTTPEventBuilder.class,
                APIGatewayV2HTTPResponse.class,
        })
@ReflectionHint(value = ReflectionHint.AccessType.ALL_DECLARED_FIELDS,
        types = {
                LambdaRuntime.class,
                LambdaRuntimeInternal.class,
                InvocationRequest.class,
        })
@ResourceHint(include = {
        "\\Qaarch64/aws-lambda-runtime-interface-client.glibc.so\\E",
        "\\Qaarch64/aws-lambda-runtime-interface-client.musl.so\\E",
        "\\Qx86_64/aws-lambda-runtime-interface-client.glibc.so\\E",
        "\\Qx86_64/aws-lambda-runtime-interface-client.musl.so\\E",
})
@JniHint(types = InvocationRequest.class,
        value = {JniHint.AccessType.ALL_DECLARED_FIELDS, JniHint.AccessType.ALL_PUBLIC_METHODS})
@JniHint(types = LambdaRuntimeClientException.class,
        value = JniHint.AccessType.ALL_DECLARED_METHODS)
final class GraalVMHints {

    private GraalVMHints() { }
}
