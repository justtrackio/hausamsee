package io.justtrack.hausamsee.ingester;

import io.justtrack.hausamsee.Catalog;
import io.justtrack.hausamsee.Click;
import io.justtrack.hausamsee.ingester.json.DeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Catalog.create(tableEnv);

        env.setParallelism(1);
        env.getCheckpointConfig().setCheckpointInterval(Duration.ofSeconds(10).toMillis());

        KafkaSource<Click> source = KafkaSource.<Click>builder()
                .setBootstrapServers("localhost:19092")
                .setTopics("clicks")
                .setGroupId("hausamsee-ingester")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new DeserializationSchema<>(Click.class))
                .build();

        DataStream<Click> sourceStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        Table sourceTable = tableEnv.fromDataStream(sourceStream);
        sourceTable.insertInto("hausamsee.main.clicks").execute();
    }
}