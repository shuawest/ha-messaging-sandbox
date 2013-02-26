#!/bin/bash

bin/standalone.sh \
 -b mwjdg2.ny.salab.redhat.com \
 -bmanagement=mwjdg2.ny.salab.redhat.com \
 -c standalone-symmetric-jms-cluster.xml \
 -Djboss.socket.binding.port-offset=0 \
 -Djboss.node.name=Node2 \
 -Djboss.server.base.dir=./standalone2 \
 -Djboss.server.config.dir=./standalone2/configuration

