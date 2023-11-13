
# Useful Commands and Procedures

Frequently used commands .


## Docker 

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
java -cp "target/wallet-link-0.0.1-SNAPSHOT-app/wallet-link-0.0.1-SNAPSHOT.jar:../../plugins/default-event-user/target/default-event-0.0.1-SNAPSHOT-shaded.jar:../../plugins/sms/twilio/target/twilio-0.0.1-SNAPSHOT-shaded.jar:../../plugins/email/gmail-email-service/target/gmail-email-service-0.0.1-SNAPSHOT-shaded.jar"  com.jinjava.wallet.WalletLinkApplication  --spring.config.location=classpath:application.properties,../../config.yml
```


## Authors

- [@Shubham Chouksey](https://github.com/ShubhamChouksey123)
