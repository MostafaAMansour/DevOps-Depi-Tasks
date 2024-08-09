# Logstash, Elasticsearch, and Kibana (ELK Stack)

## Overview

This repository provides a Docker Compose configuration for setting up an ELK stack consisting of Elasticsearch, Logstash, and Kibana. The stack is used for managing and visualizing log data.

- **Elasticsearch**: A distributed search and analytics engine.
- **Logstash**: A server-side data processing pipeline that ingests data, transforms it, and then sends it to Elasticsearch.
- **Kibana**: A data visualization and exploration tool used to analyze and visualize the data stored in Elasticsearch.

### Docker Compose Configuration

The `docker-compose.yml` file defines the services for Elasticsearch, Logstash, and Kibana:

```yaml
version: '3.7'
services:
  elasticsearch:
    image: elasticsearch:7.16.1
    container_name: es
    environment:
      discovery.type: single-node
      ES_JAVA_OPTS: "-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    healthcheck:
      test: ["CMD-SHELL", "curl --silent --fail localhost:9200/_cluster/health || exit 1"]
      interval: 10s
      timeout: 10s
      retries: 3
    networks:
      - elastic

  logstash:
    image: logstash:7.16.1
    container_name: log
    environment:
      discovery.seed_hosts: logstash
      LS_JAVA_OPTS: "-Xms512m -Xmx512m"
    volumes:
      - ./logstash/pipeline/logstash-nginx.config:/usr/share/logstash/pipeline/logstash-nginx.config
      - ./logstash/nginx.log:/home/nginx.log
    ports:
      - "5000:5000/tcp"
      - "5000:5000/udp"
      - "5044:5044"
      - "9600:9600"
    depends_on:
      - elasticsearch
    networks:
      - elastic
    command: logstash -f /usr/share/logstash/pipeline/logstash-nginx.config

  kibana:
    image: kibana:7.16.1
    container_name: kib
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      - elastic

networks:
  elastic:
    driver: bridge
```

### Logstash Configuration

The Logstash configuration file (logstash-nginx.config) is used to define how Logstash processes incoming data:

```conf
input {
  file {
    path => "/home/nginx.log"
    start_position => "beginning"
  }
}

filter {
  # Example filter configuration
  grok {
    match => { "message" => "%{COMBINEDAPACHELOG}" }
  }
}

output {
  elasticsearch {
    hosts => ["http://es:9200"]
    index => "nginx-logs-%{+YYYY.MM.dd}"
  }
}
```
### Running the ELK Stack

first run the following command

```bash
docker-compose up
```

**Access Kibana**

Open your browser and navigate to http://localhost:5601 to access the Kibana interface and start analyzing your data.

### Notes
`Elasticsearch` is configured to run as a single node for simplicity.
`Logstash` is set up to process logs from a file (nginx.log). You can adjust the Logstash configuration file (logstash-nginx.config) as needed to match your specific log format and processing requirements.
`Kibana` depends on `Elasticsearch` and provides the web interface for visualizing data.