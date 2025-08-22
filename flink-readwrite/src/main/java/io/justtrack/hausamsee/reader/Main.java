package io.justtrack.hausamsee.reader;

import io.justtrack.hausamsee.Catalog;
import io.justtrack.hausamsee.Click;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;


import static org.apache.flink.table.api.Expressions.*;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Catalog.create(tableEnv);

        Table result = tableEnv.from("hausamsee.main.clicks").select($("*")).limit(10);

        DataStream<Click> stream = tableEnv.toDataStream(result, Click.class);
        stream.print();

        env.execute();
    }
}