package main

import (
	"archive/zip"
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"io"

	"github.com/justtrackio/gosoline/pkg/application"
	"github.com/justtrackio/gosoline/pkg/cfg"
	"github.com/justtrackio/gosoline/pkg/kernel"
	"github.com/justtrackio/gosoline/pkg/log"
	_ "github.com/marcboeker/go-duckdb/v2"
	"github.com/twmb/franz-go/pkg/kgo"
)

type ClicksIn struct {
	Id             string `json:"id"`
	StoreId        string `json:"store_id"`
	CampaignId     int    `json:"campaign_id"`
	GeoCity        string `json:"geo_city"`
	GeoCountryCode string `json:"geo_country_code"`
	CreatedAt      string `json:"created_at"`
}

type ClicksOut struct {
	Id         string `json:"id"`
	StoreId    string `json:"storeId"`
	CampaignId int    `json:"campaignId"`
	Geo        Geo    `json:"geo"`
	CreatedAt  string `json:"createdAt"`
}

type Geo struct {
	City        string `json:"city"`
	CountryCode string `json:"countryCode"`
}

func main() {
	application.RunFunc(func(ctx context.Context, config cfg.Config, logger log.Logger) (kernel.ModuleRunFunc, error) {
		return func(ctx context.Context) error {
			var err error
			var client *kgo.Client
			var clicks []ClicksIn
			var bytes []byte

			if client, err = kgo.NewClient(kgo.SeedBrokers("localhost:19092"), kgo.AllowAutoTopicCreation()); err != nil {
				return fmt.Errorf("kgo.NewClient(): %w", err)
			}
			defer client.Close()

			if clicks, err = ReadClicksFromJSON("clicks.json.zip"); err != nil {
				return fmt.Errorf("ReadClicksFromJSON(): %w", err)
			}

			for _, click := range clicks {
				out := ClicksOut{
					Id:         click.Id,
					StoreId:    click.StoreId,
					CampaignId: click.CampaignId,
					Geo: Geo{
						City:        click.GeoCity,
						CountryCode: click.GeoCountryCode,
					},
					CreatedAt: click.CreatedAt,
				}

				if bytes, err = json.Marshal(out); err != nil {
					return fmt.Errorf("json.Marshal(): %w", err)
				}

				record := &kgo.Record{Topic: "clicks", Value: bytes}
				if err = client.ProduceSync(ctx, record).FirstErr(); err != nil {
					return fmt.Errorf("client.ProduceSync(): %w", err)
				}
			}

			return nil
		}, nil
	})
}

func ReadClicksFromJSON(path string) ([]ClicksIn, error) {
	var err error
	var zipReader *zip.ReadCloser
	var zippedFile io.ReadCloser

	// Check if the file is a zip file
	if zipReader, err = zip.OpenReader(path); err != nil {
		return nil, fmt.Errorf("zip.OpenReader(): %w", err)
	}

	if len(zipReader.File) == 0 {
		return nil, fmt.Errorf("zip file is empty")
	}

	if zippedFile, err = zipReader.File[0].Open(); err != nil {
		return nil, fmt.Errorf("zip.File.Open(): %w", err)
	}
	defer zippedFile.Close()

	return readClicksFromReader(zippedFile)
}

// Helper to read ClicksIn from an io.Reader (jsonl)
func readClicksFromReader(r io.Reader) ([]ClicksIn, error) {
	scanner := bufio.NewScanner(r)
	var clicks []ClicksIn

	for scanner.Scan() {
		click := ClicksIn{}
		if err := json.Unmarshal(scanner.Bytes(), &click); err != nil {
			return nil, fmt.Errorf("json.Unmarshal(): %w", err)
		}

		clicks = append(clicks, click)
	}

	if err := scanner.Err(); err != nil {
		return nil, fmt.Errorf("scanner.Err(): %w", err)
	}

	return clicks, nil
}
