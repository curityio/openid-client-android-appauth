#!/bin/bash

#####################################################################################
# Provide a working OAuth setup between the mobile app and the Curity Identity Server
#####################################################################################

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
cp ./license.json deployment/appauth/license.json
./deployment/appauth/start.sh
if [ $? -ne 0 ]; then
  echo 'Problem encountered deploying the Curity Identity Server'
  exit
fi

#
# Update the mobile app configuration with the Identity Server URL
#
CONFIG_FILE_PATH='./app/src/main/res/raw/config.json'
AUTHORITY_URL=$(cat './deployment/appauth/output.txt')
echo "Curity Identity Server is running at $AUTHORITY_URL"
MOBILE_CONFIG="$(cat $CONFIG_FILE_PATH)"
echo $MOBILE_CONFIG | jq --arg i "$AUTHORITY_URL" '.issuer = $i' > $CONFIG_FILE_PATH
