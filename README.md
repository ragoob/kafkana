# kafkana
Real time and management  dashboard  for multiple kafka clusters 


# Features
 - Support multiple clusters stored in your browser local-storage 
 - List of brokers with configuration map
 - List of topics with details about replica - ISR etc..
 - List of consumers and consumer groups with details
 - Create / Delete topics
 - Cluster summary topic count,partition count,unde replicated count,preferred replica rercent,broker count etc..
 - Get topic by name with details
 - Get messages from topic with filter by timeStamp start - end with message details 

# Technolgies
 - Backend java with spring-boot framework
 - frontend angular 11
 
 
 # How to run using docker
  - Backend:
    ```
    docker build -t kafkana-api  -f backend/Dockerfile .
    docker run -d -p 8080:8080 kafkana-api
    ```


 - Frontend:
   ```
   docker build -t kafkana-ui  --build-arg configuration=production -f frontend/Dockerfile .
   docker run -d -p 4200:80 kafkana-ui -e API_URL=localhost:8080
   ```

# Using docker-compose with kafka cluster 3 brokers 
  ```
  - old version of docker
  docker-compose up -d
  
  - new docker version 
  docker compose up -d
  ```
   # Configurations
  | Variable       | Type         |Description|
| ------------- |:-------------:| -----:|
| ALLOW_CACHE   | boolean       | use redis to cache topics, kpi ,etch |
| REDIS_HOST    | string        | redis host in case of allow cache |
| REDIS_PORT    | number        |    redis port in case of allow cache  |
| ALLOW_TOPIC_CREATION    | boolean        |    allow creating topics  |
| ALLOW_TOPIC_DELETION    | boolean       |    allow deleting topics  |
| KAFKA_POLL_DURATION    | number       |    poll duration in consumer with ms default 3000  |
| KAFKA_CONSUMER_GROUP    |string       |    consumer group name  |
