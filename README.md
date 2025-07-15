# IntelliMap Smart Service Plug-In

An Appian Smart Service plug-in that provides intelligent data mapping capabilities using Azure OpenAI services.

## Overview

The IntelliMap Smart Service analyzes customer data and intelligently maps it to standardized customer fields using AI-powered natural language processing. It leverages Azure OpenAI to extract key-value pairs from unstructured or semi-structured data and returns mapped data with confidence scores.

## Features

- **Intelligent Data Mapping**: Uses Azure OpenAI to analyze and map customer data
- **Configurable Prompts**: Support for custom mapping instructions
- **Confidence Scoring**: Returns confidence levels for mapping accuracy
- **Robust Error Handling**: Comprehensive exception handling with internationalization
- **Rate Limiting**: Built-in rate limiting to prevent API quota exhaustion
- **Retry Logic**: Automatic retry with exponential backoff
- **Comprehensive Logging**: Detailed logging for debugging and monitoring
- **Proper JSON Parsing**: Uses Jackson library for reliable JSON processing

## Recent Improvements

### 1. JSON Parsing Enhancement
- **Before**: Manual string parsing for JSON responses
- **After**: Integrated Jackson JSON library for robust JSON processing
- **Benefits**: 
  - More reliable JSON parsing
  - Better error handling for malformed JSON
  - Type-safe JSON operations
  - Reduced parsing errors

### 2. Internationalization (i18n)
- **Before**: Hard-coded error messages
- **After**: Resource bundle with internationalized messages
- **Benefits**:
  - Support for multiple languages
  - Centralized message management
  - Easy localization
  - Consistent error messaging

### 3. Configuration Management
- **Before**: Hard-coded values throughout the code
- **After**: Centralized configuration class
- **Benefits**:
  - Easy configuration changes
  - Environment-specific settings
  - Reduced code duplication
  - Better maintainability

### 4. Comprehensive Logging
- **Before**: No logging mechanism
- **After**: Structured logging with different levels
- **Benefits**:
  - Better debugging capabilities
  - Performance monitoring
  - Error tracking
  - Audit trail

### 5. Rate Limiting
- **Before**: No rate limiting
- **After**: Sliding window rate limiting
- **Benefits**:
  - Prevents API quota exhaustion
  - Configurable limits (per minute/hour)
  - Automatic cleanup of expired entries
  - Thread-safe implementation

## Dependencies

### Required JAR Files
1. **Appian Plug-in SDK**: `appian-plug-in-sdk.jar`
2. **Jackson Core**: `jackson-core-2.15.2.jar`
3. **Jackson Databind**: `jackson-databind-2.15.2.jar`
4. **Jackson Annotations**: `jackson-annotations-2.15.2.jar`

### Download Instructions
Download the Jackson JAR files from Maven Central:
- [Jackson Core](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.15.2)
- [Jackson Databind](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.15.2)
- [Jackson Annotations](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.15.2)

Place them in the `src/META-INF/lib/` directory.

## Configuration

### Rate Limiting Settings
```java
// In IntelliMapConfig.java
public static final int MAX_REQUESTS_PER_MINUTE = 60;
public static final int MAX_REQUESTS_PER_HOUR = 1000;
public static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(1000);
```

### Retry Settings
```java
public static final int MAX_RETRIES = 3;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
```

### Logging Settings
```java
public static final boolean ENABLE_DEBUG_LOGGING = false;
public static final String LOGGER_NAME = "com.lcm.intellimapsmartservice";
```

## Input Parameters

### Required
- `customerData` (TypedValue): The data to be mapped
- `azureOpenAIEndpoint` (String): Azure OpenAI service endpoint
- `azureOpenAIKey` (String): API key for Azure OpenAI
- `azureOpenAIDeploymentName` (String): Specific deployment name

### Optional
- `userPrompt` (String): Custom prompt for mapping instructions

## Output Parameters

- `mappedData` (String): JSON-formatted mapped customer data
- `confidenceScore` (Double): Confidence level (0-1) of the mapping

## Usage Example

```java
// In Appian Process Model
IntelliMapSmartService smartService = new IntelliMapSmartService();
smartService.setCustomerData(customerData);
smartService.setAzureOpenAIEndpoint("https://your-resource.openai.azure.com/");
smartService.setAzureOpenAIKey("your-api-key");
smartService.setAzureOpenAIDeploymentName("your-deployment");
smartService.setUserPrompt("Map to standard customer fields");

smartService.run();

String mappedData = smartService.getMappedData();
Double confidence = smartService.getConfidenceScore();
```

## Error Handling

The plug-in provides comprehensive error handling with:
- Input validation
- API error handling
- JSON parsing error recovery
- Rate limit handling
- Retry logic with exponential backoff
- Internationalized error messages

## Logging

The plug-in uses structured logging with different levels:
- **INFO**: General operation information
- **WARNING**: Non-critical issues
- **ERROR**: Error conditions with stack traces
- **DEBUG**: Detailed debugging information (configurable)

## Rate Limiting

The rate limiter implements:
- Sliding window algorithm
- Per-minute and per-hour limits
- Automatic cleanup of expired entries
- Thread-safe operations
- Configurable delays between requests

## Performance Considerations

- **Connection Pooling**: HTTP client with configurable timeouts
- **Rate Limiting**: Prevents API quota exhaustion
- **Retry Logic**: Handles transient failures
- **Memory Management**: Automatic cleanup of rate limiting data
- **JSON Efficiency**: Jackson library for optimal JSON processing

## Security

- API keys are handled securely
- Input validation prevents injection attacks
- Rate limiting prevents abuse
- Error messages don't expose sensitive information

## Deployment

1. Compile the project
2. Download required JAR dependencies
3. Package as Appian plug-in
4. Deploy to Appian environment
5. Configure Azure OpenAI credentials

## Troubleshooting

### Common Issues
1. **Missing JAR files**: Ensure all Jackson JAR files are in the lib directory
2. **Rate limiting errors**: Check Azure OpenAI quota and adjust limits
3. **JSON parsing errors**: Verify Azure OpenAI response format
4. **Timeout errors**: Increase timeout values in configuration

### Debug Mode
Enable debug logging by setting `ENABLE_DEBUG_LOGGING = true` in `IntelliMapConfig.java`.

## Version History

### v1.0.0
- Initial release with basic functionality
- Manual JSON parsing
- Basic error handling

### v1.1.0 (Current)
- Added Jackson JSON library integration
- Implemented internationalization
- Added configuration management
- Added comprehensive logging
- Added rate limiting
- Added retry logic with exponential backoff
- Improved error handling and recovery

## Support

For issues and questions, please refer to:
- Appian documentation
- Azure OpenAI documentation
- Jackson JSON library documentation 