#!/bin/bash

PORT=8080

exec java -jar -Dserver.port="${PORT}" "payment-reconciliation-consumer-java.jar"