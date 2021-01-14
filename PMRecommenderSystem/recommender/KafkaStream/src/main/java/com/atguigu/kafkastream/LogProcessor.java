package com.atguigu.kafkastream;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * @author 14188
 * @date 2020/11/25 15 :37
 * @description
 */
public class LogProcessor implements Processor<byte[], byte[]> {
    private ProcessorContext content;

    @Override
    public void init(ProcessorContext processorContext) {
        this.content = processorContext;
    }

    @Override
    public void process(byte[] dummy, byte[] line) {
        //将收集到的日志用String表示
        String input = new String(line);
        //根据前缀MOVIE_RATING_PREFIX: 从日志中提取评分数据
        if(input.contains("MOVIE_RATING_PREFIX:") ){
            System.out.println("movie rating data coming!>>>>>>>>>>>>>" + input);

            input = input.split("MOVIE_RATING_PREFIX:")[1].trim();
            content.forward("logProcessor".getBytes(), input.getBytes());
        }

    }

    @Override
    public void punctuate(long l) {

    }

    @Override
    public void close() {

    }
}
