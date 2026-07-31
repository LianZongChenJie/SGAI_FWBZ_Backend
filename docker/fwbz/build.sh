#!/bin/bash
rm -rf sgai-module-fwbz-start.jar
cp ../../sgai-module-fwbz/sgai-module-fwbz-start/target/sgai-module-fwbz-start.jar ./
docker build --platform linux/amd64 -t sgai-module-fwbz:1.0.0 .
docker save -o sgai-module-fwbz-1.0.0-linux-amd64.tar sgai-module-fwbz:1.0.0
