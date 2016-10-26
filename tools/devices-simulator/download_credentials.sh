#!/usr/bin/env bash

DATA=$(iothub-explorer list --display="deviceId,authentication.SymmetricKey.primaryKey," --raw|sort|awk '{ print $0 ","}')
echo "var connString = '$CONNSTRING';" > credentials.js
echo "var hubDevices = [$DATA];" >> credentials.js
