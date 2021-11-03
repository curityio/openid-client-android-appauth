#!/bin/bash

#####################################################################################
# Provide a working OAuth setup between the mobile app and the Curity Identity Server
#####################################################################################

#
# By default the Curity Identity Server will use a dynamic NGROK URL
# Set USE_NGROK to false if you want to use a localhost or IP address based URL instead
#
USE_NGROK=true
BASE_URL=http://192.168.0.2:8443

#
# First check prerequisites
#
if [ ! -f './license.json' ]; then
  echo 'Please copy a license.json file into the root folder'
  exit 1
fi

#
# Download mobile deployment resources
#
git submodule update --init --remote --rebase
if [ $? -ne 0 ]; then
  echo 'Problem encountered downloading deployment resources'
  exit
fi

#
# Run the deployment script to get an NGROK URL and deploy the Curity Identity Server 
#
cp ./license.json deployment/resources/license.json
./deployment/start.sh "$USE_NGROK" "$BASE_URL" 'appauth'
if [ $? -ne 0 ]; then
  echo 'Problem encountered deploying the Curity Identity Server'
  exit
fi

#
# Inform the user of the Curity Identity Server URL, to be copied to configuration
#
IDENTITY_SERVER_BASE_URL=$(cat './deployment/output.txt')
echo "Curity Identity Server is running at $IDENTITY_SERVER_BASE_URL"

#
# Update the mobile app configuration with the Identity Server URL
#
CONFIG_FILE_PATH='./app/src/main/res/raw/config.json'
AUTHORITY_URL="$IDENTITY_SERVER_BASE_URL/oauth/v2/oauth-anonymous"
MOBILE_CONFIG="$(cat $CONFIG_FILE_PATH)"
echo $MOBILE_CONFIG | jq --arg i "$AUTHORITY_URL" '.issuer = $i' > $CONFIG_FILE_PATH
