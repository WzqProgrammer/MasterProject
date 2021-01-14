package com.atguigu.kafkastream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;

import java.util.Properties;

/**
 * @author 14188
 * @date 2020/11/25 15 :20
 * @description
 */
public class Application {
    public static void main(String[] args) {

        String brokers = "192.168.206.100:9092";
        String zookeepers = "192.168.206.100:2181";

        //定义输入和输出的topic
        String from = "log";
        String to = "recommender";

        //定义kafka streaming的配置
        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "logFilter");
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        settings.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zookeepers);

        StreamsConfig config = new StreamsConfig(settings);

        //拓扑构建器
        TopologyBuilder builder = new TopologyBuilder();

        //定义流处理的拓扑结构
        builder.addSource("SOURCE", from)
                .addProcessor("PROCESS", () -> new LogProcessor(), "SOURCE")
                .addSink("SINK", to, "PROCESS");

        KafkaStreams streams = new KafkaStreams(builder, config);

        streams.start();

        System.out.println("Kafka stream started! >>>>>>>>>>>>>>>>>>>>>>>");
    }
}
