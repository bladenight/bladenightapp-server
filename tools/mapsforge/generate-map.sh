#!/bin/sh

minlat=48.0
minlon=11.4
maxlat=48.3
maxlon=11.8

export PATH=$PATH:/Volumes/DD500/ocroquette/bin/osmosis-0.42/bin
if [ ! -f oberbayern-latest.osm.pbf ] ; then
# wget http://download.geofabrik.de/osm/europe/germany/bayern/oberbayern.osm.bz2 || exit 1
wget http://download.geofabrik.de/europe/germany/bayern/oberbayern-latest.osm.pbf || exit 1
fi

if [ ! -f munich.osm ] ; then
time ( osmosis --rb oberbayern-latest.osm.pbf --bb left=$minlon bottom=$minlat right=$maxlon top=$maxlat --wx munich.osm ) || exit 1
fi

export JAVACMD_OPTIONS=-Xmx2G
time osmosis \
  --rx file=munich.osm  \
  --mapfile-writer      \
  file=munich.map       \
  map-start-position=48.132453,11.543895  \
  bbox=$minlat,$minlon,$maxlat,$maxlon    \
  tag-conf-file=tag-mapping.xml \
  || exit 1


