#!/usr/bin/env bash
brew services start nginx
#sleep 8s
sleep 8
sh /usr/local/zookeeper-3.4.10/bin/zkServer.sh start
sleep 8
sh /usr/local/kafka_2.11-1.1.0/bin/kafka-server-start.sh /usr/local/kafka_2.11-1.1.0/config/server.properties