package main

import (
	"time"
)

type Click struct {
	Id         string    `avro:"id" json:"id" db:"id"`
	StoreId    string    `avro:"storeId" json:"storeId" db:"storeId"`
	CampaignId string    `avro:"campaignId" json:"campaignId" db:"campaignId"`
	Geo        Geo       `avro:"geo" json:"geo" db:"geo"`
	CreatedAt  time.Time `avro:"createdAt" json:"createdAt" db:"createdAt"`
}

type Geo struct {
	City        string `avro:"city" json:"city"`
	CountryCode string `avro:"countryCode" json:"countryCode"`
}
