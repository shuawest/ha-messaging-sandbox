#!/bin/bash

java -cp ha-messaging-sandbox-1.0-jar-with-dependencies.jar \
 -Dlog4j.configuration=file:./log4j.debug.properties \
 -Djava.net.preferIPv4Stack=true \
 com.redhat.salab.messaging.Main producer.properties


