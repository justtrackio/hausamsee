CREATE SCHEMA main;
USE main;

CREATE TABLE clicks (
    id STRING,
    storeId STRING,
    campaignId INTEGER,
    geo STRUCT<
        city: STRING,
        countryCode: STRING
    >,
    createdAt TIMESTAMP
)
USING iceberg
PARTITIONED BY (day(createdAt));


SELECT * FROM main.click;


ALTER TABLE clicks CREATE BRANCH `migration`;
CALL hausamsee.system.fast_forward('main.clicks', 'main', 'migration');
CALL hausamsee.system.rollback_to_snapshot('main.clicks', 6882603185664415922);
CALL hausamsee.system.cherrypick_snapshot('main.clicks', 7384190157042721594);
CALL hausamsee.system.rewrite_data_files(table => 'main.clicks', options => map('rewrite-all', 'true', 'remove-dangling-deletes', 'true'));
CALL hausamsee.system.remove_orphan_files(table => 'main.clicks', dry_run => true);

CREATE BRANCH dev;

UPDATE `clicks@dev` SET storeId = 2 WHERE storeId = 3;

MERGE INTO `clicks@main` t
USING (SELECT * FROM `clicks@dev`) s
ON t.id = s.id
WHEN MATCHED THEN UPDATE SET *
WHEN NOT MATCHED BY TARGET THEN INSERT *
WHEN NOT MATCHED BY SOURCE THEN DELETE;

DROP BRANCH dev;

MERGE INTO `clicks@main` t
USING (SELECT * FROM `clicks@dev`) s
ON t.id = s.id
WHEN MATCHED THEN UPDATE SET *
WHEN NOT MATCHED BY SOURCE THEN DELETE;