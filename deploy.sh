# push to https://hub.docker.com/r/krisraich/simplea/
mvn clean package && docker build -f src/main/docker/Dockerfile.jvm -t krisraich/simplea . && docker push krisraich/simplea:latest
