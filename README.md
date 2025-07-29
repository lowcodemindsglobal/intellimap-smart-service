# IntelliMap Smart Service Plug-In

[![Version](https://img.shields.io/badge/version-v4.0.0-blue.svg)](https://github.com/your-org/intellimapsmartservice)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Appian](https://img.shields.io/badge/Appian-Compatible-green.svg)](https://appian.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Updated](https://img.shields.io/badge/README-Updated%20for%20Accuracy-green.svg)](#)

An enterprise-grade Appian Smart Service plug-in specialized for **apparel industry data mapping** using Azure OpenAI services. This plugin provides a single specialized smart service, **Apparel Order Mapper**, which transforms unstructured apparel order data into standardized formats with confidence scoring and comprehensive error handling.

> **🏷️ Industry Focus**: This plugin is specifically designed for the **apparel and fashion industry**. It contains only one smart service: `ApparelOrderMapper`, which specializes in mapping apparel order data to standardized industry field formats.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Smart Service](#smart-service)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [API Reference](#api-reference)
- [Architecture](#architecture)
- [Error Handling](#error-handling)
- [Performance & Security](#performance--security)
- [Troubleshooting](#troubleshooting)
- [Deployment Guide](#deployment-guide)
- [Version History](#version-history)
- [Contributing](#contributing)
- [Support](#support)

## 🎯 Overview

The IntelliMap Smart Service leverages Azure OpenAI's advanced natural language processing to intelligently analyze, understand, and map **apparel order data** to standardized industry formats. This specialized plugin provides enterprise-ready data transformation capabilities specifically designed for the **apparel industry** workflow and business processes.

### Why Choose IntelliMap?

- 🤖 **AI-Powered Intelligence**: Utilizes Azure OpenAI for sophisticated data understanding
- 📊 **Confidence Scoring**: Each mapping includes confidence levels for quality assurance
- ⚡ **Enterprise-Ready**: Built-in rate limiting, retry logic, and robust error handling
- 🔧 **Highly Configurable**: Customizable prompts, field mappings, and processing parameters
- 🌍 **Internationalization**: Multi-language support for global deployments
- 📈 **Production-Tested**: Comprehensive logging and monitoring capabilities

## ✨ Key Features

### 🎯 Intelligent Mapping
- **Natural Language Processing**: Advanced AI understanding of data relationships
- **Semantic Field Matching**: Intelligent matching beyond simple keyword matching
- **Contextual Analysis**: Considers data context for more accurate mappings
- **Custom Prompt Support**: Tailor AI instructions for specific use cases

### 🔒 Enterprise Security & Reliability
- **Rate Limiting**: Sliding window algorithm with configurable limits
- **Retry Logic**: Exponential backoff with intelligent failure handling
- **Input Validation**: Comprehensive sanitization and validation
- **Secure Credential Handling**: Safe management of API keys and sensitive data

### 📊 Quality Assurance
- **Confidence Scoring**: Individual confidence ratings (0-100) for each mapping
- **Comprehensive Logging**: Multi-level logging with performance metrics
- **Error Recovery**: Graceful handling of API failures and malformed data
- **Audit Trail**: Complete tracking of all mapping operations

### 🛠️ Developer Experience
- **Jackson JSON Integration**: Robust JSON parsing and generation
- **Internationalization**: Resource bundles for multi-language support
- **Configuration Management**: Centralized, environment-specific settings
- **Extensive Documentation**: Complete API reference and examples

## 🚀 Smart Service

This plugin provides one specialized smart service:

### Apparel Order Mapper

**Purpose**: Specialized mapping for apparel industry order data to canonical business fields.

**Key Capabilities**:
- Maps raw order data to 106+ standardized apparel ordering fields
- Industry-specific semantic understanding
- Comprehensive coverage of apparel business processes
- Support for complex product hierarchies and business rules

**Standardized Field Categories**:
- 🏷️ **Product Information** (F-series): Style, color, material, sizing
- 📦 **Order Details** (O-series): PO numbers, quantities, delivery dates
- 👤 **Customer Data** (C-series): Departments, regions, preferences  
- 📍 **Location Data** (L-series): Shipping, distribution, warehouses
- 💰 **Pricing** (P-series): FOB pricing, cost structures
- 📊 **Quantities** (S-series): Order quantities, tolerances

**Ideal For**:
- Apparel industry order processing
- Supply chain data standardization
- ERP system integrations
- B2B order management

## ⚡ Quick Start

### Prerequisites
- Appian 24.2+ environment
- Azure OpenAI service with deployed model
- Java 8+ runtime
- Required JAR dependencies (see [Installation](#installation))

### Basic Setup

1. **Download and Install**
   ```bash
   # Download the plugin JAR
   wget https://releases.your-domain.com/intellimapsmartservice-v4.0.0.jar
   
   # Install in Appian Admin Console
   # Navigate to Plug-ins → Install Plug-in
   ```

2. **Configure Azure OpenAI**
   ```javascript
   // In Appian Process Model or Interface
   azureEndpoint: "https://your-resource.openai.azure.com/"
   azureApiKey: "your-32-character-api-key"
   deploymentName: "gpt-4-deployment-name"
   apiVersion: "2023-05-15"
   ```

3. **Test Basic Mapping**
   ```javascript
   // Simple Apparel Order Mapper example
   ApparelOrderMapper(
     inputRecords: {
       customer_style: "SS24-POLO-001",
       style_description: "Men's Cotton Polo Shirt",
       color_code: "NVY",
       order_quantity: "2400"
     },
     azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
     azureOpenAIKey: cons!AZURE_OPENAI_KEY,
     azureOpenAIDeploymentName: cons!DEPLOYMENT_NAME,
     azureOpenAIApiVersion: cons!API_VERSION,
     targetFields: {"F2:Customer Style Number", "F5:Style Description"},
     userPrompt: "Map apparel order data to standard fields"
   )
   ```

## 🔧 Installation

### Step 1: Download Dependencies

Download the required JAR files and place them in `src/META-INF/lib/`:

| Dependency | Version | Download Link |
|------------|---------|---------------|
| Jackson Core | 2.15.2 | [Maven Central](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.15.2) |
| Jackson Databind | 2.15.2 | [Maven Central](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.15.2) |
| Jackson Annotations | 2.15.2 | [Maven Central](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.15.2) |

### Step 2: Directory Structure
```
intellimapsmartservice/
├── src/
│   ├── appian-plugin.xml
│   ├── META-INF/
│   │   └── lib/
│   │       ├── jackson-core-2.15.2.jar
│   │       ├── jackson-databind-2.15.2.jar
│   │       └── jackson-annotations-2.15.2.jar
│   └── com/lcm/plugins/intellimapsmartservice/
│       ├── ApparelOrderMapper.java
│       ├── IntelliMapConfig.java
│       └── RateLimiter.java
└── bin/ (compiled classes)
```

### Step 3: Build and Deploy
```bash
# Compile the project
javac -cp "lib/*:appian-plug-in-sdk.jar" src/com/lcm/plugins/intellimapsmartservice/*.java -d bin/

# Package as JAR
jar -cvf intellimapsmartservice-v4.0.0.jar -C bin/ .

# Deploy to Appian (via Admin Console)
```

## ⚙️ Configuration

### Environment Configuration

Create configuration constants in Appian:

```javascript
// Azure OpenAI Configuration
cons!AZURE_OPENAI_ENDPOINT: "https://your-resource.openai.azure.com/"
cons!AZURE_OPENAI_KEY: "your-api-key-here"
cons!AZURE_OPENAI_DEPLOYMENT: "gpt-4-deployment"
cons!AZURE_OPENAI_API_VERSION: "2023-05-15"

// Rate Limiting Configuration
cons!MAX_REQUESTS_PER_MINUTE: 60
cons!MAX_REQUESTS_PER_HOUR: 1000
cons!RATE_LIMIT_DELAY_MS: 1000

// Retry Configuration
cons!MAX_RETRIES: 3
cons!REQUEST_TIMEOUT_SECONDS: 30
cons!ENABLE_DEBUG_LOGGING: false
```

### Advanced Configuration Options

#### Rate Limiting Settings
```java
// In IntelliMapConfig.java
public static final int MAX_REQUESTS_PER_MINUTE = 60;
public static final int MAX_REQUESTS_PER_HOUR = 1000;
public static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(1000);
public static final boolean ENABLE_RATE_LIMITING = true;
```

#### Retry and Timeout Settings
```java
public static final int MAX_RETRIES = 3;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
public static final Duration RETRY_BASE_DELAY = Duration.ofSeconds(1);
public static final double RETRY_MULTIPLIER = 2.0;
```

#### Logging Configuration
```java
public static final boolean ENABLE_DEBUG_LOGGING = false;
public static final boolean ENABLE_PERFORMANCE_LOGGING = true;
public static final String LOGGER_NAME = "com.lcm.intellimapsmartservice";
```

## 📝 Usage Examples

### Apparel Order Mapper - Basic Example

```javascript
=ApparelOrderMapper(
  inputRecords: {
    /* Simple apparel order data */
    customer_style: "FW24-DRESS-005",
    style_description: "Women's Wool Dress",
    color_code: "BLK", 
    color_name: "Black",
    material_composition: "80% Wool, 20% Polyester",
    order_quantity: "1200",
    customer_po_number: "PO-2024-001789",
    delivery_date_requested: "2024-12-01",
    season: "Fall Winter 2024",
    department: "Women's Formal"
  },
  azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
  azureOpenAIKey: cons!AZURE_OPENAI_KEY,
  azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
  azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
  targetFields: {
    "F2:Customer Style Number",
    "F5:Style Description", 
    "F6:Color Code",
    "O11:FG Order Quantity",
    "C1:Season"
  },
  userPrompt: "Map this apparel order data to standard industry fields. Focus on accurate style and quantity information."
)

/* Expected Output Structure */
{
  mappedResult: [
    {
      field_code: "F2",
      field_name: "Customer Style Number", 
      input_key: "customer_style",
      value: "FW24-DRESS-005",
      confidence: 98
    },
    {
      field_code: "O11",
      field_name: "FG Order Quantity",
      input_key: "order_quantity",
      value: "1200", 
      confidence: 100
    }
    /* ... additional mappings ... */
  ],
  overallConfidence: 0.89
}
```

### Apparel Order Mapper - Complete Example

```javascript
=ApparelOrderMapper(
  inputRecords: {
    /* Raw apparel order data */
    customer_style: "SS24-POLO-001",
    style_description: "Men's Cotton Polo Shirt",
    color_code: "NVY",
    color_name: "Navy Blue",
    material_composition: "100% Cotton",
    sizes_ordered: "S,M,L,XL",
    customer_po_number: "PO-2024-001234",
    vendor_po_number: "VPO-SS24-5678",
    order_quantity: "2400",
    unit_price: "12.50",
    customer_number: "CUST-1001",
    ship_to_location: "DC-EAST-01",
    delivery_date_requested: "2024-08-15",
    season: "Spring Summer 2024",
    buy_program: "Core Basics",
    department: "Men's Casual",
    product_category: "Knit Tops",
    material_group: "Cotton Knits",
    production_plant: "PLANT-VN-01",
    sales_office: "NYC-SALES",
    order_type: "BULK",
    currency: "USD"
  },
  azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
  azureOpenAIKey: cons!AZURE_OPENAI_KEY,
  azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
  azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
  targetFields: {
    "F2:Customer Style Number",
    "F5:Style Description", 
    "F6:Color Code",
    "F7:Color Description",
    "F18:Material Type",
    "F19:Material Group",
    "O9:Customer Purchase Order",
    "O10:Vendor Purchase Order",
    "O11:FG Order Quantity",
    "O16:Actual Production Plant Allocated",
    "C1:Season",
    "C6:Customer Department",
    "L2:Ship to party",
    "L6:Requested Delivery Date",
    "P1:FG Price (FOB)"
  },
  userPrompt: "Focus on accurate mapping of product attributes and order details. Ensure quantities and dates are properly formatted. Map color codes to standard industry formats."
)

/* Expected Output Structure */
{
  mappedResult: [
    {
      field_code: "F2",
      field_name: "Customer Style Number",
      input_key: "customer_style",
      value: "SS24-POLO-001",
      confidence: 98
    },
    {
      field_code: "F5", 
      field_name: "Style Description",
      input_key: "style_description",
      value: "Men's Cotton Polo Shirt",
      confidence: 100
    },
    {
      field_code: "F6",
      field_name: "Color Code", 
      input_key: "color_code",
      value: "NVY",
      confidence: 95
    },
    {
      field_code: "O11",
      field_name: "FG Order Quantity",
      input_key: "order_quantity", 
      value: "2400",
      confidence: 100
    }
    /* ... additional mappings ... */
  ],
  overallConfidence: 0.87
}
```

### Advanced Usage Patterns

#### Batch Processing Example
```javascript
/* Process multiple apparel order records efficiently */
a!forEach(
  items: local!apparelOrderRecords,
  expression: ApparelOrderMapper(
    inputRecords: fv!item,
    azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
    azureOpenAIKey: cons!AZURE_OPENAI_KEY, 
    azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
    azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
    targetFields: local!standardApparelFields,
    userPrompt: "Map apparel order data to industry standard fields"
  )
)
```

#### Conditional Processing Example
```javascript
/* Apply different prompts based on apparel data type */
if(
  isnull(ri!inputRecords.season),
  /* Basic product mapping prompt */
  "Map basic apparel product information focusing on style, color, and material details",
  /* Seasonal order mapping prompt */ 
  "Map seasonal apparel order data including product details, quantities, and delivery requirements"
)
```

#### Error Handling Example
```javascript
/* Robust error handling pattern */
a!try(
  ApparelOrderMapper(
    inputRecords: ri!apparelOrderData,
    azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
    azureOpenAIKey: cons!AZURE_OPENAI_KEY,
    azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
    azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
    targetFields: ri!targetFields,
    userPrompt: ri!mappingPrompt
  ),
  a!catch(
    error: {
      success: false,
      errorMessage: "Apparel order mapping failed: " & fv!error.message,
      fallbackData: ri!apparelOrderData
    }
  )
)
```

## 📚 API Reference

### Apparel Order Mapper

#### Input Parameters  

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `inputRecords` | TypedValue | ✅ | Raw apparel order data | Appian Dictionary |
| `azureOpenAIEndpoint` | String | ✅ | Azure OpenAI service URL | `"https://resource.openai.azure.com/"` |
| `azureOpenAIKey` | String | ✅ | API authentication key | `"abc123...xyz789"` |
| `azureOpenAIDeploymentName` | String | ✅ | Model deployment name | `"gpt-4-deployment"` |
| `azureOpenAIApiVersion` | String | ✅ | Azure OpenAI API version | `"2023-05-15"` |
| `targetFields` | TypedValue | ✅ | List of target field mappings | Text List |
| `userPrompt` | String | ✅ | Custom mapping instructions | Custom prompt string |

#### Output Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `mappedResult` | String | JSON array of mapped fields with confidence scores |
| `overallConfidence` | Double | Overall mapping confidence (0.0-1.0) |

### Target Field Format Reference

#### Supported Formats
```javascript
// Format 1: Code and Name
"F2:Customer Style Number"

// Format 2: Code - Name  
"F2 - Customer Style Number"

// Format 3: Name only (auto-generates code)
"Customer Style Number"
```

#### Standard Apparel Field Categories

**Product Fields (F-series)**
```javascript
[
  "F2:Customer Style Number",
  "F5:Style Description", 
  "F6:Color Code",
  "F7:Color Description",
  "F8:Master Grid",
  "F9:FG Sizes",
  "F18:Material Type",
  "F19:Material Group",
  "F20:External Material Group"
  // ... 40+ additional product fields
]
```

**Order Fields (O-series)**  
```javascript
[
  "O2:Buy Month",
  "O3:Buy Year", 
  "O4:Buy Sequence",
  "O6:Customer Number (Sold to Party)",
  "O8:Order Type",
  "O9:Customer Purchase Order",
  "O10:Vendor Purchase Order",
  "O11:FG Order Quantity"
  // ... 25+ additional order fields  
]
```

**Customer & Location Fields (C & L-series)**
```javascript
[
  "C1:Season",
  "C2:Sales Organization",
  "C6:Customer Department", 
  "L1:Regions",
  "L2:Ship to party",
  "L3:Ship Mode",
  "L6:Requested Delivery Date"
  // ... 15+ additional customer/location fields
]
```

**Pricing & Quantity Fields (P & S-series)**
```javascript
[
  "P1:FG Price (FOB)",
  "P6:Standard Price (ThirdParty Orders)",
  "S1:QTY", 
  "S2:FOB"
  // ... additional pricing/quantity fields
]
```

## 🏗️ Architecture

### 1. High-Level System Architecture

```mermaid
graph TB
    %% External Systems
    subgraph "External Systems"
        APPIAN[Appian Platform<br/>Process Models & Interfaces]
        AZURE[Azure OpenAI Service<br/>GPT-4/GPT-3.5 Models]
    end

    %% Plugin Core - CORRECTED to show only one smart service
    subgraph "IntelliMap Smart Service Plugin v4.0.0"
        subgraph "Smart Service Layer"
            AOM[Apparel Order Mapper<br/>ApparelOrderMapper.java<br/>Single Smart Service Implementation]
        end
        
        subgraph "Core Components"
            CONFIG[Configuration Manager<br/>IntelliMapConfig.java]
            RATE[Rate Limiter<br/>RateLimiter.java<br/>Sliding Window Algorithm]
            JSON[JSON Processor<br/>Jackson Libraries]
        end
        
        subgraph "Internationalization"
            I18N_DEFAULT[apparel-order-mapper-v4.properties<br/>Default Locale Messages]
            I18N_EN[apparel-order-mapper-v4_en_US.properties<br/>English US Messages]
        end
        
        subgraph "Processing Pipeline"
            VALIDATE[Input Validation<br/>& Sanitization]
            PARSE[Data Parser<br/>Multiple Format Support]
            CHUNK[Chunking Engine<br/>Large Input Handling]
            RETRY[Retry Logic<br/>Exponential Backoff]
            CONFIDENCE[Confidence Calculator<br/>Quality Scoring]
        end
    end

    %% Build Artifacts
    subgraph "Build Output"
        JAR[intellimapsmartservice-v4.0.0.jar<br/>32MB Final Plugin<br/>Single Deployable Artifact]
        CLASSES[Compiled Classes<br/>ApparelOrderMapper.class<br/>IntelliMapConfig.class<br/>RateLimiter.class]
    end

    %% Data Flow - Input Processing
    APPIAN -->|Process Input Data| AOM
    
    AOM --> VALIDATE
    VALIDATE --> PARSE
    PARSE -->|Large Input| CHUNK
    PARSE -->|Regular Input| RATE
    CHUNK --> RATE
    
    %% Configuration and Dependencies
    CONFIG -.->|Configuration Values| AOM
    CONFIG -.->|Rate Limits & Timeouts| RATE
    CONFIG -.->|JSON Settings| JSON
    I18N_DEFAULT -.->|Error Messages & Labels| AOM
    I18N_EN -.->|Localized Messages| AOM
    
    RATE -->|Rate Check Passed| RETRY
    
    %% External API Integration
    RETRY -->|HTTP Request<br/>with API Key| AZURE
    AZURE -->|JSON Response| JSON
    
    %% Response Processing
    JSON --> CONFIDENCE
    CONFIDENCE --> AOM
    
    %% Output
    AOM -->|mappedResult JSON Array<br/>overallConfidence Score| APPIAN

    %% Build Process
    AOM --> CLASSES
    CONFIG --> CLASSES
    RATE --> CLASSES
    I18N_DEFAULT --> CLASSES
    I18N_EN --> CLASSES
    CLASSES --> JAR

    %% Data Formats & Protocols
    subgraph "Supported Input Formats"
        FORMATS["• Apparel Dictionary Format<br/>• JSON Objects/Arrays<br/>• Custom Delimited Format<br/>• Map Collections"]
    end
    
    subgraph "Output Standards"
        OUTPUT["• JSON Array of Mapped Fields<br/>• Individual Confidence Scores<br/>• Overall Confidence Rating<br/>• Error Handling & Logging"]
    end
    
    %% Dependencies
    subgraph "External Dependencies"
        JACKSON["Jackson JSON Libraries<br/>• jackson-core-2.15.2<br/>• jackson-databind-2.15.2<br/>• jackson-annotations-2.15.2"]
        SDK["Appian Plugin SDK<br/>Smart Service Framework"]
    end
    
    %% Technical Specifications
    subgraph "Technical Details"
        SPECS["• Java 8+ Runtime<br/>• Thread-Safe Operations<br/>• Concurrent Request Handling<br/>• Memory-Efficient Processing<br/>• Multi-Language Support (i18n)"]
    end

    %% Styling
    classDef external fill:#e1f5fe,stroke:#01579b,stroke-width:2px,color:#000
    classDef plugin fill:#f3e5f5,stroke:#4a148c,stroke-width:2px,color:#000
    classDef core fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#000
    classDef process fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px,color:#000
    classDef data fill:#fff8e1,stroke:#f57f17,stroke-width:2px,color:#000
    classDef i18n fill:#fce4ec,stroke:#c2185b,stroke-width:2px,color:#000
    classDef build fill:#f1f8e9,stroke:#689f38,stroke-width:2px,color:#000
    
    class APPIAN,AZURE external
    class AOM,CONFIG,RATE,JSON plugin
    class VALIDATE,PARSE,CHUNK,RETRY,CONFIDENCE process
    class FORMATS,OUTPUT,JACKSON,SDK,SPECS data
    class I18N_DEFAULT,I18N_EN i18n
    class JAR,CLASSES build
```

### 2. High-Level Process Flow

```mermaid
flowchart TD
    %% Start
    START([🚀 Appian Process Model<br/>Initiates ApparelOrderMapper])
    
    %% Main Processing Steps
    INPUT[📥 Receive Input Data<br/>• Apparel order records<br/>• Target field mappings<br/>• Azure OpenAI credentials]
    
    VALIDATE[✅ Validate & Parse<br/>• Check required parameters<br/>• Parse input format<br/>• Sanitize data]
    
    RATE_LIMIT[⏱️ Rate Limiting<br/>• Check API limits<br/>• Apply delays if needed<br/>• 60 req/min, 1000 req/hour]
    
    AI_PROCESS[🤖 Azure OpenAI Processing<br/>• Build dynamic prompt<br/>• Send API request<br/>• GPT-4 intelligent mapping]
    
    PARSE_RESULTS[📊 Process Results<br/>• Extract mapped fields<br/>• Calculate confidence scores<br/>• Format JSON output]
    
    RETURN[📤 Return to Appian<br/>• mappedResult: JSON array<br/>• overallConfidence: score<br/>• Error handling if needed]
    
    %% End
    END([✨ Complete<br/>Apparel Data Successfully Mapped])

    %% Flow connections
    START --> INPUT
    INPUT --> VALIDATE
    VALIDATE --> RATE_LIMIT
    RATE_LIMIT --> AI_PROCESS
    AI_PROCESS --> PARSE_RESULTS
    PARSE_RESULTS --> RETURN
    RETURN --> END

    %% Error handling (simplified)
    VALIDATE -.->|❌ Validation Error| ERROR[🚨 Error Response<br/>SmartServiceException]
    AI_PROCESS -.->|❌ API Error| RETRY[🔄 Retry Logic<br/>Up to 3 attempts]
    RETRY -.->|✅ Success| PARSE_RESULTS
    RETRY -.->|❌ All retries failed| ERROR
    ERROR -.-> END

    %% Styling
    classDef startEnd fill:#e8f5e8,stroke:#4caf50,stroke-width:3px,color:#000
    classDef process fill:#e3f2fd,stroke:#2196f3,stroke-width:2px,color:#000
    classDef ai fill:#fff3e0,stroke:#ff9800,stroke-width:2px,color:#000
    classDef error fill:#ffebee,stroke:#f44336,stroke-width:2px,color:#000
    
    class START,END startEnd
    class INPUT,VALIDATE,RATE_LIMIT,PARSE_RESULTS,RETURN process
    class AI_PROCESS ai
    class ERROR,RETRY error
```

### 3. Detailed Data Flow Processing

```mermaid
flowchart TD
    %% Input Stage
    START([Appian Process Model<br/>Calls ApparelOrderMapper<br/>Only Smart Service])
    
    %% Input Parameter Validation
    PARAM_CHECK[Parameter Validation<br/>7 Required Input Parameters<br/>Using i18n Error Messages]
    
    %% Input Type Detection - CORRECTED based on actual parsing logic
    INPUT{Input Records<br/>Type Detection}
    APPIAN_DICT[Apparel Dictionary Format<br/>Starts with bracket-asterisk]
    JSON_FORMAT[JSON Object/Array<br/>Starts with curly or square bracket]
    DELIMITED[Custom Delimited Format<br/>key=value pairs]
    
    %% Multi-record Detection - Based on actual implementation
    MULTI{Multiple Records<br/>Detection Logic}
    SINGLE[Single Record Processing<br/>parseCustomDelimitedFormat]
    BATCH[Multiple Record Batch<br/>processMultipleRecords method]
    
    %% Size Check and Chunking - Based on IntelliMapConfig constants
    SIZE{Input Size Check<br/>MAX_INPUT_TOKENS_PER_CHUNK: 6000<br/>MAX_INPUT_KEYS_PER_CHUNK: 50}
    NORMAL[Normal Processing<br/>Direct API Call]
    CHUNKING[Chunking Required<br/>processChunkedInput method<br/>MAX_CHUNKS_PER_REQUEST: 5]
    
    %% Rate Limiting - Based on RateLimiter.java implementation
    RATE_CHECK[Rate Limiter Check<br/>Sliding Window Algorithm<br/>60 req/min, 1000 req/hour]
    RATE_WAIT[Apply Delay<br/>RATE_LIMIT_DELAY: 1 second<br/>Check minute and hour limits]
    
    %% Azure OpenAI Integration - Based on actual API calls
    BUILD_PROMPT[Build System Prompt<br/>Include Target Fields<br/>Add User Instructions]
    API_CALL[callAzureOpenAIWithRetry<br/>HttpClient with Timeout: 30s<br/>Max Tokens: 8192, Temperature: 0.1]
    API_ERROR{API Response<br/>Status Check}
    RETRY_LOGIC[Exponential Backoff Retry<br/>MAX_RETRIES: 3<br/>2^attempt seconds delay]
    
    %% Response Processing - Based on parseOpenAIResponseWithJackson
    JSON_PARSE[Parse JSON Response<br/>Extract choices.message.content<br/>Jackson ObjectMapper]
    CONTENT_EXTRACT[Extract Mapping Content<br/>Look for Direct Array or<br/>Wrapped result field]
    FIELD_MAPPING[Process Field Mappings<br/>Extract field_code, field_name,<br/>input_key, value, confidence]
    
    %% Confidence Calculation - Based on calculateOverallConfidence
    CONFIDENCE_CALC[Calculate Confidence Scores<br/>Individual Field Confidence<br/>Overall Average Calculation<br/>DEFAULT_CONFIDENCE_SCORE: 0.5]
    
    %% Output Formatting - Based on actual return values
    MERGE{Multiple Chunks<br/>to Merge?}
    CHUNK_MERGE[mergeChunkResults method<br/>Combine All Mappings<br/>Create JSON Array]
    FINAL_FORMAT[Format Output<br/>mappedResult: JSON Array<br/>overallConfidence: Double]
    
    %% Error Handling - Based on SmartServiceException usage
    ERROR_HANDLE[SmartServiceException<br/>With i18n Error Messages<br/>From .properties files]
    FALLBACK[Partial Results Handling<br/>Continue Processing Other Records<br/>Log Errors to System.err]
    
    %% End States
    SUCCESS([Return Success<br/>mappedResult + overallConfidence<br/>to Appian Process])
    FAILURE([Return SmartServiceException<br/>With Localized Error Message])

    %% Flow Connections
    START --> PARAM_CHECK
    PARAM_CHECK --> INPUT
    
    INPUT -->|Apparel Dictionary| APPIAN_DICT
    INPUT -->|JSON Format| JSON_FORMAT
    INPUT -->|Delimited Format| DELIMITED
    
    APPIAN_DICT --> MULTI
    JSON_FORMAT --> MULTI
    DELIMITED --> MULTI
    
    MULTI -->|Single Record| SINGLE
    MULTI -->|Multiple Records| BATCH
    
    SINGLE --> SIZE
    BATCH --> SIZE
    
    SIZE -->|Small Input| NORMAL
    SIZE -->|Large Input| CHUNKING
    
    NORMAL --> RATE_CHECK
    CHUNKING --> RATE_CHECK
    
    RATE_CHECK -->|Rate OK| BUILD_PROMPT
    RATE_CHECK -->|Rate Exceeded| RATE_WAIT
    RATE_WAIT --> RATE_CHECK
    
    BUILD_PROMPT --> API_CALL
    API_CALL --> API_ERROR
    API_ERROR -->|Success 200| JSON_PARSE
    API_ERROR -->|Error| RETRY_LOGIC
    
    RETRY_LOGIC -->|Retry Available| API_CALL
    RETRY_LOGIC -->|Max Retries Reached| ERROR_HANDLE
    
    JSON_PARSE --> CONTENT_EXTRACT
    CONTENT_EXTRACT --> FIELD_MAPPING
    FIELD_MAPPING --> CONFIDENCE_CALC
    CONFIDENCE_CALC --> MERGE
    
    MERGE -->|Single Response| FINAL_FORMAT
    MERGE -->|Multiple Chunks| CHUNK_MERGE
    CHUNK_MERGE --> FINAL_FORMAT
    
    FINAL_FORMAT --> SUCCESS
    
    ERROR_HANDLE --> FALLBACK
    FALLBACK --> FAILURE

    %% Styling
    classDef startEnd fill:#e8eaf6,stroke:#3f51b5,stroke-width:3px,color:#000
    classDef process fill:#e8f5e8,stroke:#4caf50,stroke-width:2px,color:#000
    classDef decision fill:#fff3e0,stroke:#ff9800,stroke-width:2px,color:#000
    classDef external fill:#e1f5fe,stroke:#2196f3,stroke-width:2px,color:#000
    classDef error fill:#ffebee,stroke:#f44336,stroke-width:2px,color:#000
    classDef config fill:#f3e5f5,stroke:#9c27b0,stroke-width:2px,color:#000
    
    class START,SUCCESS,FAILURE startEnd
    class PARAM_CHECK,APPIAN_DICT,JSON_FORMAT,DELIMITED,SINGLE,BATCH,NORMAL,CHUNKING,BUILD_PROMPT,JSON_PARSE,CONTENT_EXTRACT,FIELD_MAPPING,CHUNK_MERGE,FINAL_FORMAT process
    class INPUT,MULTI,SIZE,API_ERROR,MERGE decision
    class API_CALL,RATE_WAIT external
    class ERROR_HANDLE,FALLBACK,RETRY_LOGIC error
    class RATE_CHECK,CONFIDENCE_CALC config
```

### 4. Component Dependencies & Relationships

```mermaid
graph TD
    %% Plugin Definition
    subgraph "Appian Plugin Framework"
        PLUGIN[appian-plugin.xml<br/>Plugin Configuration<br/>Version 4.0.0<br/>Single Smart Service Definition]
    end

    %% CORRECTED - Only One Smart Service
    subgraph "Smart Service Implementation"
        AOM[ApparelOrderMapper.java<br/>@AppianSmartService<br/>@PaletteInfo<br/>Only Smart Service Class<br/>1613 lines of code]
    end

    %% Core Components
    subgraph "Configuration & Utilities"
        CONFIG[IntelliMapConfig.java<br/>Static Configuration<br/>Constants & Settings<br/>56 lines]
        RATE[RateLimiter.java<br/>Thread-Safe<br/>Sliding Window Algorithm<br/>135 lines]
    end

    %% NEW - Internationalization Support
    subgraph "Internationalization Files"
        I18N_DEFAULT[apparel-order-mapper-v4.properties<br/>Default Locale Messages<br/>48 lines]
        I18N_EN[apparel-order-mapper-v4_en_US.properties<br/>English US Messages<br/>48 lines]
    end

    %% External Libraries
    subgraph "Jackson JSON Processing"
        MAPPER[ObjectMapper<br/>JSON Serialization/Deserialization]
        NODE[JsonNode<br/>JSON Tree Navigation]
        EXCEPTION[JsonProcessingException<br/>Error Handling]
    end

    %% HTTP Client
    subgraph "HTTP Communication"
        CLIENT[HttpClient<br/>Azure OpenAI Communication]
        REQUEST[HttpRequest<br/>API Request Building]
        RESPONSE[HttpResponse<br/>API Response Handling]
    end

    %% Data Structures
    subgraph "Data Models"
        TYPED[TypedValue<br/>Appian Input Parameters]
        MAP[HashMap & ConcurrentHashMap<br/>Data Storage & Thread Safety]
        LIST[ArrayList<br/>Record Collections]
        STRING[String<br/>JSON & Text Processing]
    end

    %% UPDATED - Actual Methods from Code
    subgraph "Core Processing Methods"
        VALIDATE[validateInputs<br/>7 Parameter Validation]
        PARSE_MULTI[parseMultipleRecordsFromAppianFormat<br/>Multiple Record Support]
        PARSE_SINGLE[parseCustomDelimitedFormat<br/>Single Record Parsing]
        CHUNK[processChunkedInput<br/>Large Input Handling]
        AZURE[callAzureOpenAIWithRetry<br/>API Integration with Retry]
        CONFIDENCE[calculateOverallConfidence<br/>Quality Assessment]
        BUILD_PROMPT[buildSystemPrompt<br/>Dynamic Prompt Generation]
    end

    %% CORRECTED - Build Output
    subgraph "Build Artifacts"
        CLASSES[Compiled Classes<br/>ApparelOrderMapper.class: 40KB<br/>IntelliMapConfig.class: 1.8KB<br/>RateLimiter.class: 6.1KB]
        JAR[intellimapsmartservice-v4.0.0.jar<br/>32MB Final Plugin<br/>Single Deployable Artifact]
    end

    %% Dependencies and Relationships
    PLUGIN --> AOM
    
    AOM --> CONFIG
    AOM --> RATE
    AOM --> MAPPER
    AOM --> CLIENT
    AOM --> TYPED
    AOM --> I18N_DEFAULT
    AOM --> I18N_EN
    
    CONFIG -.->|Provides Settings| VALIDATE
    CONFIG -.->|Rate Limits| RATE
    CONFIG -.->|Timeouts| CLIENT
    CONFIG -.->|Token Limits| BUILD_PROMPT
    
    I18N_DEFAULT -.->|Error Messages| VALIDATE
    I18N_EN -.->|Localized Messages| VALIDATE
    
    RATE --> |Thread Safety| MAP
    RATE --> |Time Management| STRING
    
    MAPPER --> NODE
    MAPPER --> EXCEPTION
    
    CLIENT --> REQUEST
    CLIENT --> RESPONSE
    
    %% Method Dependencies
    AOM --> VALIDATE
    AOM --> PARSE_MULTI
    AOM --> PARSE_SINGLE
    AOM --> CHUNK
    AOM --> AZURE
    AOM --> CONFIDENCE
    AOM --> BUILD_PROMPT
    
    VALIDATE --> TYPED
    PARSE_MULTI --> STRING
    PARSE_MULTI --> MAP
    PARSE_SINGLE --> MAP
    CHUNK --> LIST
    AZURE --> CLIENT
    AZURE --> RATE
    CONFIDENCE --> NODE
    BUILD_PROMPT --> CONFIG

    %% External System Integration
    subgraph "External Systems"
        APPIAN_PLATFORM[Appian Platform<br/>Process Models<br/>Smart Service Framework]
        AZURE_AI[Azure OpenAI Service<br/>GPT Models<br/>REST API]
    end

    APPIAN_PLATFORM --> TYPED
    AZURE --> AZURE_AI

    %% Build Process
    AOM --> CLASSES
    CONFIG --> CLASSES
    RATE --> CLASSES
    I18N_DEFAULT --> CLASSES
    I18N_EN --> CLASSES
    CLASSES --> JAR

    %% Configuration Details - UPDATED with actual values
    subgraph "Configuration Constants"
        TIMEOUTS["• DEFAULT_TIMEOUT: 30s<br/>• RATE_LIMIT_DELAY: 1s<br/>• MAX_RETRIES: 3"]
        LIMITS["• MAX_TOKENS: 8192<br/>• MAX_REQUESTS_PER_MINUTE: 60<br/>• MAX_CHUNKS_PER_REQUEST: 5<br/>• MAX_INPUT_KEYS_PER_CHUNK: 50"]
        API_SETTINGS["• TEMPERATURE: 0.1<br/>• CONTENT_TYPE_JSON<br/>• API_KEY_HEADER<br/>• DEFAULT_ENCODING: UTF-8"]
    end

    CONFIG --> TIMEOUTS
    CONFIG --> LIMITS
    CONFIG --> API_SETTINGS

    %% Styling
    classDef plugin fill:#e8eaf6,stroke:#3f51b5,stroke-width:2px,color:#000
    classDef service fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#000
    classDef core fill:#fff3e0,stroke:#f57c00,stroke-width:2px,color:#000
    classDef i18n fill:#fce4ec,stroke:#c2185b,stroke-width:2px,color:#000
    classDef jackson fill:#e8f5e8,stroke:#388e3c,stroke-width:2px,color:#000
    classDef http fill:#e1f5fe,stroke:#0288d1,stroke-width:2px,color:#000
    classDef data fill:#fff8e1,stroke:#fbc02d,stroke-width:2px,color:#000
    classDef methods fill:#e3f2fd,stroke:#1976d2,stroke-width:2px,color:#000
    classDef external fill:#f1f8e9,stroke:#689f38,stroke-width:2px,color:#000
    classDef config fill:#fff3e0,stroke:#ff8f00,stroke-width:1px,color:#000
    classDef build fill:#f9fbe7,stroke:#827717,stroke-width:2px,color:#000

    class PLUGIN plugin
    class AOM service
    class CONFIG,RATE core
    class I18N_DEFAULT,I18N_EN i18n
    class MAPPER,NODE,EXCEPTION jackson
    class CLIENT,REQUEST,RESPONSE http
    class TYPED,MAP,LIST,STRING data
    class VALIDATE,PARSE_MULTI,PARSE_SINGLE,CHUNK,AZURE,CONFIDENCE,BUILD_PROMPT methods
    class APPIAN_PLATFORM,AZURE_AI external
    class TIMEOUTS,LIMITS,API_SETTINGS config
    class CLASSES,JAR build
```

### Component Overview

#### Core Components

**ApparelOrderMapper.java**  
- **Only smart service implementation** in the plugin
- Specialized for apparel industry order data mapping
- Industry-specific field mappings and semantic understanding
- Support for 106+ standardized apparel fields
- Advanced confidence scoring and error handling
- Manages Azure OpenAI API integration with retry logic

**IntelliMapConfig.java**
- Centralized configuration management
- Environment-specific settings
- Runtime configuration parameters
- Constants and defaults

**RateLimiter.java**
- Sliding window rate limiting algorithm
- Thread-safe implementation 
- Configurable limits (per minute/hour)
- Automatic cleanup of expired entries

#### Supporting Infrastructure

**Internationalization Support**
- `apparel-order-mapper-v4.properties` - Default locale messages
- `apparel-order-mapper-v4_en_US.properties` - English US messages
- Localized error messages and parameter descriptions
- Multi-language support for global deployments

**Logging Framework**
- Structured logging with multiple levels
- Performance metrics and monitoring
- Debug mode for troubleshooting
- Audit trail capabilities

**JSON Processing**
- Jackson library integration
- Type-safe JSON operations
- Robust parsing with error recovery
- Optimal performance

### Data Flow

1. **Input Processing**
   - Validate input parameters
   - Sanitize data for security
   - Format for Azure OpenAI API

2. **Rate Limiting**
   - Check current request rate
   - Apply delays if necessary  
   - Update request counters

3. **API Communication**
   - Construct Azure OpenAI request
   - Execute with timeout handling
   - Implement retry logic on failures

4. **Response Processing**
   - Parse JSON response with Jackson
   - Extract mapped data and confidence scores
   - Validate response structure

5. **Output Generation**
   - Format mapped results
   - Calculate overall confidence
   - Return structured response

## 🚨 Error Handling

### Error Categories

#### Input Validation Errors
```java
// Missing required parameters
MISSING_REQUIRED_PARAMETER("Required parameter {0} is missing or null")

// Invalid data formats  
INVALID_INPUT_FORMAT("Input data format is invalid: {0}")

// Parameter validation failures
PARAMETER_VALIDATION_FAILED("Parameter validation failed for {0}: {1}")
```

#### API Communication Errors
```java
// Authentication failures
AZURE_AUTHENTICATION_FAILED("Azure OpenAI authentication failed")

// Network connectivity issues
NETWORK_CONNECTION_ERROR("Network connection error: {0}")

// API quota exceeded
RATE_LIMIT_EXCEEDED("API rate limit exceeded. Retry after: {0}")

// Service unavailable
AZURE_SERVICE_UNAVAILABLE("Azure OpenAI service temporarily unavailable")
```

#### Processing Errors
```java
// JSON parsing failures
JSON_PARSING_ERROR("Failed to parse JSON response: {0}")

// Invalid response format
INVALID_RESPONSE_FORMAT("Azure OpenAI response format is invalid")

// Mapping failures
MAPPING_OPERATION_FAILED("Data mapping operation failed: {0}")
```

### Error Recovery Strategies

#### Automatic Retry Logic
```java
public class RetryConfig {
    public static final int MAX_RETRIES = 3;
    public static final Duration BASE_DELAY = Duration.ofSeconds(1);
    public static final double BACKOFF_MULTIPLIER = 2.0;
    
    // Exponential backoff: 1s, 2s, 4s
    public static Duration calculateDelay(int retryAttempt) {
        return BASE_DELAY.multipliedBy((long) Math.pow(BACKOFF_MULTIPLIER, retryAttempt));
    }
}
```

#### Graceful Degradation
- **Partial Results**: Return successfully mapped fields even if some fail
- **Fallback Values**: Use default mappings when AI processing fails  
- **Error Context**: Provide detailed error information for debugging
- **Recovery Options**: Suggest alternative approaches or manual intervention

#### Error Logging and Monitoring
```java
// Structured error logging
logger.error("Mapping operation failed", 
    Map.of(
        "operation", "IntelliMapSmartService",
        "customerId", customerId,
        "errorCode", errorCode,
        "retryAttempt", retryCount,
        "processingTime", processingTime
    )
);
```

### Best Practices for Error Handling

1. **Always Use Try-Catch**: Wrap smart service calls in error handling
2. **Check Confidence Scores**: Validate mapping quality before processing
3. **Implement Timeouts**: Set reasonable timeout values for API calls
4. **Log Errors Appropriately**: Use structured logging for debugging
5. **Provide Fallbacks**: Have alternative processing paths ready
6. **Monitor Rate Limits**: Track API usage to prevent quota issues

## 🔒 Performance & Security

### Performance Optimization

#### Connection Management
```java
// HTTP client configuration
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .executor(Executors.newFixedThreadPool(5))
    .build();
```

#### Memory Management
- **Connection Pooling**: Reuse HTTP connections for efficiency
- **Rate Limit Cleanup**: Automatic removal of expired rate limit entries
- **JSON Streaming**: Process large responses without full memory loading
- **Garbage Collection**: Optimize object creation and cleanup

#### Caching Strategies
```java
// Configuration caching
private static final Map<String, Object> configCache = new ConcurrentHashMap<>();

// Response caching for duplicate requests (optional)
private static final Cache<String, String> responseCache = 
    CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build();
```

### Security Measures

#### API Key Protection
- **Secure Storage**: API keys stored in Appian secure constants
- **Transmission Security**: HTTPS-only communication
- **Key Rotation**: Support for regular API key updates
- **Access Control**: Restricted access to configuration constants

#### Input Sanitization
```java
// Input validation and sanitization
public String sanitizeInput(String input) {
    if (input == null) return null;
    
    // Remove potentially dangerous characters
    String sanitized = input.replaceAll("[<>\"'&]", "");
    
    // Limit length to prevent DoS attacks
    if (sanitized.length() > MAX_INPUT_LENGTH) {
        sanitized = sanitized.substring(0, MAX_INPUT_LENGTH);
    }
    
    return sanitized;
}
```

#### Rate Limiting Security
- **DoS Protection**: Prevents abuse through request limiting
- **IP-based Limiting**: Optional IP-based rate limiting
- **Quota Management**: Prevents accidental API quota exhaustion
- **Monitoring**: Track unusual usage patterns

#### Data Privacy
- **No Data Persistence**: Input data not stored permanently
- **Logging Controls**: Configurable logging levels for sensitive data
- **Audit Trails**: Complete tracking of data processing operations
- **Compliance**: GDPR and CCPA compliant data handling

### Performance Monitoring

#### Key Metrics
```java
// Performance metrics to monitor
- API Response Time (avg/p95/p99)
- Request Success Rate
- Rate Limit Utilization  
- Memory Usage
- Error Rates by Category
- Confidence Score Distribution
```

#### Logging Performance Data
```java
logger.info("Performance metrics", 
    Map.of(
        "operation", operationType,
        "responseTimeMs", responseTime,
        "confidenceScore", confidence,
        "inputSizeKb", inputSize / 1024,
        "outputSizeKb", outputSize / 1024,
        "retryCount", retries
    )
);
```

## 🔧 Troubleshooting

### Common Issues and Solutions

#### 1. Missing JAR Dependencies
**Symptoms**: ClassNotFoundException, NoClassDefFoundError
```bash
# Error example
java.lang.ClassNotFoundException: com.fasterxml.jackson.databind.ObjectMapper
```
**Solution**: 
```bash
# Verify JAR files exist in correct location
ls -la src/META-INF/lib/
# Should show:
# jackson-core-2.15.2.jar
# jackson-databind-2.15.2.jar  
# jackson-annotations-2.15.2.jar

# Download missing JARs if needed
wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar
```

#### 2. Azure OpenAI Authentication Failures
**Symptoms**: 401 Unauthorized, 403 Forbidden responses
```json
{
  "error": {
    "code": "InvalidAuthenticationToken",
    "message": "The access token is invalid"
  }
}
```
**Solution**:
```javascript
// Verify credentials in Appian constants
cons!AZURE_OPENAI_KEY // Should be 32-character string
cons!AZURE_OPENAI_ENDPOINT // Should end with trailing slash
cons!AZURE_OPENAI_DEPLOYMENT // Should match deployed model name

// Test with minimal request
curl -X POST "https://your-resource.openai.azure.com/openai/deployments/your-deployment/completions?api-version=2023-05-15" \
  -H "api-key: YOUR_API_KEY" \
  -H "Content-Type: application/json"
```

#### 3. Rate Limiting Issues
**Symptoms**: 429 Too Many Requests, slow processing
```json
{
  "error": {
    "code": "TooManyRequests", 
    "message": "Rate limit exceeded"
  }
}
```
**Solution**:
```java
// Adjust rate limits in IntelliMapConfig.java
public static final int MAX_REQUESTS_PER_MINUTE = 30; // Reduce from 60
public static final int MAX_REQUESTS_PER_HOUR = 500;  // Reduce from 1000
public static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(2000); // Increase delay
```

#### 4. JSON Parsing Errors
**Symptoms**: JsonProcessingException, malformed response errors
```bash
# Error example
com.fasterxml.jackson.core.JsonParseException: Unexpected character
```
**Solution**:
```java
// Enable debug logging to see raw responses
public static final boolean ENABLE_DEBUG_LOGGING = true;

// Check Azure OpenAI response format
// Ensure deployment returns valid JSON
// Verify prompt instructions request JSON output
```

#### 5. Timeout Issues
**Symptoms**: SocketTimeoutException, slow responses
```bash
# Error example  
java.net.SocketTimeoutException: Read timed out
```
**Solution**:
```java
// Increase timeout values
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60); // Increase from 30
public static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(20);

// Monitor Azure OpenAI service status
// Consider model performance (GPT-4 vs GPT-3.5)
```

#### 6. Memory Issues
**Symptoms**: OutOfMemoryError, heap space errors
```bash
# Error example
java.lang.OutOfMemoryError: Java heap space
```
**Solution**:
```java
// Limit input data size
public static final int MAX_INPUT_SIZE = 50000; // 50KB limit

// Process data in batches
// Use streaming JSON parsing for large responses
// Monitor memory usage in Appian Admin Console
```

### Debug Mode Configuration

#### Enable Debug Logging
```java
// In IntelliMapConfig.java
public static final boolean ENABLE_DEBUG_LOGGING = true;
public static final boolean ENABLE_PERFORMANCE_LOGGING = true;
public static final boolean LOG_API_REQUESTS = true; // Careful with sensitive data
public static final boolean LOG_API_RESPONSES = true; // Careful with sensitive data
```

#### Debug Output Examples
```bash
# Input validation debug
DEBUG [IntelliMapSmartService] Input validation passed: customerData size=1.2KB

# Rate limiting debug  
DEBUG [RateLimiter] Current rate: 45 requests/minute, 890 requests/hour

# API request debug
DEBUG [IntelliMapSmartService] Sending request to Azure OpenAI: endpoint=https://resource.openai.azure.com

# Response processing debug
DEBUG [IntelliMapSmartService] Received response: size=2.1KB, processingTime=1.2s

# Confidence calculation debug
DEBUG [IntelliMapSmartService] Confidence scores: field1=95%, field2=87%, overall=91%
```

### Monitoring and Alerting

#### Key Metrics to Monitor
```javascript
// Success rate (should be > 95%)
successRate = successfulRequests / totalRequests

// Average response time (should be < 5s)
avgResponseTime = totalResponseTime / requestCount  

// Error rate by category
authErrors, networkErrors, parsingErrors, rateLimitErrors

// Confidence score distribution
avgConfidence, lowConfidenceAlerts (<70%)

// Resource utilization
memoryUsage, cpuUsage, diskUsage
```

#### Alert Conditions
```javascript
// Critical alerts
- Success rate < 90% for 5+ minutes
- Average response time > 10s for 5+ minutes  
- Authentication errors > 5% of requests

// Warning alerts  
- Success rate < 95% for 10+ minutes
- Average confidence < 80% for 30+ minutes
- Rate limit utilization > 80%
```

### Performance Tuning

#### Optimal Configuration
```java
// Balanced performance configuration
public static final int MAX_REQUESTS_PER_MINUTE = 45;
public static final int MAX_REQUESTS_PER_HOUR = 800;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(45);
public static final int MAX_RETRIES = 2;
public static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(1500);
```

#### Load Testing Recommendations
```bash
# Test with realistic data volumes
# Simulate concurrent users (5-10 simultaneous requests)
# Monitor resource usage during peak loads
# Test error scenarios and recovery
# Validate timeout and retry behavior
```

## 🚀 Deployment Guide

### Pre-Deployment Checklist

#### Environment Prerequisites
- [ ] Appian 23.1+ environment with plug-in support
- [ ] Azure OpenAI service provisioned and configured
- [ ] GPT-4 or GPT-3.5-turbo model deployed
- [ ] Network connectivity to Azure OpenAI endpoints
- [ ] Sufficient API quota allocated

#### Required Files and Dependencies
- [ ] `intellimapsmartservice-v4.0.0.jar` (compiled plug-in)
- [ ] `jackson-core-2.15.2.jar`
- [ ] `jackson-databind-2.15.2.jar`
- [ ] `jackson-annotations-2.15.2.jar`
- [ ] `appian-plugin.xml` (properly configured)

#### Configuration Constants
- [ ] `AZURE_OPENAI_ENDPOINT` constant created
- [ ] `AZURE_OPENAI_KEY` constant created (encrypted)
- [ ] `AZURE_OPENAI_DEPLOYMENT` constant created
- [ ] `AZURE_OPENAI_API_VERSION` constant created
- [ ] Optional: Rate limiting and timeout constants

### Deployment Steps

#### Step 1: Prepare the Environment
```bash
# 1. Create deployment directory
mkdir intellimap-deployment
cd intellimap-deployment

# 2. Download required JAR files
wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar
wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar
wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar

# 3. Verify JAR integrity
sha256sum jackson-*.jar
```

#### Step 2: Build the Plug-in
```bash
# 1. Compile Java sources
javac -cp "lib/*:appian-plug-in-sdk.jar" src/com/lcm/plugins/intellimapsmartservice/*.java -d bin/

# 2. Copy resources
cp src/com/lcm/plugins/intellimapsmartservice/*.properties bin/com/lcm/plugins/intellimapsmartservice/
cp src/appian-plugin.xml bin/
cp -r src/META-INF bin/

# 3. Create JAR file
cd bin
jar -cvf ../intellimapsmartservice-v4.0.0.jar *
cd ..

# 4. Verify JAR contents
jar -tf intellimapsmartservice-v4.0.0.jar | head -20
```

#### Step 3: Configure Appian Constants
```javascript
// Navigate to: Admin Console > Constants > New Constant

// Azure OpenAI Configuration
cons!AZURE_OPENAI_ENDPOINT
- Name: "Azure OpenAI Endpoint"
- Value: "https://your-resource.openai.azure.com/"
- Description: "Azure OpenAI service endpoint URL"

cons!AZURE_OPENAI_KEY  
- Name: "Azure OpenAI API Key"
- Value: "your-32-character-api-key"
- Description: "Authentication key for Azure OpenAI"
- Security: Mark as "Encrypted" ✓

cons!AZURE_OPENAI_DEPLOYMENT
- Name: "Azure OpenAI Deployment Name"
- Value: "gpt-4-deployment"
- Description: "Name of the deployed model"

cons!AZURE_OPENAI_API_VERSION
- Name: "Azure OpenAI API Version"
- Value: "2023-05-15"
- Description: "API version for Azure OpenAI requests"
```

#### Step 4: Deploy the Plug-in
```bash
# 1. Access Appian Admin Console
# Navigate to: Admin Console > Plug-ins

# 2. Click "Install Plug-in"

# 3. Upload intellimapsmartservice-v4.0.0.jar

# 4. Review deployment summary:
#    - Smart Services: 1 (ApparelOrderMapper)
#    - Dependencies: 3 JAR files
#    - Security: Passed

# 5. Click "Install"

# 6. Verify installation
# Navigate to: Objects > Smart Services
# Confirm the service appears in the list
```

#### Step 5: Post-Deployment Verification
```javascript
// Create test process model with minimal smart service call
ApparelOrderMapper(
  inputRecords: {
    customer_style: "TEST-001",
    style_description: "Test Item"
  },
  azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
  azureOpenAIKey: cons!AZURE_OPENAI_KEY,
  azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
  azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
  targetFields: {"F2:Customer Style Number", "F5:Style Description"},
  userPrompt: "Map test data to standard fields"
)

// Execute and verify:
// - Process completes successfully
// - Mapped data is returned
// - Confidence score is reasonable (>0.7)
// - No errors in application logs
```

### Environment-Specific Configurations

#### Development Environment
```java
// IntelliMapConfig.java - Development settings
public static final boolean ENABLE_DEBUG_LOGGING = true;
public static final int MAX_REQUESTS_PER_MINUTE = 30;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
public static final boolean ENABLE_PERFORMANCE_LOGGING = true;
```

#### Testing Environment  
```java
// IntelliMapConfig.java - Testing settings
public static final boolean ENABLE_DEBUG_LOGGING = false;
public static final int MAX_REQUESTS_PER_MINUTE = 45;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(45);
public static final boolean ENABLE_PERFORMANCE_LOGGING = true;
```

#### Production Environment
```java
// IntelliMapConfig.java - Production settings
public static final boolean ENABLE_DEBUG_LOGGING = false;
public static final int MAX_REQUESTS_PER_MINUTE = 60;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
public static final boolean ENABLE_PERFORMANCE_LOGGING = false;
```

### Migration from Previous Versions

#### From v3.x to v4.0.0
```bash
# Breaking changes:
# 1. Jackson JSON library integration - requires new JAR dependencies
# 2. Configuration centralization - some parameters moved to config class
# 3. Enhanced error handling - new error codes and messages

# Migration steps:
# 1. Add Jackson JAR files to deployment
# 2. Update process models to handle new error format
# 3. Review custom prompt configurations
# 4. Test with existing data to ensure compatibility
```

#### Rollback Procedures
```bash
# If deployment fails or issues arise:

# 1. Immediate rollback
# - Admin Console > Plug-ins > IntelliMap > Uninstall
# - Reinstall previous version

# 2. Process model updates
# - Revert any process model changes
# - Update constants to previous format if needed

# 3. Monitoring
# - Monitor system performance after rollback
# - Verify existing functionality works correctly
```

### Production Monitoring Setup

#### Application Logs
```bash
# Monitor these log locations in Appian:
# - Application Server logs: /appian/logs/application-server.log
# - Plug-in logs: search for "intellimapsmartservice"
# - Error logs: search for ERROR level messages

# Key log patterns to monitor:
grep "com.lcm.plugins.intellimapsmartservice" application-server.log
grep "ERROR.*IntelliMap" application-server.log  
grep "Rate limit exceeded" application-server.log
```

#### Performance Dashboards
```javascript
// Create Appian reports/dashboards to monitor:
// - Smart service execution frequency
// - Average processing times  
// - Success/failure rates
// - Confidence score trends
// - Error categorization

// Example queries for process reporting:
// - Count of IntelliMap executions per day
// - Average execution time by data size
// - Distribution of confidence scores
```

## 📋 Version History

### v4.0.0 (Current - 2024)
#### 📚 Documentation Updates
- **README Accuracy**: Updated README to accurately reflect single smart service implementation
- **Corrected Examples**: Fixed all code examples to use correct service name and parameters
- **Appian Version**: Updated minimum Appian requirement to 24.2+

#### 🎉 Major Features
- **Apparel Order Mapper**: New specialized smart service for apparel industry
- **Configurable Target Fields**: Support for custom field mappings
- **Enhanced API Integration**: Updated Azure OpenAI API compatibility
- **Performance Improvements**: Optimized processing and memory usage

#### 🔧 Technical Improvements
- **Jackson JSON Library**: Robust JSON processing with error recovery
- **Internationalization (i18n)**: Multi-language support with resource bundles
- **Centralized Configuration**: Environment-specific configuration management
- **Advanced Rate Limiting**: Sliding window algorithm with thread safety
- **Comprehensive Logging**: Structured logging with performance metrics

#### 🛡️ Security & Reliability
- **Enhanced Input Validation**: Comprehensive sanitization and validation
- **Retry Logic**: Exponential backoff with intelligent failure handling
- **Error Recovery**: Graceful degradation and fallback mechanisms
- **Audit Trails**: Complete tracking of mapping operations

#### 📚 Documentation
- **Complete API Reference**: Detailed parameter and response documentation
- **Architecture Guide**: System design and component overview
- **Troubleshooting**: Comprehensive issue resolution guide
- **Deployment Guide**: Step-by-step production deployment

### v3.2.1 (2023)
#### 🐛 Bug Fixes
- Fixed memory leak in HTTP client connections
- Resolved JSON parsing edge cases
- Improved error message clarity
- Fixed rate limiting synchronization issues

#### 🔧 Improvements
- Enhanced logging detail
- Improved API timeout handling
- Better exception propagation
- Performance optimizations

### v3.2.0 (2023)
#### ✨ New Features
- **Rate Limiting**: Basic rate limiting implementation
- **Retry Logic**: Simple retry mechanism for failed requests
- **Configuration Options**: Basic configuration management
- **Enhanced Logging**: Improved logging capabilities

#### 🔧 Improvements
- Better error handling
- Improved API response parsing
- Enhanced input validation
- Performance optimizations

### v3.1.0 (2023)
#### ✨ New Features
- **Custom Prompts**: Support for user-defined mapping instructions
- **Confidence Scoring**: Basic confidence calculation
- **Improved Error Handling**: Better exception management

#### 🐛 Bug Fixes
- Fixed JSON parsing issues
- Resolved timeout problems
- Improved null handling

### v3.0.0 (2023)
#### 🎉 Major Release
- **Azure OpenAI Integration**: Full Azure OpenAI service support
- **Smart Service Architecture**: Appian plug-in framework implementation
- **Basic Data Mapping**: Core intelligent mapping functionality

#### 🔧 Technical Foundation
- Java-based smart service implementation
- HTTP client for API communication
- Basic JSON processing
- Fundamental error handling

### v2.x Series (2022-2023)
#### Legacy Features
- Prototype implementations
- Basic AI integration experiments
- Early data mapping concepts

### v1.x Series (2022)
#### Initial Development
- Proof of concept
- Basic framework setup
- Initial research and development

## 🤝 Contributing

We welcome contributions to the IntelliMap Smart Service project! Whether you're reporting bugs, suggesting features, or contributing code, your involvement helps make this tool better for everyone.

### 📋 Contribution Guidelines

#### Code of Conduct
- Be respectful and inclusive
- Focus on constructive feedback
- Help maintain a welcoming environment
- Follow professional communication standards

#### Types of Contributions
- 🐛 **Bug Reports**: Help us identify and fix issues
- 💡 **Feature Requests**: Suggest new capabilities
- 📚 **Documentation**: Improve guides and examples  
- 🔧 **Code Contributions**: Bug fixes and enhancements
- 🧪 **Testing**: Add test cases and validation scenarios

### 🚀 Development Setup

#### Prerequisites
```bash
# Required software
- Java 8+ JDK
- Maven 3.6+
- Git
- IDE with Java support (IntelliJ, Eclipse, VSCode)

# Appian development environment
- Appian 23.1+ 
- Appian Plug-in SDK
- Local Appian instance (optional)
```

#### Local Development
```bash
# 1. Clone the repository
git clone https://github.com/your-org/intellimapsmartservice.git
cd intellimapsmartservice

# 2. Set up development environment
mkdir -p lib
# Download Appian SDK and Jackson JARs to lib/

# 3. Build the project
mvn clean compile

# 4. Run tests
mvn test

# 5. Create plug-in JAR
mvn package
```

### 📝 Submitting Issues

#### Bug Reports
When reporting bugs, please include:

```markdown
## Bug Description
Clear description of the issue

## Steps to Reproduce
1. Step one
2. Step two
3. Step three

## Expected Behavior
What should have happened

## Actual Behavior  
What actually happened

## Environment
- Appian Version: 
- Plugin Version:
- Java Version:
- Azure OpenAI Model:

## Additional Context
- Error logs
- Configuration details
- Sample data (anonymized)
```

#### Feature Requests
```markdown
## Feature Summary
Brief description of the requested feature

## Business Value
Why this feature would be valuable

## Proposed Solution
How you envision this working

## Alternatives Considered
Other approaches you've thought about

## Additional Context
Any other relevant information
```

### 💻 Code Contributions

#### Development Workflow
```bash
# 1. Create feature branch
git checkout -b feature/your-feature-name

# 2. Make your changes
# - Follow existing code style
# - Add appropriate comments
# - Include tests where applicable

# 3. Test your changes
mvn clean test
# Test with local Appian environment if possible

# 4. Commit with clear message
git commit -m "Add: Detailed description of changes"

# 5. Push and create pull request
git push origin feature/your-feature-name
```

#### Code Standards
```java
// Follow these conventions:

// 1. Java naming conventions
public class MySmartService extends SmartService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySmartService.class);
    
    private String camelCaseVariable;
    
    public void methodNamesInCamelCase() {
        // Method implementation
    }
}

// 2. Documentation requirements
/**
 * Brief description of the method
 * 
 * @param parameterName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception occurs
 */
public String exampleMethod(String parameterName) throws ExceptionType {
    // Implementation
}

// 3. Error handling pattern
try {
    // Operation that might fail
} catch (SpecificException e) {
    LOGGER.error("Specific error message", e);
    throw new SmartServiceException("User-friendly message", e);
}
```

#### Testing Requirements
```java
// Include unit tests for new functionality
@Test
public void testNewFeature() {
    // Arrange
    MySmartService service = new MySmartService();
    String testInput = "test data";
    
    // Act
    String result = service.processData(testInput);
    
    // Assert
    assertNotNull(result);
    assertTrue(result.contains("expected content"));
}

// Include integration tests for API interactions
@Test
public void testAzureOpenAIIntegration() {
    // Test with mock Azure OpenAI responses
    // Verify error handling
    // Check rate limiting behavior
}
```

### 📋 Pull Request Process

#### Before Submitting
- [ ] Code follows project style guidelines
- [ ] Tests are added for new functionality
- [ ] Documentation is updated if needed
- [ ] Changes are tested locally
- [ ] Commit messages are clear and descriptive

#### Pull Request Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented)
```

#### Review Process
1. **Automated Checks**: CI/CD pipeline runs tests
2. **Code Review**: Team members review changes
3. **Discussion**: Address feedback and questions
4. **Approval**: Maintainer approval required
5. **Merge**: Changes integrated into main branch

### 🎯 Development Priorities

#### Current Focus Areas
- **Performance Optimization**: Improve processing speed and memory usage
- **Error Handling**: Enhanced error recovery and user feedback
- **Documentation**: Expand examples and troubleshooting guides
- **Testing Coverage**: Increase automated test coverage
- **Security**: Security auditing and improvements

#### Future Roadmap
- **Additional AI Models**: Support for other AI services
- **Batch Processing**: Efficient handling of large datasets
- **Real-time Processing**: Streaming data support
- **Advanced Analytics**: Enhanced confidence scoring and metrics
- **Cloud Deployment**: Containerization and cloud-native features

### 📞 Community

#### Getting Help
- **GitHub Issues**: For bug reports and feature requests
- **Discussions**: For questions and general discussion
- **Email**: For security issues or private inquiries
- **Documentation**: Comprehensive guides and examples

#### Stay Connected
- ⭐ **Star the Repository**: Show your support
- 👀 **Watch for Updates**: Get notified of new releases
- 🍴 **Fork and Experiment**: Try your own modifications
- 💬 **Join Discussions**: Share experiences and ideas

### 🙏 Recognition

#### Contributors
We recognize and appreciate all contributors to this project. Contributors are acknowledged in:
- GitHub contributor graphs
- Release notes for significant contributions  
- Project documentation
- Community showcases

#### Attribution
When contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

---

**Thank you for contributing to IntelliMap Smart Service!** 🎉

Your contributions help create better tools for the entire Appian community.

## 📞 Support

### 🆘 Getting Help

#### Quick Support Options
- 📖 **Documentation**: Check this README and inline code documentation
- 💬 **GitHub Issues**: Report bugs or request features
- 📧 **Email Support**: For urgent production issues
- 🔍 **Search**: Check existing issues for similar problems

#### Response Times
- **Critical Production Issues**: Within 4 hours
- **Bug Reports**: Within 2 business days  
- **Feature Requests**: Within 1 week
- **General Questions**: Within 3 business days

### 📚 Resources

#### Official Documentation
- **[Appian Documentation](https://docs.appian.com/)**: Appian platform guides
- **[Azure OpenAI Documentation](https://docs.microsoft.com/azure/cognitive-services/openai/)**: Azure OpenAI service docs
- **[Jackson Documentation](https://github.com/FasterXML/jackson-docs)**: JSON processing library docs

#### Community Resources
- **Appian Community**: [community.appian.com](https://community.appian.com)
- **Azure AI Community**: Microsoft Azure AI forums
- **Stack Overflow**: Tag questions with `appian` and `azure-openai`

#### Training Materials
- **IntelliMap Getting Started Guide**: Step-by-step tutorial
- **Best Practices Guide**: Production deployment recommendations
- **Video Tutorials**: Visual walkthroughs of common scenarios
- **Sample Applications**: Complete working examples

### 🎓 Professional Services

#### Implementation Support
- **Architecture Review**: Design validation and optimization
- **Custom Development**: Tailored features and integrations  
- **Migration Services**: Upgrade assistance and data migration
- **Performance Tuning**: Optimization for high-volume usage

#### Training and Consulting
- **Developer Training**: Hands-on workshops for development teams
- **Administrator Training**: Configuration and maintenance training
- **Best Practices Consulting**: Industry-specific implementation guidance
- **Code Review Services**: Expert review of custom implementations

### 🚨 Support Escalation

#### Priority Levels

**🔴 Critical (P0)**
- Production system down
- Data loss or corruption
- Security vulnerabilities
- **Response**: Within 2 hours

**🟠 High (P1)**  
- Major functionality broken
- Performance severely degraded
- Multiple users affected
- **Response**: Within 8 hours

**🟡 Medium (P2)**
- Feature not working as expected
- Minor performance issues
- Single user affected
- **Response**: Within 2 business days

**🟢 Low (P3)**
- Documentation issues
- Feature requests
- General questions
- **Response**: Within 1 week

#### Escalation Process
```bash
# 1. Create GitHub Issue (for non-critical issues)
# Include: Environment, error logs, steps to reproduce

# 2. Email Support (for critical issues)
# Subject: [CRITICAL] IntelliMap Issue - Brief Description
# Include: All relevant technical details

# 3. Emergency Contact (for production down scenarios)
# Follow your organization's emergency procedures
# Include: Impact assessment and business justification
```

### 📋 Support Information Template

When requesting support, please provide:

```markdown
## Environment Information
- **Appian Version**: 
- **Plugin Version**: 
- **Java Version**: 
- **Operating System**: 
- **Azure OpenAI Model**: 

## Issue Details
- **Issue Type**: Bug/Question/Feature Request
- **Priority Level**: Critical/High/Medium/Low
- **Affected Users**: Number of users impacted
- **Business Impact**: Description of business impact

## Technical Details
- **Error Messages**: Copy exact error messages
- **Log Entries**: Relevant log file entries
- **Configuration**: Anonymized configuration details
- **Data Samples**: Anonymized sample data (if relevant)

## Steps to Reproduce
1. Step one
2. Step two  
3. Step three

## Expected vs Actual Behavior
- **Expected**: What should happen
- **Actual**: What actually happens

## Troubleshooting Attempted
- [ ] Checked documentation
- [ ] Searched existing issues
- [ ] Verified configuration
- [ ] Tested with sample data
- [ ] Checked Azure OpenAI service status
```

### 🔧 Self-Service Tools

#### Diagnostic Commands
```javascript
// Health check function for Appian
a!localVariables(
  local!testResult: ApparelOrderMapper(
    inputRecords: { test_field: "connectivity" },
    azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
    azureOpenAIKey: cons!AZURE_OPENAI_KEY,
    azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
    azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
    targetFields: {"F2:Customer Style Number"},
    userPrompt: "Test connectivity"
  ),
  if(
    isnull(local!testResult.error),
    "✅ ApparelOrderMapper service is functioning correctly",
    "❌ Error: " & local!testResult.error
  )
)
```

#### Configuration Validator
```javascript
// Validate configuration constants
a!localVariables(
  local!checks: {
    { 
      name: "Azure OpenAI Endpoint",
      valid: not(isnull(cons!AZURE_OPENAI_ENDPOINT)) and contains(cons!AZURE_OPENAI_ENDPOINT, "openai.azure.com"),
      value: cons!AZURE_OPENAI_ENDPOINT
    },
    {
      name: "Azure OpenAI Key", 
      valid: not(isnull(cons!AZURE_OPENAI_KEY)) and len(cons!AZURE_OPENAI_KEY) = 32,
      value: "[CONFIGURED]"
    },
    {
      name: "Deployment Name",
      valid: not(isnull(cons!AZURE_OPENAI_DEPLOYMENT)) and len(cons!AZURE_OPENAI_DEPLOYMENT) > 0,
      value: cons!AZURE_OPENAI_DEPLOYMENT
    }
  },
  a!forEach(
    items: local!checks,
    expression: if(
      fv!item.valid,
      "✅ " & fv!item.name & ": " & fv!item.value,
      "❌ " & fv!item.name & ": Invalid or missing"
    )
  )
)
```

#### Performance Monitor
```javascript
// Monitor ApparelOrderMapper performance
a!localVariables(
  local!startTime: now(),
  local!result: ApparelOrderMapper(
    inputRecords: /* your input records */,
    azureOpenAIEndpoint: cons!AZURE_OPENAI_ENDPOINT,
    azureOpenAIKey: cons!AZURE_OPENAI_KEY,
    azureOpenAIDeploymentName: cons!AZURE_OPENAI_DEPLOYMENT,
    azureOpenAIApiVersion: cons!AZURE_OPENAI_API_VERSION,
    targetFields: /* your target fields */,
    userPrompt: /* your prompt */
  ),
  local!endTime: now(),
  local!duration: local!endTime - local!startTime,
  {
    "Processing Time": local!duration & " seconds",  
    "Overall Confidence": local!result.overallConfidence,
    "Status": if(isnull(local!result.error), "Success", "Error"),
    "Data Size": len(tostring(local!result.mappedResult)) & " characters"
  }
)
```

### 📈 Service Level Agreement (SLA)

#### Availability Targets
- **Service Uptime**: 99.5% (excluding planned maintenance)
- **API Response Time**: < 5 seconds (95th percentile)
- **Error Rate**: < 1% of requests
- **Data Accuracy**: > 90% confidence scores

#### Maintenance Windows
- **Planned Maintenance**: First Saturday of each month, 2:00-4:00 AM UTC
- **Emergency Maintenance**: As needed with 2-hour advance notice
- **Notification**: Email and GitHub announcements

#### Support Coverage
- **Business Hours**: Monday-Friday, 8:00 AM - 6:00 PM EST
- **Emergency Support**: 24/7 for critical production issues
- **Holidays**: Limited support on major holidays

### 📞 Contact Information

#### Primary Support Channels
```bash
# GitHub Issues (Preferred)
https://github.com/your-org/intellimapsmartservice/issues

# Email Support  
intellimap-support@your-domain.com

# Emergency Hotline (Critical Issues Only)
+1-XXX-XXX-XXXX
```

#### Regional Support
- **Americas**: English, Spanish, Portuguese
- **EMEA**: English, French, German, Dutch
- **APAC**: English, Mandarin, Japanese

#### Business Contacts
```bash
# Sales and Licensing
sales@your-domain.com

# Partnership Opportunities  
partnerships@your-domain.com

# Security Issues
security@your-domain.com
```

---

**🎉 Thank you for choosing IntelliMap Smart Service!**

We're committed to providing you with the best possible support and ensuring your success with **apparel industry data mapping**. Don't hesitate to reach out whenever you need assistance with your apparel order processing workflows.

---

*© 2024 Your Organization. All rights reserved. IntelliMap Smart Service is licensed under the MIT License.*

[![GitHub](https://img.shields.io/badge/GitHub-Repository-black.svg)](https://github.com/your-org/intellimapsmartservice)
[![Documentation](https://img.shields.io/badge/Documentation-Latest-blue.svg)](https://your-org.github.io/intellimapsmartservice)
[![Support](https://img.shields.io/badge/Support-Available-green.svg)](mailto:intellimap-support@your-domain.com) 