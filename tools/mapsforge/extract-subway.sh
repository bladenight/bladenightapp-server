#!/bin/sh

# Extract the UBahn and SBahn station from the OSM file
# You will still have to modify it manually to get something usuable.

TMPFILE=`mktemp -t rail`
export JAVACMD_OPTIONS=-Xmx2G
time osmosis \
  --rx file=munich.osm  \
  --tf accept-nodes railway=station \
  --tf reject-ways \
  --tf reject-relations \
  --write-xml "$TMPFILE"


sed "s/ id='/ id='9999/g" "$TMPFILE" > railway-stations.osm

