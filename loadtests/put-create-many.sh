#!/bin/bash

CNT_FILES=5000000
for i in $(seq 1 $CNT_FILES); do
	echo "Creating file $i"
	echo $i | curl -T - http://localhost:8080/api/v1/files/$i.txt
done
