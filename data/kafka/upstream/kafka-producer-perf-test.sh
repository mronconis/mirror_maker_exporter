#!/bin/bash

TOPIC="test_"${RANDOM:0:2}
PARTITION=10

create_topic () {
  bin/kafka-topics.sh \
    --bootstrap-server upstream:9092 \
    --topic $1 \
    --partitions $2 \
    --create
}

producer_perf_test () {
  bin/kafka-producer-perf-test.sh \
    --producer.config /tmp/client.properties \
    --topic $1 \
    --throughput 10000 \
    --record-size 5000 \
    --num-records 1000000 \
    --producer-props linger.ms=0 \
    --print-metrics
}

create_topic $TOPIC $PARTITION
producer_perf_test $TOPIC

