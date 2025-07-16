package com.lcm.plugins.intellimapsmartservice;

import java.time.Duration;
import java.lang.String;

/**
 * Configuration class for IntelliMap Smart Service
 * Centralizes all configurable values
 */
public class IntelliMapConfig {

    // Azure OpenAI Configuration
    public static final String AZURE_OPENAI_API_VERSION = "2023-05-15";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    public static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(1000); // 1 second delay between calls

    // Request Configuration - Increased for larger responses
    public static final int MAX_TOKENS = 8192; // Increased from 1000 to 8192 (GPT-4 max)
    public static final double TEMPERATURE = 0.1;
    public static final int MAX_RETRIES = 3;

    // Chunking Configuration for Large Inputs
    public static final int MAX_INPUT_TOKENS_PER_CHUNK = 6000; // Leave room for system prompt
    public static final int MAX_INPUT_KEYS_PER_CHUNK = 50; // Maximum dictionary keys per chunk
    public static final int MAX_CHUNKS_PER_REQUEST = 5; // Maximum chunks to process in one request

    // Rate Limiting Configuration
    public static final int MAX_REQUESTS_PER_MINUTE = 60;
    public static final int MAX_REQUESTS_PER_HOUR = 1000;

    // JSON Configuration
    public static final String DEFAULT_ENCODING = "UTF-8";

    // Logging Configuration
    public static final String LOGGER_NAME = "com.lcm.intellimapsmartservice";
    public static final boolean ENABLE_DEBUG_LOGGING = false;

    // Error Handling Configuration
    public static final double DEFAULT_CONFIDENCE_SCORE = 0.5;
    public static final double FALLBACK_CONFIDENCE_SCORE = 0.3;

    // System Message for OpenAI
    public static final String SYSTEM_MESSAGE = "You are a data mapping assistant that extracts and maps customer information.";

    // HTTP Headers
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String API_KEY_HEADER = "api-key";

    // Response Keys
    public static final String MAPPED_DATA_KEY = "mappedData";
    public static final String CONFIDENCE_SCORE_KEY = "confidenceScore";
    public static final String CONTENT_KEY = "content";
    public static final String MESSAGES_KEY = "messages";
    public static final String ROLE_KEY = "role";
    public static final String USER_ROLE = "user";
    public static final String SYSTEM_ROLE = "system";
}