#!/bin/bash

#   Local Command 
#   ./start.sh to start the script 
#   Make sure to do it from the root directory,  

#   Killing app service  
npx kill-port 8080


#   Building app - Maven
echo "Building app with maven..."
cd app
./mvnw spotless:apply
./mvnw clean install
cd ..
echo "Successful Build of app with maven"

# Stating the quiz app - Maven
echo "Starting quiz app ..."
env $(cat local-development/.env.local | grep -v "^#" | xargs) java -jar app/target/app-0.0.1-SNAPSHOT.jar