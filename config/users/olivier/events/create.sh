#!/bin/bash

set -e
 
DATE=date
FORMAT="%Y-%m-%d"
start=`$DATE +$FORMAT -d "2023-05-01"`
end=`$DATE +$FORMAT -d "2023-09-11"`
now=$start
while [[ "$now" < "$end" ]] ; do
  now=`$DATE +$FORMAT -d "$now + 1 week"`
  echo "$now"
  sed "s/template/$now/" template > $now.per
done

