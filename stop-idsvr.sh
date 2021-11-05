#!/bin/bash

#########################################
# Free deployment resources when required
#########################################

USE_NGROK=true
./deployment/stop.sh "$USE_NGROK" 'appauth'