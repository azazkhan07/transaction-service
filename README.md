# transaction-service

Transaction microservice for PayFlow Fintech Platform.

## Features

- Transfer Money
- Transaction History
- Transaction Status Tracking
- Transaction Reversal
- Filter Transactions By Status
- Filter Transactions By Date Range
- Pagination & Sorting
- OpenFeign Integration
- PostgreSQL Database
- Swagger API Documentation

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- OpenFeign
- Eureka Client
- Lombok
- MapStruct
- Swagger OpenAPI

## APIs

POST /api/v1/transactions/transfer

GET /api/v1/transactions/{referenceId}

GET /api/v1/transactions/status/{referenceId}

GET /api/v1/transactions/wallet/{walletId}

PUT /api/v1/transactions/reverse/{referenceId}

GET /api/v1/transactions

GET /api/v1/transactions/filter/status/{status}

GET /api/v1/transactions/filter/date-range
