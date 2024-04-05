
# Quiz Website

## Overview


Welcome to my quiz website repository! This digital platform serves as a showcase of my journey as a software engineer, spotlighting my skills, projects, and experiences. Built with HTML, CSS, and JavaScript for the frontend, and Java with Spring Boot for the backend, it offers visitors profound insights into my expertise and creative endeavors. Leveraging PostgreSQL for database management and Gmail SMTP protocol for seamless communication, it stands as a testament to my commitment to delivering robust and dynamic web solutions.

- The master build is live at: [Quiz-Website](https://quiz-website-g8d7.onrender.com/)
- To ensure continuous uptime, a monitoring system is in place: [Uptime-Robot-URL](https://uptimerobot.com/dashboard)
- Scheduled tasks are configured to access the website every 10 minutes: [Cron-Job-URL](https://console.cron-job.org/jobs)




## Features

- Engage users with a collection of random multiple-choice questions sourced from a database.
- Assess user performance by evaluating quiz submissions and comparing scores with other participants.
- Classify scores as above average or otherwise, providing valuable feedback to users.
- Maintain an updated leader board showcasing top performers in the quiz.

## Getting Started

Embark on your quiz journey by visiting the [Quiz Website](https://quiz-website-g8d7.onrender.com/). Navigate through user-friendly interfaces offering options to start new quizzes, access leader boards, view results, and connect through the contact page.




## Useful Commands and Procedures

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

## License

This project operates under an open-source model and is available under the [MIT License](LICENSE).

## Connect and Collaborate

I'm enthusiastic about exploring new opportunities, collaborations, and engaging discussions. Whether you have a project in mind, an innovative idea to explore, or simply wish to connect and share insights, I'm just an email or phone call away:

- Email: shubhamchouksey1998@gmail.com
- Phone: +91 9479917417

Let's embark on new journeys together and craft innovative solutions. Thank you for visiting my quiz website, and I eagerly anticipate connecting with you!



## Authors

- [@Shubham Chouksey](https://github.com/ShubhamChouksey123)

