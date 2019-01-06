FROM jenkinsci/blueocean

MAINTAINER Nest.yt

USER root

#get rid of admin password setup
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV JENKINS_UC_DOWNLOAD="http://ftp-nyc.osuosl.org/pub/jenkins"

#automatically installing all plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY init-nester.groovy /var/jenkins_home/init.groovy.d/init-nester.groovy
COPY Local-CI /var/jenkins_home/jobs/Local-CI

RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt && \
    apk add --update \
    python \
    python-dev \
    py-pip \
    build-base \
    rsync \
    sudo \
    unzip \
  && pip install --upgrade pip \
  && rm -rf /var/cache/apk/* \
  && mkdir /var/app