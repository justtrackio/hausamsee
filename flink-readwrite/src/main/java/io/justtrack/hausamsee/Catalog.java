package io.justtrack.hausamsee;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.catalog.CatalogDescriptor;

public class Catalog {
    public static void create(TableEnvironment tableEnv) {
        Configuration conf = new Configuration();
        conf.setString("type", "iceberg");
        conf.setString("catalog-impl", "org.apache.iceberg.nessie.NessieCatalog");
        conf.setString("io-impl", "org.apache.iceberg.aws.s3.S3FileIO");
        conf.setString("uri", "http://localhost:19120/api/v1");
        conf.setString("warehouse", "s3://warehouse");
        conf.setString("s3.endpoint", "http://localhost:9000");
        conf.setString("s3.access-key-id", "admin");
        conf.setString("s3.secret-access-key", "password");
        conf.setString("s3.path-style-access", "true");

        CatalogDescriptor desc = CatalogDescriptor.of("hausamsee", conf);
        tableEnv.createCatalog("hausamsee", desc);
    }
}
