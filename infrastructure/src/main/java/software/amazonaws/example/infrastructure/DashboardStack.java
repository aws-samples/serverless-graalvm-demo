// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.cloudwatch.Dashboard;
import software.amazon.awscdk.services.cloudwatch.GraphWidget;
import software.amazon.awscdk.services.cloudwatch.GraphWidgetView;
import software.amazon.awscdk.services.cloudwatch.IMetric;
import software.amazon.awscdk.services.cloudwatch.IWidget;
import software.amazon.awscdk.services.cloudwatch.MathExpression;
import software.amazon.awscdk.services.cloudwatch.MetricOptions;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardStack extends Stack {

    public DashboardStack(final Construct parent, final String id, List<Function> functions) {
        this(parent, id, null, functions);
    }

    public DashboardStack(final Construct parent, final String id, final StackProps props, final List<Function> functions) {
        super(parent, id, props);

        List<IMetric> p50DurationMetrics = functions.stream()
                .map(f -> f.metricDuration(MetricOptions.builder()
                        .label(f.getFunctionName())
                        .period(Duration.minutes(1))
                        .statistic("p50")
                        .build()))
                .collect(Collectors.toList());

        IWidget p50DurationGraph = GraphWidget.Builder.create()
                .title("P50 Duration")
                .left(p50DurationMetrics)
                .view(GraphWidgetView.TIME_SERIES)
                .build();

        List<IMetric> p90DurationMetrics = functions.stream()
                .map(f -> f.metricDuration(MetricOptions.builder()
                        .label(f.getFunctionName())
                        .period(Duration.minutes(1))
                        .statistic("p90")
                        .build()))
                .collect(Collectors.toList());

        IWidget p90DurationGraph = GraphWidget.Builder.create()
                .title("P90 Duration")
                .left(p90DurationMetrics)
                .view(GraphWidgetView.TIME_SERIES)
                .build();

        Function function;
        List<IMetric> errorRates = new ArrayList<>();
        for (int i = 0; i < functions.size(); i++) {
            function = functions.get(i);
            errorRates.add(MathExpression.Builder.create()
                    .expression(String.format("(errors%s / invocations%s) * 100", i, i))
                    .usingMetrics(Map.of("errors" + i, function.metricErrors(),
                            "invocations" + i, function.metricInvocations()))
                    .label(function.getFunctionName() + " Error Rate")
                    .build());
        }

        IWidget errorRateGraph = GraphWidget.Builder.create()
                .title("Error Rates")
                .left(errorRates)
                .view(GraphWidgetView.TIME_SERIES)
                .build();

        List<IMetric> concurrentExecutionsMetrics = functions.stream()
                .map(f -> f.metric("ConcurrentExecutions", MetricOptions.builder()
                        .label(f.getFunctionName())
                        .period(Duration.minutes(1))
                        .statistic("Average")
                        .build()))
                .collect(Collectors.toList());

        IWidget concurrentExecutionsGraph = GraphWidget.Builder.create()
                .title("ConcurrentExecutions")
                .left(concurrentExecutionsMetrics)
                .view(GraphWidgetView.TIME_SERIES)
                .build();

        List<IWidget> widgets = List.of(p90DurationGraph, p50DurationGraph, errorRateGraph, concurrentExecutionsGraph);
        Dashboard dashboard = Dashboard.Builder.create(this, "ProductsDashboard")
                .dashboardName("ProductsDashboard")
                .widgets(Collections.singletonList(widgets))
                .build();
    }
}
