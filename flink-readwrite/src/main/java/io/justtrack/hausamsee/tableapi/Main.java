package io.justtrack.hausamsee.tableapi;

import io.justtrack.hausamsee.Catalog;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Catalog.create(tableEnv);

        tableEnv.sqlQuery("SELECT * FROM hausamsee.main.clicks LIMIT 10")
                .execute()
                .print();
    }
}