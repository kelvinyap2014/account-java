# Account (FinTech) Pet Project
This pet project is to demonstrate the skills of implementation using:
- Java, Spring Boot
- PostgreSQL
- Git/GitHub

# Development

## Java development

### How to Run Different Spring Boot Profiles

#### Run Dev Profile (PostgreSQL):
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Run Test Profile (H2 Database):
```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

# Testing

## Test API with Swagger UI

### 1. Start the Application
```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

### 2. Check and execute the APIs via Swagger UI
The API will be available at `http://localhost:8080/swagger-ui/index.html`.
