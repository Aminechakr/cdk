package com.myorg;

import java.util.HashMap;
import java.nio.file.Paths;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.TopiProps;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;

import software.amazon.awscdk.services.cloudwatch.Alarm;
import software.amazon.awscdk.services.cloudwatch.AlarmActionConfig;
import software.amazon.awscdk.services.cloudwatch.Metric;
import software.amazon.awscdk.services.cloudwatch.ComparisionOperator;
import software.amazon.awscdk.services.cloudwatch.IAlarm;
import software.amazon.awscdk.services.cloudwatch.IAlarmAction;
import software.amazon.awscdk.services.cloudwatch.TreatMissingData;

import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.RoleProps;
import software.amazon.awscdk.services.iam.ServicePrincipal;

import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.RetentionDays;

public class CloudWatchCdkStack extends Stack {

    public static class Props implements StacksProps {
        private final String alarmName;
        private final String alarmDescription;
        private final String clusterName;
        private final String namespace;
        private final String metricname;
        // private final String serviceName;
        private final String statistic;
        private final Duration period;
        private final Number threshold;
        private final Number evaluationPeriods;
        private final Number datapointsToAlarm;
        private final ComparisionOperator comparisonOperator;
        private final TreatMissingData treatMissingData;
        private final String lambdaKillerPackagePath;

        public Props(final String namespace, final String metricName,
                // final String serviceName,
                final String clusterName, final String alarmName, final String metricname, final String threshold,
                final Number evaluationPeriods, final Number datapointsToAlarm, final String alarmDescription,
                final Duration period, final ComparaisonOperator comparisonOperator, final String statistic,
                final TreatMissingData treatMissingData, final String lambdaKillerPackagePath) {
            super();
            this.namespace = namespace;
            this.metricname = metricName;
            this.clusterName = clusterName;
            this.alarmName = alarmName;
            this.threshold = threshold;
            this.datapointsToAlarm = datapointsToAlarm;
            this.alarmName = alarmDescription;
            this.period = period;
            this.comparisonOperator = comparisonOperator;
            this.statistic = statistic;
            this.treatMissingData = treatMissingData;
            this.lambdaKillerPackagePath = lambdaKillerPackagePath;
            // this.serviceName = serviceName;
        })

        public CloudWatchCdkStack(final Construct parent, final String id, final Props props) {
            super(parent, id, props);

            Metric metric = Metric.Builder.create().namespace(props.namespace).metricName(props.metricName)
                    .statistic(props.statistic).period(props.period).dimensions(new HashMap<String, Object>() {
                        {
                            put("ClusterName", props.clusterName);
//                          put("ServiceName", props.serviceName);
                        }
                    }).build();

            Alarm alarm = Alarm.Builder.create(this, props.alarmName).metric(metric),actionsEnabled(true)
                    .threshold(props.threshold).evaluationPeriods(props.evaluationPeriods)
                    .datapointsToAlarm(props.datapointsToAlarm).alarmDescription(props.alarmDescription)
                    .comparisonOperator(props.ComparaisonOperator).treatMissingData(props.treatMissingData).build();

            
            final String lambdaPackagePath = Paths.get(props.lambdaKillerPackagePath).toString();
            final String clusterArn = ""; // Get cluster ARN



            final PolicyStatement LambdaECSPolicy = new PolicyStatement(PolicyStatementPorps.builder()
                    .resources(List.of("*")).actions(List.of("ecs:ListTasks", "ecs:StopTask").build()));
            
            final PolicyStatement LambdaLogPolicy = new PolicyStatement(PolicyStatementPorps.builder()
                    .resources(List.of("*")).actions(List.of("logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents").build()));
            
            final Role LambdaKillerRole = new Role(this, "LambdaKillerRole",
                    RoleProps.builder().assumedBy(new ServicePrincipal("lambda.amazonaws.com")).build());
            LambdaKillerRole.addToPolicy(LambdaECSPolicy);
            LambdaKillerRole.addToPolicy(LambdaLogPolicy);

            final Function lambdaEcsCleanUPFunction = new Function(this, "lambdaEcsCleanUPFunction",
                    FunctionProps.builder().role(LambdaKillerRole).code(Code.framAsset(lambdaPackagePath))
                            .handler("main.stop_handler").logRetention(RetentionDays.ONE_MOUNTH).runtime(Runtime.PYTHON_3_6)
                            .memorySize(128).timeout(Duration.minutes(5)).build());

            lambdaEcsCleanUPFunction.addEnvironment("CLUSTER_ARN", clusterArn);

            Topic ecsAlarmSnsTopic = new Topic(this, "ecsAlarmSnsTopic",
                    TopicProps.builder().displayname("ECS CleanUP Topic").build());

            ecsAlarmSnsTopic.addSubscription(new LambdaSubscription(lambdaEcsCleanUPFunction));
            ecsAlarmSnsTopic.addSubscription(new EmailSubscription("mymail@org.com"));

            alarm.addAlarmAction(new IAlarmAction() {

                @Override
                public @NotNull AlarmActionConfig bind(@NotNull Construct scope, @NotNull IAlarm alarm) {
                    return AlarmActionConfig.builder().alarmActionArn(ecsAlarmSnsTopic.getTopicArn().build());
                }
            });

        }

    }
}