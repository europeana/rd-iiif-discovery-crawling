#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

java -Xmx256m -cp classes:$CLASSPATH europeana.rnd.iiif.discovery.demo.EdmAggregationDemonstrator "$@"

