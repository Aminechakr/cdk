from logging import Logger
from os import getenv

import boto3
import logging

client = boto3.client('ecs')
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def stop_handler(event, context):
    print("#### EVENT ####")
    print(event)

    logger.info("Starting restart of {0} service in {1} cluster".format(service_name, cluster))
    response = client.list_tasks(cluster=cluster, desiredStatus='RUNNING')
#   response = client.list_tasks(cluster=cluster, serviceName=service_name, desiredStatus='RUNNING')
    tasks = response.get('TasksArns', [])
    logger.info("Service is running {0} underlying tasks".format(tasks))
    for task in tasks:
        logger.info("Stopping tasks {0}".format(tasks))
        client.stop_task(cluster=cluster, task=task)
    logger.info("Completed stop tasks of {0}".format(tasks))
#   logger.info("Completed stop tasks of {0}"".format(service_name)))
