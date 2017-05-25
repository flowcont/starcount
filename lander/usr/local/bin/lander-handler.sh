#!/bin/sh
cat - | pv --rate-limit 10m >> /data/$(uuidgen).avro
