// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class StandardOutLambdaLogger implements LambdaLogger {
    @Override
    public void log(String s) {
        System.out.println(s);
    }

    @Override
    public void log(byte[] bytes) {
        System.out.println(bytes);
    }
}
