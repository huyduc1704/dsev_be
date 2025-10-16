# DSEV_Sport Project

## Overview

DSEV_Sport is a Spring Boot application that provides the backend for an e-commerce platform. It includes services for managing products, categories, users, authentication, and more.

## Project Structure

The project is organized into a modular structure, separating concerns for better maintainability.

- **`src/main/java/com/dsevSport/DSEV_Sport`**: The root package for all Java source code.
  - **`commerce`**: Contains the core e-commerce business logic.
    - **`controller`**: Handles incoming REST API requests and delegates to services.
    - **`dto`**: Data Transfer Objects used for API communication.
    - **`mapper`**: Maps between JPA entities and DTOs.
    - **`model`**: JPA entities that represent the database tables.
    - **`repository`**: Spring Data JPA repositories for database operations.
    - **`service`**: Contains the main business logic and orchestrates repository calls.
  - **`common`**: Contains shared utilities and configurations.
    - **`config`**: Application-wide configurations (e.g., security).
    - **`security`**: JWT and Spring Security related classes for authentication and authorization.
    - **`util`**: Utility classes.
- **`src/main/resources`**: Contains non-Java files.
  - **`application.yml`**: The main configuration file for the Spring Boot application.
  - **`db/migration`**: Contains Flyway database migration scripts (e.g., `V1__init_schema.sql`).
- **`pom.xml`**: The Maven project file, defining dependencies and build settings.
- **`compose.yaml`**: Docker Compose file to set up a PostgreSQL database for local development.

## Getting Started

Follow these instructions to get the project up and running on your local machine.

### Prerequisites

- Java JDK 21
- Apache Maven
- Docker and Docker Compose

### Setup

1.  **Clone the repository:**
    ```sh
    git clone <your-repository-url>
    cd dsev_be
    ```

2.  **Configure Environment Variables:**

    Create a `.env` file in the root of the project by copying the example file:

    ```sh
    cp .env.example .env
    ```
    (On Windows, use `copy .env.example .env`)

    Open the newly created `.env` file and update the values for your local environment, especially the `DB_PASSWORD`.

    ```dotenv
    JWT_SECRET=your_jwt_secret
    DB_URL=jdbc:postgresql://localhost:5432/dsev
    DB_USER=postgres
    DB_PASSWORD=your_postgres_password
    ```

    The application is configured to read these variables from the `.env` file to set up the database connection and JWT secret.

3.  **Start the PostgreSQL Database:**

    Use the provided `compose.yaml` to start a PostgreSQL container.

    ```sh
    docker-compose up -d
    ```

    This command will start a PostgreSQL server on port `5432` with a database named `dsev`. The default credentials are `postgres` / `admin123` as defined in `compose.yaml`. Make sure your `.env` password matches.

4.  **Build the Project:**

    Use the Maven wrapper to build the project and download dependencies.

    ```sh
    ./mvnw clean install
    ```
    (On Windows, use `mvnw.cmd clean install`)

5.  **Run the Application:**

    You can run the application using the Maven Spring Boot plugin:

    ```sh
    ./mvnw spring-boot:run
    ```
    (On Windows, use `mvnw.cmd spring-boot:run`)

    Alternatively, you can run the packaged JAR file:
    ```sh
    java -jar target/DSEV_Sport-0.0.1-SNAPSHOT.jar
    ```

### Database Migrations

This project uses **Flyway** to manage database schema changes.

-   **Applying Migrations**: Migrations are automatically applied when the application starts. The SQL scripts are located in `src/main/resources/db/migration`.
-   **Creating New Migrations**: To create a new migration, add a new SQL file in the `db/migration` directory following the Flyway naming convention: `V<VERSION>__<DESCRIPTION>.sql`. For example: `V3__add_new_table.sql`.
