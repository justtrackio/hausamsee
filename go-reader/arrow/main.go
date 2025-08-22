package main

import (
	"context"
	"encoding/json"
	"fmt"
	"iter"
	"log"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/iceberg-go/catalog"
	"github.com/apache/iceberg-go/catalog/rest"
	"github.com/apache/iceberg-go/table"
)

func main() {
	var err error
	var cat catalog.Catalog
	var tbl *table.Table
	var records iter.Seq2[arrow.Record, error]
	var bytes []byte

	ctx := context.Background()
	props := map[string]string{
		"s3.endpoint":          "http://localhost:9000",
		"s3.path-style-access": "true",
		"s3.access-key-id":     "admin",
		"s3.secret-access-key": "password",
	}

	if cat, err = rest.NewCatalog(ctx, "rest", "http://localhost:19120/iceberg", rest.WithAdditionalProps(props)); err != nil {
		log.Fatal(err)
	}

	if tbl, err = cat.LoadTable(ctx, catalog.ToIdentifier("main.clicks"), props); err != nil {
		log.Fatal(err)
	}

	fmt.Print(tbl.Schema())

	tableScan := tbl.Scan()
	//tableScan := tbl.Scan(table.WithRowFilter(iceberg.EqualTo(iceberg.Reference("id"), "df634877-ff5e-49c7-97dc-03076fb33e4e")))

	if _, records, err = tableScan.ToArrowRecords(ctx); err != nil {
		log.Fatal(err)
	}

	for record, err := range records {
		if err != nil {
			log.Fatal(err)
		}

		if bytes, err = record.MarshalJSON(); err != nil {
			log.Fatal(err)
		}

		events := make([]Click, 0)
		if err = json.Unmarshal(bytes, &events); err != nil {
			log.Fatal(err)
		}

		for _, event := range events {
			fmt.Println(event)
		}
	}
}
