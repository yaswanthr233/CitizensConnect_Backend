# CitizensConnect Spring Boot Backend

A complete production-ready RESTful backend built with Spring Boot, Spring Security (JWT), Spring Data JPA, and MySQL.

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL Server

## Configuration

1. Make sure you have a local MySQL running at `localhost:3306`.
2. Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=password
   ```

## Running the Application

You can run the application directly from the command line using Maven:

```bash
mvn spring-boot:run
```

Or you can package and run the JAR:
```bash
mvn clean package
java -jar target/backend-1.0.0.jar
```

## API Documentation
The API documentation is powered by Swagger OpenAPI.
Once the server is running on port 8080, navigate your browser to:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

You can click on the **Authorize** button and input the JWT token (value starts with `Bearer xxxx...`) to test protected endpoints.

## Directory Structure
- `com.citizensconnect.controllers`: REST endpoints
- `com.citizensconnect.service`: Business logic
- `com.citizensconnect.repository`: Spring Data JPA interfaces
- `com.citizensconnect.models`: JPA Entities and Enums
- `com.citizensconnect.payload.*`: DTOs for Request/Response
- `com.citizensconnect.security`: JWT validation, filtering, and SecurityConfig
- `com.citizensconnect.advice`: Global Exception handling
- `com.citizensconnect.config`: Swagger and CORS configurations
