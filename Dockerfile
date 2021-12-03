# Import base image
FROM openjdk:8u212-jre

# Create log file directory and set permission
RUN groupadd -r gwas-curation-service && useradd -r --create-home -g gwas-curation-service gwas-curation-service
RUN add-apt-repository universe
RUN apt-get update
RUN apt-get install inetutils-traceroute
RUN if [ ! -d /var/log/gwas/ ];then mkdir /var/log/gwas/;fi
RUN chown -R gwas-curation-service:gwas-curation-service /var/log/gwas

# Move project artifact
ADD target/gwas-curation-service-*.jar /home/gwas-curation-service/
USER gwas-curation-service

# Launch application server
ENTRYPOINT exec $JAVA_HOME/bin/java $XMX $XMS -jar -Dspring.profiles.active=$ENVIRONMENT /home/gwas-curation-service/gwas-curation-service-*.jar