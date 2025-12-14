# AWS Translate POC

A Spring Boot application demonstrating integration with AWS Translate service for text translation.

## Overview

This POC provides REST APIs to translate text using AWS Translate, supporting both single and batch translation requests with optional formality settings.

## Key Features

- **Single Text Translation** - Translate individual text snippets
- **Batch Translation** - Translate multiple texts in a single request (with 9KB chunk optimization)
- **Formality Control** - Optional formal/informal translation settings
- **Language Support** - Supports all AWS Translate language pairs

## Tech Stack

- Java 17
- Spring Boot 3.5.7
- AWS SDK for Java v2
- Maven

## API Endpoints

### Single Translation
```http
POST /api/translate
Content-Type: application/json

{
  "text": "Hello World",
  "sourceLanguageCode": "en",
  "targetLanguageCode": "de",
  "settings": {
    "formality": "FORMAL"
  }
}
```

### Batch Translation
```http
POST /api/translate/batch
Content-Type: application/json

{
  "texts": ["Hello", "Good Morning", "Thank you"],
  "sourceLanguageCode": "en",
  "targetLanguageCode": "de",
  "settings": {
    "formality": "FORMAL"
  }
}
```

## Architecture

```
Controller Layer → Service Layer → AWS Translate Client
                      ↓
                TranslationChunkBuilder (optimizes batch requests)
```

**TranslationChunkBuilder**: splits large batch requests into 9KB chunks to comply with AWS Translate API limits.

## Configuration

Set AWS credentials via environment variables or AWS credentials file:
```bash
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret
AWS_REGION=us-east-1
```

## Running the Application

```bash
./mvnw spring-boot:run
```

The application runs on port `8081`.

## Testing

Run unit tests:
```bash
./mvnw test
```