# Portfolio Backend

![CI/CD](https://github.com/chinhnt-ps/portfolio-be/workflows/CI/CD%20Pipeline%20-%20Backend/badge.svg)

Backend API cho portfolio website sử dụng Java Spring Boot.

## Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Data MongoDB
- Spring Security + JWT
- MongoDB Atlas
- Maven

## Project Structure

```
portfolio-be/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── portfolio/
│       │           ├── PortfolioApplication.java
│       │           ├── config/          # Configuration classes
│       │           ├── controller/     # REST controllers
│       │           ├── service/         # Business logic
│       │           ├── repository/     # Data access layer
│       │           ├── model/           # Entity models
│       │           ├── dto/             # Data transfer objects
│       │           ├── security/        # Security configuration
│       │           └── exception/       # Exception handling
│       └── resources/
│           ├── application.yml
│           └── application-dev.yml
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- **Java**: 21
- **Maven**: 3.8+
- **MongoDB Atlas**: Free tier account

### Setup

1. Clone repository và navigate vào `portfolio-be`:
   ```bash
   cd portfolio-be
   ```

2. Configure MongoDB connection trong `src/main/resources/application.yml`

3. Run application:
   ```bash
   mvn spring-boot:run
   ```

4. API sẽ chạy tại: `http://localhost:8080`

## Environment Variables

Create `.env` file hoặc set environment variables:

```env
MONGODB_URI=mongodb+srv://...
JWT_SECRET=your-secret-key
```

## API Documentation

API documentation sẽ được generate tự động khi chạy app (Swagger/OpenAPI).

## Deployment

Backend sẽ được deploy trên Railway hoặc Render (free tier).

Xem `docs/ai/deployment/README.md` để biết chi tiết.

