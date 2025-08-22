package main

import (
	"errors"
	"time"
)

type Click struct {
	Id         string    `avro:"id" json:"id"`
	StoreId    string    `avro:"storeId" json:"storeId"`
	CampaignId string    `avro:"campaignId" json:"campaignId"`
	Geo        Geo       `avro:"money" json:"money"`
	CreatedAt  ArrowTime `avro:"createdAt" json:"createdAt"`
}

type Geo struct {
	City        string `avro:"city" json:"city"`
	CountryCode string `avro:"countryCode" json:"countryCode"`
}

type ArrowTime struct {
	time.Time
}

func (t *ArrowTime) UnmarshalJSON(data []byte) error {
	if string(data) == "null" {
		return nil
	}
	// TODO(https://go.dev/issue/47353): Properly unescape a JSON string.
	if len(data) < 2 || data[0] != '"' || data[len(data)-1] != '"' {
		return errors.New("Time.UnmarshalJSON: input is not a JSON string")
	}
	data = data[len(`"`) : len(data)-len(`"`)]

	tm, err := time.Parse("2006-01-02 15:04:05.999999999Z07:00", string(data))
	t.Time = tm
	return err
}
