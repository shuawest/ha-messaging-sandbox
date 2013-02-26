#!/bin/bash

java -cp ha-messaging-sandbox-1.0-jar-with-dependencies.jar:hornetq-core.jar \
 -Dlog4j.configuration=file:./log4j.debug.properties \
 -Djava.net.preferIPv4Stack=true \
 -Dorg.jboss.byteman.verbose -javaagent:$BYTEMAN_HOME/lib/byteman.jar=script:$BYTEMAN_SCRIPTS/hornetq.btm \
 com.redhat.salab.messaging.Main producer.properties


