

# Monitor system 

- The master build is up an running on URL : [@render-url](https://quiz-website-g8d7.onrender.com/) 
- The monitor system to avoid the downtime due to free instance : [@uptimerrobot-url](https://uptimerobot.com/dashboard)
- To avoid the downtime we also created a scheduled task to access the website every 10 mins : [@cron-job-ur](https://console.cron-job.org/jobs)


Frequently used commands .

# Useful Commands and Procedures

Frequently used commands .


### Docker 

*  Build docker without any name and tag  
```shell script
docker build .
```
```shell script
docker build -t quiz-app .
```

*  Returns all images in the docker
```shell script
docker images
```
*  Map port of the docker to external services
```shell script
docker run -p 8080:8080
```

### Zookeeper Start


```shell script
./bin/zkServer.sh start
```
### Kafka Start


```shell script
./bin/kafka-server-start.sh ./config/server.properties
```



## Java Server 

### JinJava Project Start


* Make sure the command in start.sh script is correct as per your config files path
```shell script
env $(cat app/.env | grep -v "^#" | xargs) java -jar app/target/app-0.0.1-SNAPSHOT.jar
```


## Sample .env File
```shell script
# Quiz Website Database credentials
QUIZ_DB_HOST=localhost
QUIZ_DB_PORT=5432
QUIZ_DB_NAME=quiz
QUIZ_DB_USERNAME=postgres
QUIZ_DB_PASSWORD=root
QUIZ_DB_CONFIG_QUERY_STRING=allowPublicKeyRetrieval=true&useSSL=false&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false&createDatabaseIfNotExist=true
```

## TODO 

* Database for storing the resume send information
* creating our own client-id and secret key
* Add category to be added in database and in Java entity class.
* Contact page making the success modal clear after 30 seconds.
* Random questions from the database. 
* About Page: content






## Authors

- [@Shubham Chouksey](https://github.com/ShubhamChouksey123)
