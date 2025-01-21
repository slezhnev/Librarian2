call ./mvnw package || exit /b
podman build -f src/main/docker/Dockerfile.jvm -t quarkus/librarian2-jvm .
podman push --tls-verify=false quarkus/librarian2-jvm docker://192.168.8.4:17001/quarkus/librarian2-jvm:1.0