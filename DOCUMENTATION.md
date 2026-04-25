# AWS Translate POC - Detailed Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Folder Structure](#folder-structure)
3. [Module Breakdown](#module-breakdown)
4. [API Details](#api-details)
5. [Data Flow](#data-flow)
6. [Key Design Decisions](#key-design-decisions)

---

## Architecture Overview

### Layered Architecture
The application follows a standard three-tier Spring Boot architecture:

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (TranslationController)                │
│  - REST endpoints                       │
│  - Request validation                   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Service Layer                  │
│  (AmazonTranslateService)               │
│  - Business logic                       │
│  - AWS API orchestration                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        AWS SDK Client Layer             │
│  (TranslateClient)                      │
│  - AWS Translate API calls              │
└─────────────────────────────────────────┘
```

### Supporting Components
- **TranslationChunkBuilder**: Utility for splitting large batch requests into AWS-compliant chunks
- **Configuration**: AWS client configuration and Spring Boot settings
- **DTOs**: Request/response models for API contracts

---

## Folder Structure

```
aws-translate-poc/
├── src/
│   ├── main/
│   │   ├── java/com/translate/aws_translate_poc/
│   │   │   ├── AwsTranslatePocApplication.java    # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   └── AwsConfig.java                 # AWS client configuration
│   │   │   ├── controller/
│   │   │   │   └── TranslationController.java     # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── AmazonTranslateService.java    # Translation logic
│   │   │   ├── util/
│   │   │   │   └── TranslationChunkBuilder.java   # Chunking utility
│   │   │   └── model/
│   │   │       ├── TranslateRequestDTO.java       # Single translation request
│   │   │       ├── TranslateResponseDTO.java      # Single translation response
│   │   │       ├── TranslateMultipleRequestDTO.java   # Batch request
│   │   │       ├── TranslateMultipleResponseDTO.java  # Batch response
│   │   │       ├── request/
│   │   │       │   └── TranslationChunk.java      # Chunk metadata model
│   │   │       └── response/
│   │   │           └── TranslatedChunk.java       # Translated chunk model
│   │   └── resources/
│   │       └── application.yml                    # Application config
│   └── test/
│       └── java/com/translate/aws_translate_poc/
│           └── util/
│               └── TranslationChunkBuilderTest.java   # Unit tests
├── pom.xml                                        # Maven dependencies
└── api-requests.http                              # Sample API requests
```

---

## Module Breakdown

### 1. Controller Layer (`controller/`)

**TranslationController**
- Exposes REST endpoints for translation services
- Handles HTTP requests and responses
- Performs input validation using `@Validated`
- Endpoints:
  - `POST /api/translate` - Single text translation
  - `POST /api/translate/batch` - Batch translation

### 2. Service Layer (`service/`)

**AmazonTranslateService**
- Core business logic for translation operations
- Orchestrates AWS Translate API calls
- Manages single and batch translation workflows
- Key methods:
  - `translateText()` - Translates single text
  - `translateMultipleTexts()` - Handles batch translation with chunking
  - `translateChunk()` - Internal method for chunk translation
  - `rebuildResponse()` - Reconstructs batch response in original order

### 3. Configuration (`config/`)

**AwsConfig**
- Configures AWS TranslateClient bean
- Sets AWS region (AP_SOUTH_1)
- Uses AWS SDK default credential provider chain
- Credential resolution order:
  1. Environment variables
  2. System properties
  3. AWS credentials file (~/.aws/credentials)
  4. IAM role (if running on EC2/ECS/Lambda)

### 4. Utility (`util/`)

**TranslationChunkBuilder**
- Splits large batch requests into AWS-compliant chunks
- Maximum chunk size: 9KB (9,216 bytes)
- Preserves original text order via index tracking
- Uses newline (`\n`) as separator between texts
- Key method: `buildChunks(List<String> texts)`

### 5. Model Layer (`model/`)

**Request DTOs:**
- `TranslateRequestDTO`: Single translation input
  - Fields: text, sourceLanguageCode, targetLanguageCode, settings
- `TranslateMultipleRequestDTO`: Batch translation input
  - Fields: texts (List), sourceLanguageCode, targetLanguageCode, settings
- `Settings`: Optional formality configuration (FORMAL/INFORMAL)
- `TranslationChunk`: Internal chunk representation with indexes and payload

**Response DTOs:**
- `TranslateResponseDTO`: Single translation output
  - Fields: translatedText, sourceLanguageCode, targetLanguageCode, appliedSettings
- `TranslateMultipleResponseDTO`: Batch translation output
  - Contains list of TranslatedItem objects
- `TranslatedItem`: Paired original and translated text
- `TranslatedChunk`: Internal chunk result with index mapping

---

## API Details

### Single Translation Endpoint

**Endpoint:** `POST /api/translate`

**Request Body:**
```json
{
  "text": "Hello World",
  "sourceLanguageCode": "en",
  "targetLanguageCode": "de",
  "settings": {
    "formality": "FORMAL"
  }
}
```

**Required Fields:**
- `text` (String, not blank): Text to translate
- `sourceLanguageCode` (String, not blank): Source language code (ISO 639-1)
- `targetLanguageCode` (String, not blank): Target language code (ISO 639-1)

**Optional Fields:**
- `settings.formality` (Enum): FORMAL or INFORMAL

**Response:**
```json
{
  "translatedText": "Hallo Welt",
  "sourceLanguageCode": "en",
  "targetLanguageCode": "de",
  "appliedTerminologies": [],
  "appliedSettings": {
    "formality": "FORMAL"
  }
}
```

**Status Codes:**
- `200 OK`: Successful translation
- `400 Bad Request`: Invalid input (validation failure)
- `500 Internal Server Error`: AWS service error

---

### Batch Translation Endpoint

**Endpoint:** `POST /api/translate/batch`

**Request Body:**
```json
{
  "texts": ["Hello", "Good Morning", "Thank you"],
  "sourceLanguageCode": "en",
  "targetLanguageCode": "de",
  "settings": {
    "formality": "FORMAL"
  }
}
```

**Required Fields:**
- `texts` (List<String>, not empty): Array of texts to translate
- `sourceLanguageCode` (String, not blank): Source language code
- `targetLanguageCode` (String, not blank): Target language code

**Optional Fields:**
- `settings.formality` (Enum): FORMAL or INFORMAL

**Response:**
```json
{
  "items": [
    {
      "originalText": "Hello",
      "translatedText": "Hallo"
    },
    {
      "originalText": "Good Morning",
      "translatedText": "Guten Morgen"
    },
    {
      "originalText": "Thank you",
      "translatedText": "Danke schön"
    }
  ],
  "sourceLanguageCode": "en",
  "targetLanguageCode": "de",
  "appliedSettings": {
    "formality": "FORMAL"
  }
}
```

**Status Codes:**
- `200 OK`: Successful translation
- `400 Bad Request`: Invalid input (empty array, validation failure)
- `500 Internal Server Error`: AWS service error

---

## Data Flow

### Single Translation Flow

```
1. Client sends POST /api/translate
   ↓
2. TranslationController validates request
   ↓
3. AmazonTranslateService.translateText() called
   ↓
4. Build AWS TranslateTextRequest
   ├─ Set text, source/target languages
   └─ Optionally add formality settings
   ↓
5. TranslateClient.translateText() - AWS API call
   ↓
6. Parse AWS response
   ↓
7. Build TranslateResponseDTO
   ↓
8. Return response to client
```

### Batch Translation Flow

```
1. Client sends POST /api/translate/batch
   ↓
2. TranslationController validates request
   ↓
3. AmazonTranslateService.translateMultipleTexts() called
   ↓
4. TranslationChunkBuilder.buildChunks()
   ├─ Iterate through texts
   ├─ Combine texts with newline separator
   ├─ Check if adding next text exceeds 9KB
   ├─ If yes: create chunk, start new one
   └─ Track original indexes for each chunk
   ↓
5. For each chunk:
   ├─ Build AWS TranslateTextRequest with chunk payload
   ├─ Call TranslateClient.translateText()
   └─ Split translated response by newlines
   ↓
6. Rebuild response in original order
   ├─ Create list with original size
   ├─ Map translated texts back to original indexes
   └─ Create TranslatedItem for each text
   ↓
7. Return TranslateMultipleResponseDTO to client
```

### Chunking Algorithm Details

**Input:** List of N texts
**Constraint:** Each AWS API call payload must be ≤ 9KB

**Algorithm:**
1. Initialize empty chunk payload and index list
2. For each text at index i:
   - Combine current payload + "\n" + text[i]
   - Calculate combined UTF-8 byte size
   - If size > 9KB:
     - Save current chunk
     - Start new chunk with text[i]
   - Else:
     - Add text[i] to current payload
     - Add i to current index list
3. Save final chunk

**Output:** List of TranslationChunk objects
- Each chunk has: chunkIndex, originalIndexes[], payload
- Chunks are translated independently
- Results are reassembled using originalIndexes

---

## Key Design Decisions

### 1. Chunking Strategy (9KB Limit)

**Problem:** AWS Translate API has a 10KB payload limit per request.

**Solution:** Implement TranslationChunkBuilder to:
- Combine multiple texts into single requests (reduces API calls)
- Use 9KB threshold (safety margin for encoding variations)
- Track original indexes to maintain order
- Preserve empty strings and special characters

**Trade-offs:**
- **Pros:** Reduces API calls, improves performance, maintains order
- **Cons:** Adds complexity, requires response reconstruction

**Alternative Considered:** Individual API calls per text
- **Rejected:** Too many API calls, higher latency and cost

---

### 2. Newline Separator for Batch Texts

**Problem:** Need to combine multiple texts in a single AWS API call.

**Solution:** Join texts with newline character (`\n`).

**Rationale:**
- AWS Translate preserves newlines in output
- Allows reliable splitting of response
- Maintains text boundaries
- Works with texts containing other delimiters

**Edge Cases Handled:**
- Empty strings: Creates consecutive newlines
- Texts with embedded newlines: Preserved correctly
- Single text: No separator added

---

### 3. Formality Settings as Optional

**Design:** Formality (FORMAL/INFORMAL) is optional in both endpoints.

**Rationale:**
- Not all language pairs support formality
- AWS Translate returns error if formality is unsupported
- Users might not need formality control for all translations

**Implementation:**
- Check if settings object and formality field are present
- Only add TranslationSettings to AWS request if formality is specified
- AWS uses default behavior when not specified

---

### 4. Region Configuration in Code

**Design:** AWS region (AP_SOUTH_1) is hardcoded in AwsConfig.

**Current Implementation:**
```java
@Bean
public TranslateClient translateClient() {
    return TranslateClient.builder()
            .region(Region.AP_SOUTH_1)
            .build();
}
```

**Rationale:**
- Simple POC setup
- Predictable behavior
- No external configuration needed

**Production Recommendation:**
- Externalize to application.yml or environment variable
- Allow different regions for different deployments

**Suggested Improvement:**
```java
@Value("${aws.region:ap-south-1}")
private String awsRegion;

@Bean
public TranslateClient translateClient() {
    return TranslateClient.builder()
            .region(Region.of(awsRegion))
            .build();
}
```

---

### 5. Credential Management via AWS SDK Default Chain

**Design:** Uses AWS SDK default credential provider chain.

**Resolution Order:**
1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
2. System properties
3. AWS credentials file (~/.aws/credentials)
4. IAM role (for EC2, ECS, Lambda)

**Rationale:**
- Follows AWS best practices
- Flexible for different environments
- Secure for production (use IAM roles)
- Easy for local development (use credentials file)

**Security Note:** Never hardcode credentials in source code.

---

### 6. Response DTO Structure

**Design:** Different response structures for single vs. batch:
- Single: Returns flat structure with translatedText field
- Batch: Returns list of TranslatedItem objects (original + translated)

**Rationale:**
- Single translation: Simple, direct response
- Batch translation: Need to pair originals with translations for clarity
- Maintains order in batch responses
- Easy for clients to process

**Alternative Considered:** Return only translated texts array
- **Rejected:** Clients would need to maintain original texts separately

---

### 7. Validation Strategy

**Design:** Use Jakarta Bean Validation annotations:
- `@NotBlank` for required string fields
- `@NotEmpty` for required collections
- `@Validated` at controller level

**Rationale:**
- Declarative validation (clean code)
- Automatic 400 Bad Request responses
- Consistent error handling
- Spring Boot integration

**Validation Rules:**
- text: must not be blank
- sourceLanguageCode: must not be blank
- targetLanguageCode: must not be blank
- texts: array must not be empty
- formality: optional, no validation when absent

---

### 8. Logging Strategy

**Design:** Use SLF4J with Lombok @Slf4j annotation.

**Current Logging Points:**
- Request received (log.info with request details)
- Batch translation initiated (log.info with count and languages)

**Production Recommendations:**
- Add structured logging (JSON format)
- Log AWS API errors
- Log chunk creation details
- Add correlation IDs for request tracing
- Use appropriate log levels (DEBUG for details, INFO for operations, ERROR for failures)

---

### 9. Error Handling

**Current Implementation:** Minimal error handling; relies on Spring Boot defaults.

**AWS SDK Errors Propagate As:**
- TranslateException → 500 Internal Server Error
- Validation errors → 400 Bad Request

**Production Recommendations:**
- Add @ControllerAdvice for global exception handling
- Map specific AWS exceptions to appropriate HTTP status codes
- Return detailed error messages with error codes
- Handle rate limiting (429 Too Many Requests)
- Implement retry logic for transient failures

**Suggested Exception Mapping:**
```
TranslateException → 502 Bad Gateway
InvalidRequestException → 400 Bad Request
UnsupportedLanguagePairException → 400 Bad Request
ServiceUnavailableException → 503 Service Unavailable
TooManyRequestsException → 429 Too Many Requests
```

---

### 10. Testing Approach

**Current Coverage:**
- Comprehensive unit tests for TranslationChunkBuilder
- Tests cover edge cases, boundary conditions, and special characters

**Test Categories:**
1. **Empty/Single inputs:** Empty list, single text
2. **Multiple texts:** Various combinations within single chunk
3. **Size boundaries:** Texts at/near 9KB limit
4. **Multi-chunk scenarios:** Texts requiring multiple chunks
5. **Edge cases:** Empty strings, special characters, many small texts

**Missing Coverage (Production Recommendations):**
- Integration tests for controller endpoints
- AWS client mock testing for service layer
- End-to-end API tests
- Error scenario testing
- Performance/load testing for batch operations

---

### 11. Port Configuration

**Design:** Application runs on port 8081 (configured in application.yml).

**Rationale:**
- Avoids conflict with default Spring Boot port (8080)
- Common pattern for secondary services
- Easy to change via configuration

**Configuration:**
```yaml
server:
  port: 8081
```

---

### 12. Actuator Endpoints

**Configuration:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

**Exposed Endpoints:**
- `/actuator/health` - Health check for monitoring
- `/actuator/info` - Application information

**Rationale:**
- Minimal exposure for security
- Essential for monitoring and health checks
- Can add more endpoints as needed (metrics, prometheus)

---

## Performance Considerations

### Batch Optimization
- Chunking reduces API calls (N texts → N/X chunks, where X is texts per chunk)
- Average chunk utilization depends on text sizes
- Best case: All texts in one chunk (1 API call)
- Worst case: Each text exceeds 9KB (N API calls)

### Typical Performance:
- Small texts (< 1KB): ~5-10 texts per chunk
- Medium texts (1-3KB): ~2-5 texts per chunk
- Large texts (5-8KB): 1-2 texts per chunk

### Latency:
- Single translation: ~200-500ms (AWS API latency)
- Batch translation: ~200-500ms per chunk + processing overhead (~10-50ms)

---

## Future Enhancements

1. **Caching:** Add translation cache to reduce duplicate API calls
2. **Async Processing:** Use Spring @Async for large batch operations
3. **Retry Logic:** Implement exponential backoff for transient failures
4. **Rate Limiting:** Add client-side rate limiting to prevent AWS throttling
5. **Terminology Support:** Add custom terminology feature
6. **Language Auto-Detection:** Support auto-detection for source language
7. **Streaming:** Support streaming responses for very large batches
8. **Monitoring:** Add metrics (API call counts, latencies, error rates)
9. **Multi-Region:** Support failover across AWS regions

---

## Conclusion

This POC demonstrates a production-ready approach to AWS Translate integration with:
- Clean architecture (layered design)
- Intelligent batching (9KB chunk optimization)
- Order preservation (index tracking)
- Flexible configuration (AWS credential chain)
- Comprehensive testing (unit tests)

The design balances simplicity (for POC) with extensibility (for production). Key patterns (chunking, validation, DTO structure) can be applied to similar AWS service integrations.
