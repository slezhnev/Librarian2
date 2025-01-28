#! /bin/sh

docker run --restart unless-stopped -d -p 8081:8081 -p 17001:17001 -p 17002:17002 --name nexus -v /opt/nexus/nexus-data:/nexus-data sonatype/nexus3