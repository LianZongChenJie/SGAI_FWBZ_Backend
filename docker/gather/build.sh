#!/bin/bash
rm -rf sgai-module-gather.jar
cp ../../sgai-module-gather/target/sgai-module-gather.jar ./
docker build --platform linux/amd64 -t sgai-module-gather:1.0.0 .
docker save -o sgai-module-gather-1.0.0-linux-amd64.tar sgai-module-gather:1.0.0
