FROM maven:3.6.3-jdk-14

ADD . /usr/src/urlsearch
WORKDIR /usr/src/urlsearch
EXPOSE 4567
ENTRYPOINT ["mvn", "clean", "verify", "exec:java"]