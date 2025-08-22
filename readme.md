

# Start docker compose environment
```bash
docker compose up -d
```

# Endpoints
- Trino: http://localhost:8010 
  - just use any username for login
- Nessie: http://localhost:19120
- MinIO: http://localhost:9000
  - Access Key: admin
  - Secret Key: password
- Spark Master: http://localhost:8080
- Spark History: http://localhost:18080

# Prepare data
Run the golang application in the `prepare` directory to generate a bunch of click events in the redpanda cluster.
```bash
cd prepare
go run main.go
```

# Spark
### log in to the master node
```bash
docker exec -it hausamsee_spark_master /bin/bash
```
### start Spark SQL client shell
```bash
docker exec -it hausamsee_spark_master spark-sql \
--packages org.apache.iceberg:iceberg-spark-runtime-3.5_2.12:1.9.1,org.apache.iceberg:iceberg-aws-bundle:1.9.1,org.projectnessie.nessie-integrations:nessie-spark-extensions-3.5_2.12:0.103.3 \
--conf spark.sql.extensions="org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions,org.projectnessie.spark.extensions.NessieSparkSessionExtensions" \
--conf spark.sql.defaultCatalog=hausamsee \
--conf spark.sql.catalog.hausamsee=org.apache.iceberg.spark.SparkCatalog \
--conf spark.sql.catalog.hausamsee.catalog-impl=org.apache.iceberg.nessie.NessieCatalog \
--conf spark.sql.catalog.hausamsee.uri=http://catalog:19120/api/v1 \
--conf spark.sql.catalog.hausamsee.ref=main \
--conf spark.sql.catalog.hausamsee.authentication.type=NONE \
--conf spark.sql.catalog.hausamsee.io-impl=org.apache.iceberg.aws.s3.S3FileIO \
--conf spark.sql.catalog.hausamsee.s3.endpoint=http://storage:9000 \
--conf spark.sql.catalog.hausamsee.s3.path-style-access=true \
--conf spark.sql.catalog.hausamsee.client.region=eu-central-1 \
--conf spark.sql.catalog.hausamsee.s3.access-key-id=admin \
--conf spark.sql.catalog.hausamsee.s3.secret-access-key=password \
--conf spark.sql.catalog.hausamsee.warehouse=s3://warehouse
```

# Nessie GC
```bash
java -jar nessie-gc-0.104.2.jar create-sql-schema \
--jdbc \
--jdbc-url jdbc:postgresql://localhost:5432/metastore_db \
--jdbc-user your_username \
--jdbc-password your_password
```

```bash
java -jar nessie-gc-0.104.2.jar gc \
  --uri http://localhost:19120/api/v2 \
  --jdbc \
  --jdbc-url jdbc:postgresql://localhost:5432/metastore_db \
  --jdbc-user your_username \
  --jdbc-password your_password \
  --iceberg "s3.access-key-id=admin,s3.secret-access-key=password,s3.endpoint=http://localhost:9000,s3.path-style-access=true" \
  --default-cutoff 1
```

```bash
java -jar nessie-gc-0.104.2.jar list \
  --jdbc \
  --jdbc-url jdbc:postgresql://localhost:5432/metastore_db \
  --jdbc-user your_username \
  --jdbc-password your_password \
  --iceberg "s3.access-key-id=admin,s3.secret-access-key=password,s3.endpoint=http://localhost:9000,s3.path-style-access=true"
```
