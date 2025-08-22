package main

import (
	"fmt"
	"log"

	"github.com/jmoiron/sqlx"
	_ "github.com/trinodb/trino-go-client/trino"
)

func main() {
	var err error
	var db *sqlx.DB
	var rows *sqlx.Rows

	dsn := "http://jan@localhost:8010?catalog=hausamsee&schema=main"

	if db, err = sqlx.Open("trino", dsn); err != nil {
		log.Fatal(err)
	}

	if rows, err = db.Queryx("SELECT * FROM clicks"); err != nil {
		log.Fatal(err)
	}

	for rows.Next() {
		dest := make(map[string]interface{})
		if err = rows.MapScan(dest); err != nil {
			log.Fatal(err)
		}

		fmt.Println(dest)
	}
}
