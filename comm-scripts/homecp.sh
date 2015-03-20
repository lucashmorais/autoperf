#!/bin/bash
tar -cf LoopScript.tar.gz ./bin/
scp -P 2222 ./LoopScript.tar.gz lucasm@143.106.167.233:LoopScript/

