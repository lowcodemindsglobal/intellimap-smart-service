# IntelliMap Smart Service Plug-In

An Appian Smart Service plug-in that provides intelligent data mapping capabilities using Azure OpenAI services. The plugin includes two smart services:

1. **IntelliMap Smart Service** - General purpose data mapping
2. **Apparel Order Mapper** - Specialized mapping for apparel order data to 106 canonical fields

## Overview

The IntelliMap Smart Service analyzes customer data and intelligently maps it to standardized customer fields using AI-powered natural language processing. It leverages Azure OpenAI to extract key-value pairs from unstructured or semi-structured data and returns mapped data with confidence scores.

## Features

### IntelliMap Smart Service
- **Intelligent Data Mapping**: Uses Azure OpenAI to analyze and map customer data
- **Configurable Prompts**: Support for custom mapping instructions
- **Confidence Scoring**: Returns confidence levels for mapping accuracy
- **Robust Error Handling**: Comprehensive exception handling with internationalization
- **Rate Limiting**: Built-in rate limiting to prevent API quota exhaustion
- **Retry Logic**: Automatic retry with exponential backoff
- **Comprehensive Logging**: Detailed logging for debugging and monitoring
- **Proper JSON Parsing**: Uses Jackson library for reliable JSON processing

### Apparel Order Mapper
- **Specialized Apparel Mapping**: Maps raw order data to 106 standardized apparel-ordering fields
- **Semantic Field Matching**: Intelligent matching of input fields to canonical apparel schema
- **Confidence Scoring**: Individual confidence scores (0-100) for each field mapping
- **Comprehensive Coverage**: Handles all major apparel order data types including:
  - Product information (style, color, material, size)
  - Order details (PO numbers, quantities, pricing)
  - Customer information (departments, regions, shipping)
  - Production details (plants, delivery dates, tolerances)
  - Business metadata (buy programs, seasons, categories)
- **Structured Output**: Returns standardized JSON format with field codes, names, and confidence scores

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

## Usage Examples

### IntelliMap Smart Service
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

### Apparel Order Mapper
```java
// In Appian Process Model
ApparelOrderMapper apparelMapper = new ApparelOrderMapper();
apparelMapper.setInputDictionary(orderData);
apparelMapper.setAzureOpenAIEndpoint("https://your-resource.openai.azure.com/");
apparelMapper.setAzureOpenAIKey("your-api-key");
apparelMapper.setAzureOpenAIDeploymentName("your-deployment");

apparelMapper.run();

String mappedResult = apparelMapper.getMappedResult();
Double overallConfidence = apparelMapper.getOverallConfidence();
```

#### Input Format
The Apparel Order Mapper expects an Appian Dictionary containing raw order data. Example:
```json
{
  "style_number": "STY-001",
  "color_code": "BLK",
  "customer_po": "PO-12345",
  "quantity": "1000",
  "season": "Fall 2024",
  "material_group": "Cotton",
  "ship_to": "Warehouse A",
  "delivery_date": "2024-08-15"
}
```

#### Output Format
The mapper returns a JSON array with mapped fields and confidence scores:
```json
[
  {
    "field_code": "F2",
    "field_name": "Customer Style Number",
    "input_key": "style_number",
    "value": "STY-001",
    "confidence": 95
  },
  {
    "field_code": "F6",
    "field_name": "Color Code",
    "input_key": "color_code",
    "value": "BLK",
    "confidence": 90
  },
  {
    "field_code": "O9",
    "field_name": "Customer Purchase Order",
    "input_key": "customer_po",
    "value": "PO-12345",
    "confidence": 100
  }
]
```

## Apparel Order Mapper - 106 Canonical Fields

The Apparel Order Mapper maps input data to the following 106 standardized fields:

### Product Information (F-series fields)
- **F2, F2_1, F2_2**: Customer Style Number and variants
- **F5**: Style Description
- **F6, F6_1, F6_2, F6_3**: Color Code and variants
- **F7**: Color Description
- **F8**: Master Grid
- **F9**: FG Sizes
- **F13, F13_1**: Unit of Measure (for FG) and variants
- **F14**: Denominator
- **F15**: Product Type
- **F16**: Repeat Style
- **F18**: Material Type
- **F19**: Material Group
- **F20**: External Material Group
- **F21, F21_1**: Product Hierarchy and Customer Product Reference
- **F22**: Product Hierarchy Description
- **F23, F23_1**: SAP Fabric Content Code and Customer FCC
- **F24**: SAP Fabric Content Code Description
- **F25**: Def. Grid Value
- **F26, F26_1**: HS Code and variants
- **F27**: Purchasing Group (for 3rd Party Finished Goods)
- **F28**: Prod. Stor. Location
- **F29, F29_1**: Gender and variants
- **F30**: Product Family
- **F31, F32, F33**: Customer Reference 1, 2, 3
- **F36**: Special Procurement Type
- **F41**: Alternative UOM
- **F42**: Def. Stock Category
- **F44**: Material description
- **F46**: Purch Org
- **F47**: Price Unit
- **F50**: SBU
- **F52**: FG Criteria (Basic Material)

### Order Information (O-series fields)
- **O2, O3**: Buy Month, Buy Year
- **O4**: Buy Sequence
- **O6, O6_1, O6_2, O6_3**: Customer Number (Sold to Party) and variants
- **O8**: Order Type
- **O9, O9_1, O9_2**: Customer Purchase Order and variants
- **O10**: Vendor Purchase Order
- **O11**: FG Order Quantity
- **O12**: Sample Type
- **O16**: Actual Production Plant Allocated
- **O22**: Possible RM InHouse Date
- **O24**: Order Reason
- **O25, O25_1, O25_2**: Purchase Order Type and variants
- **O26**: Sales Office
- **O27**: Sales Group
- **O28**: SO Sales Text
- **O31**: TP Vendor (ThirdParty Orders)
- **O35**: PO LI
- **O36, O37**: PP Month, PP Year
- **O38**: Automation team (Apex/Non-Apex)
- **O39**: Requirement category
- **O40**: SO Short Text
- **O44**: Inquiry #
- **O45**: Usage Indicator
- **O46**: Buy Name
- **O47**: Buy Program
- **O55**: Under Delivery Tolerance

### Customer & Location (C & L-series fields)
- **C1, C1_1, C1_2**: Season and variants
- **C2**: Sales Organization
- **C3**: Storage Location
- **C4**: Distribution Channel
- **C5**: Division
- **C6, C6_1**: Customer Department and variants
- **L1, L1_1, L1_2**: Regions and variants
- **L2, L2_1, L2_2**: Ship to party and variants
- **L3, L3_1**: Ship Mode and variants
- **L4**: GAC
- **L6**: Requested Delivery Date
- **L7**: NDC
- **L8**: Order Delivery Tolerance

### Pricing & Quantity (P & S-series fields)
- **P1, P1_1**: FG Price (FOB) and variants
- **P6**: Standard Price (ThirdParty Orders)
- **P7**: Sizewise PO Pricing (ThirdParty Orders)
- **S1**: QTY
- **S2**: FOB

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