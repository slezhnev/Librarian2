call ./mvnw compile install || exit /b
"%JAVA_HOME%/bin/java.exe" -Dlibrary.storagePath.override="library.storagePath.override.properties" -jar target/quarkus-app/quarkus-run.jar 