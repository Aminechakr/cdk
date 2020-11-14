package com.myorg;

import software.amazon.awscdk.core.app;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator;
import software.amazon.awscdk.services.cloudwatch.TreatMissingData;

public final class CloudwatchCdkApp {

    public static void main{final String[] args) {
        App app = new App();


        new CloudWatchCdkSTack(app, "CloudWatchCdkSTack", 
                new CloudWatchCdkSTack.Props("ECS/ContainerInsights", "TaskCount", 
                        "MyEcsClusterName", "MyAlarmeName", 5.0, 1, 1, "handing-ecs-tasks", Duration.seconds(10800),
                        ComparaisonOperator.GREATED_THAN_OR_EQUAL_TO_THRESHOLD, "Average", TreatMissingData.IGNORE, "cleanupecs/function.zip"));

         app.synth();
    }   
}