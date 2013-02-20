<!---
JBoss, Home of Professional Open Source
Copyright 2011 Red Hat Inc. and/or its affiliates and other
contributors as indicated by the @author tags. All rights reserved.
See the copyright.txt in the distribution for a full listing of
individual contributors.

This is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of
the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this software; if not, write to the Free
Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
02110-1301 USA, or see the FSF site: http://www.fsf.org.
--->

HA Messaging Sandbox
====================

# 1. Introduction

## 1.1 Purpose

This utility application is a configurable JMS message generator with the purpose of testing and observing load balancing and failover of in messaging cluster.

The application currently allows you to:
 + Generate a variable number of messages
 + Use a variable number of producer threads
 + Use a variable number of consumer threads 
 + Connect to a remote JMS brokers through a variety of Context, ConnectionFactory, Connection, and Session reuse patterns  
 + Report/verify the messages recieved by each consumer

This application is currently targeted towards HornetQ, but an attempt will be made to make it more general purpose for JBoss A-MQ (ActiveMQ), HornetQ, MRG-M (Qpid), etc.

## 1.2 System requirements
 + JBoss EAP 6.0 (may work with JBoss AS 7)
 + Maven 3

## 1.3 Documentation

+ JBoss EAP 6 Administration and Configuration Guide: 
	https://access.redhat.com/knowledge/docs/en-US/JBoss_Enterprise_Application_Platform/6/html-single/Administration_and_Configuration_Guide/index.html
+ HornetQ User Guide (oriented to EAP 5x but still applicable and useful): 
	https://access.redhat.com/knowledge/docs/en-US/JBoss_Enterprise_Application_Platform/5/html-single/HornetQ_User_Guide/index.html

## 1.4 Author

Josh West
Middleware Solutions Architect, Red Hat
shuawest at gmail dot com

If you create additional configs or enhancements please feel free to send a pull request :D

# 2. Usage

## 2.1. Configuring JBoss EAP & HornetQ

Template domain configurations are included in the `ha-messaging-sandbox/etc/<scenario>/` directories. Copy the template `domain.xml` and `host.xml` files to your `EAP_HOME/domain/configuration` directory. 

To configure nodes across multiple hosts the host.xml files will need to be modified. The configurations can also be modified to run in EAP 6 standalone mode as well. Neither is provided as part of this sample - so feel free to contribute them.

### Set the Shared Journal Path

The shared journal needs to have a base path configured that will be available across JVMs or hosts. This configuration uses the "path" construct in the EAP 6 configuration to define the root path for your shared journals. Set the `hornetq.journal.root.dir` path in the `EAP_HOME/domain/configuration/host.xml` file to the full path of the desired location of the shared journals.

	<host ...>
	    <paths>
		<path name="hornetq.journal.root.dir" path="/home/jowest/dev/servers/eap600ga/jboss-eap-6.0/domain/journals" />
	    </paths>
	    ...
	    <servers>
		<server name="hornetq1" ...>
		    <paths>
			<path name="hornetq.journal.active.dir" path="journalA" relative-to="hornetq.journal.root.dir" />
			<path name="hornetq.journal.backup.dir" path="journalB" relative-to="hornetq.journal.root.dir" />
		    </paths>
		    ...
		</server>
		<server name="hornetq2" ...>
		    <paths>
			<path name="hornetq.journal.active.dir" path="journalB" relative-to="hornetq.journal.root.dir" />
			<path name="hornetq.journal.backup.dir" path="journalA" relative-to="hornetq.journal.root.dir" />
		    </paths>
		    ...
		</server>
	    </servers>
	</host>

### Generate a JMS / HornetQ user

An application user is required for the JMS/HornetQ authentication. You will need to make sure the username/password match in your `/etc/<profile>.properties` files if your credentials differ from the defaults listed below. 

Execute the `EAP_HOME/bin/add-user.sh` script to create a JMS user. Select:
 + User type: b) Application User
 + Realm (ApplicationRealm) : ApplicationRealm
 + Username: jmsuser
 + Password: jboss
 + Re-enter Password: jboss
 + Roles: guest
 + Confirm? : yes
 + Domain? : yes

### Generate a JBoss Management User

JBoss does not give you a default administrator account to log into the management console with. An admin user is not required for this applications purposes but is good to have for watching the JMS destination activity.

Execute the `EAP_HOME/bin/add-user.sh` script to create a JMS user. Select:
 + User type: a) Management User
 + Realm (ManagementRealm) : ManagementRealm
 + Username: admin
 + Password: jboss
 + Re-enter Password: jboss
 + Confirm? : yes
 + Domain? : yes

### Start the JBoss Server

	`EAP_HOME/bin/domain.sh`

## 2.2. Running the Generator

A variety of application profiles are in the `ha-messaging-sandbox/etc/` directory. Default properties are in the `ha-messaging-sandbox/src/main/resources/default.properties` file, which are automatically loaded from the classpath if you do not specify [PathToAppProperties].  You will need to update these properties for your environment, and message generation scenario(s).

To execute the generator from the command line, make sure `java` is in your path.

	cd <project_path>/ha-messaging-sandbox
	java -cp target/ha-messaging-sandbox-1.0-jar-with-dependencies.jar com.redhat.salab.messaging.Main [PathToAppProperties]

# 3. Building

## 3.1 Configure Maven

Configure your Maven repository to pull from the JBoss repository. Add the following profile to your `~/.m2/settings.xml` file. Add the profile your to active profile list, or activate it with `mvn -Prhmaven ...` when executing maven.   

	<settings ...>
		...
		<profiles>
			<profile>
				<id>rhmaven</id>
				<activation />			
				<properties>
				  	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
				  	<project.reporting.sourceEncoding>UTF-8</project.reporting.sourceEncoding>			 	
				  	<maven.compiler.source>1.6</maven.compiler.source>
				  	<maven.compiler.target>1.6</maven.compiler.target>
	  			</properties>  			
				<repositories>
					<repository>
						<id>redhat-maven-repo</id>
						<url>http://maven.repository.redhat.com/techpreview/all</url>
						<releases><enabled>true</enabled></releases>
						<snapshots><enabled>false</enabled></snapshots>
					</repository>
				</repositories>
				<pluginRepositories>
					<pluginRepository>
						<id>redhat-maven-plugin-repository</id>
						<url>http://maven.repository.redhat.com/techpreview/all</url>
						<releases><enabled>true</enabled></releases>
						<snapshots><enabled>false</enabled></snapshots>
					</pluginRepository>
				</pluginRepositories>
			</profile>
		<profiles>
		<activeProfiles>
			<activeProfile>rhmaven</activeProfile>
		</activeProfiles>
		...
	</settings>	
	
## 3.2. Building and Executing the Application with Maven

Build the application with maven: 

	mvn clean package

Execute the already built application with maven: 

	mvn exec:java [PathToAppProperties]

Build and execute the application with maven:

	mvn clean package [PathToAppProperties]

A variety of application profiles are in the `ha-messaging-sandbox/etc/` directory. Default properties are in the `ha-messaging-sandbox/src/main/resources/default.properties` file, which are automatically loaded from the classpath if you do not specify [PathToAppProperties].  You will need to update these properties for your environment, and message generation scenario.

# Change Log

2013-02-20 Initial creation

