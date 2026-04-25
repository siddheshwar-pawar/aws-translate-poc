# AWS Translate POC

Spring Boot REST API for text translation using AWS Translate service.

## Overview

This proof-of-concept demonstrates AWS Translate integration with support for single and batch translation operations. The batch endpoint intelligently chunks large requests to optimize AWS API usage while preserving translation order.

## Tech Stack

- **Java 17** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **AWS SDK v2.25.51** - AWS Translate client
- **Maven** - Build tool
- **Lombok** - Boilerplate reduction

## Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- AWS Account with Translate service access

### AWS Configuration
Configure credentials via environment variables:
```bash
export AWS_ACCESS_KEY_ID=your_key_id
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=ap-south-1
```

Or use AWS credentials file (`~/.aws/credentials`).

### Build & Run
```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

Application starts on **port 8081**.

## Usage

### Single Translation
```bash
curl -X POST http://localhost:8081/api/translate \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Hello World",
    "sourceLanguageCode": "en",
    "targetLanguageCode": "de"
  }'
```

### Batch Translation
```bash
curl -X POST http://localhost:8081/api/translate/batch \
  -H "Content-Type: application/json" \
  -d '{
    "texts": ["Hello", "Good Morning", "Thank you"],
    "sourceLanguageCode": "en",
    "targetLanguageCode": "de",
    "settings": {"formality": "FORMAL"}
  }'
```

### Testing
```bash
./mvnw test
```

## Documentation

See [DOCUMENTATION.md](DOCUMENTATION.md) for detailed architecture and design information.