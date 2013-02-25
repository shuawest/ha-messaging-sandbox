#!/bin/bash

bin/standalone.sh \
 -b mwjdg1.ny.salab.redhat.com \
 -bmanagement=mwjdg1.ny.salab.redhat.com \
 -c standalone-symmetric-jms-cluster.xml \
 -Djboss.socket.binding.port-offset=0 \
 -Djboss.node.name=Node1 \
 -Djboss.server.base.dir=./standalone1 \ 
 -Djboss.server.config.dir=./standalone1/configuration

