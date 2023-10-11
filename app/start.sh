#!/bin/bash

#   Local Command 
#   ./start.sh to start the script 
#   Make sure to do it from the root directory,  

#   Killing app service  
npx kill-port 8080


#   Building app - Maven
echo "Building app with maven..."


mvn spotless:apply

mvn clean install 

echo "Successful Build of app with maven"

echo "Starting wallet link app ..."
