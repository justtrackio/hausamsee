CREATE SCHEMA hausamsee.main;

CREATE TABLE hausamsee.main.clicks (
    id VARCHAR,
    storeId VARCHAR,
    campaignId INTEGER,
    geo ROW(
        city VARCHAR,
        countryCode VARCHAR
    ),
    createdAt TIMESTAMP(9) WITH TIME ZONE
)
WITH (
    partitioning = ARRAY['day(createdAt)']
);
