package com.lcm.plugins.intellimapsmartservice;

import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.appiancorp.suiteapi.type.TypedValue;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

@PaletteInfo(paletteCategory = "Map Tools", palette = "IntelliMap")
public class IntelliMap extends AppianSmartService {

    // Input parameters
    private TypedValue inputRecords;
    private String azureOpenAIEndpoint;
    private String azureOpenAIKey;
    private String azureOpenAIDeploymentName;
    private String azureOpenAIApiVersion;
    private TypedValue targetFields;
    private String userPrompt;

    // Output parameters
    private String mappedResult;
    private Double overallConfidence;

    // JSON parsing and utilities
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RateLimiter rateLimiter = new RateLimiter();

    // Client identifier for rate limiting
    private String clientId;

    // Setters for input parameters
    @Input(required = Required.ALWAYS)
    public void setInputRecords(TypedValue inputRecords) {
        this.inputRecords = inputRecords;
    }

    @Input(required = Required.ALWAYS)
    public void setAzureOpenAIEndpoint(String azureOpenAIEndpoint) {
        this.azureOpenAIEndpoint = azureOpenAIEndpoint;
    }

    @Input(required = Required.ALWAYS)
    public void setAzureOpenAIKey(String azureOpenAIKey) {
        this.azureOpenAIKey = azureOpenAIKey;
    }

    @Input(required = Required.ALWAYS)
    public void setAzureOpenAIDeploymentName(String azureOpenAIDeploymentName) {
        this.azureOpenAIDeploymentName = azureOpenAIDeploymentName;
    }

    @Input(required = Required.ALWAYS)
    public void setAzureOpenAIApiVersion(String azureOpenAIApiVersion) {
        this.azureOpenAIApiVersion = azureOpenAIApiVersion;
    }

    @Input(required = Required.ALWAYS)
    public void setTargetFields(TypedValue targetFields) {
        this.targetFields = targetFields;
    }

    @Input(required = Required.ALWAYS)
    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    // Getters for output parameters
    public String getMappedResult() {
        return mappedResult;
    }

    public Double getOverallConfidence() {
        return overallConfidence;
    }

    @Override
    public void run() throws SmartServiceException {
        try {
            // Initialize client ID for rate limiting
            clientId = generateClientId();

            // Validate required inputs
            validateInputs();

            // Process multiple records
            processMultipleRecords();

        } catch (SmartServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error processing data mapping: " + e.getMessage());
        }
    }

    private boolean needsChunking(String inputData) {
        // Estimate tokens (rough approximation: 1 token ≈ 4 characters)
        int estimatedTokens = inputData.length() / 4;
        return estimatedTokens > IntelliMapConfig.MAX_INPUT_TOKENS_PER_CHUNK;
    }

    private void processChunkedInput(String inputData) throws SmartServiceException {
        try {
            // Parse input data to get dictionary entries
            Map<String, Object> inputMap = parseInputToMap(inputData);
            List<String> inputKeys = new ArrayList<>(inputMap.keySet());

            // Split into chunks
            List<List<String>> chunks = createChunks(inputKeys, IntelliMapConfig.MAX_INPUT_KEYS_PER_CHUNK);

            // Process each chunk
            List<String> chunkResults = new ArrayList<>();
            for (int i = 0; i < Math.min(chunks.size(), IntelliMapConfig.MAX_CHUNKS_PER_REQUEST); i++) {
                List<String> chunk = chunks.get(i);
                Map<String, Object> chunkData = createChunkData(inputMap, chunk);
                String chunkJson = convertMapToJsonString(chunkData);

                // Check rate limits before making API call
                rateLimiter.checkRateLimit(clientId);

                // Process chunk
                String chunkResult = callAzureOpenAIWithRetry(chunkJson);
                chunkResults.add(chunkResult);
            }

            // Merge results from all chunks
            mergeChunkResults(chunkResults);

        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error processing chunked input: " + e.getMessage());
        }
    }

    private void processSingleRequest(String inputData) throws SmartServiceException {
        try {
            // Check rate limits before making API call
            rateLimiter.checkRateLimit(clientId);

            // Call Azure OpenAI with retry logic
            String openAIResponse = callAzureOpenAIWithRetry(inputData);

            // Parse the response using proper JSON parsing
            parseOpenAIResponseWithJackson(openAIResponse);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Processing interrupted during rate limiting");
        }
    }

    private Map<String, Object> parseInputToMap(String inputData) throws SmartServiceException {
        try {
            // Try to parse as JSON first, but with better validation
            if (inputData.trim().startsWith("{")) {
                // Additional validation to ensure it's actually valid JSON before parsing
                if (isValidJsonFormat(inputData.trim())) {
                    try {
                        return objectMapper.readValue(inputData, Map.class);
                    } catch (JsonProcessingException e) {
                        System.err.println("JSON parsing failed despite initial validation: " + e.getMessage());
                        // Fall through to custom parsing
                    }
                } else {
                    System.out.println("Input starts with '{' but doesn't appear to be valid JSON format");
                }
            }

            // Handle non-JSON format (fallback)
            Map<String, Object> result = new HashMap<>();
            String[] lines = inputData.split("\n");
            for (String line : lines) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        result.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error parsing input data: " + e.getMessage());
        }
    }

    /**
     * Determines if the input string is in Appian Dictionary format
     * Appian Dictionary format has patterns like: [field:value,field2:value2]
     * This detection is based on STRUCTURE, not specific field names
     */
    private boolean isAppianDictionaryFormat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // Must start with [ for both formats, but we need to distinguish
        if (!trimmed.startsWith("[")) {
            return false;
        }

        // Look for Appian Dictionary structural patterns:

        // 1. Multiple records separated by ]; [ - this is definitely Appian Dictionary
        if (trimmed.contains("]; [")) {
            return true;
        }

        // 2. Alternative separators like ] [ or ],[
        if (trimmed.contains("] [") || trimmed.contains("],[")) {
            return true;
        }

        // 3. Look for unquoted field names followed by colons (typical of Appian
        // Dictionary)
        // JSON would have quoted field names like "fieldName":
        // This regex looks for: [optionally preceded by *] word characters [possibly
        // with spaces] followed by colon
        if (trimmed.matches(".*\\[\\*?[A-Za-z][A-Za-z0-9\\s_-]*:.*")) {
            return true;
        }

        // 4. Look for the general pattern of unquoted identifiers followed by colons
        // This catches patterns like [SomeFieldName:value, AnotherField:value2]
        if (trimmed.matches(".*\\[[^\\[\\]\"]*[A-Za-z][^\\[\\]\"]*:.*")) {
            return true;
        }

        // 5. If it looks like JSON array (starts with [ but has quoted strings and
        // proper JSON structure)
        if (trimmed.matches("^\\[\\s*\\{.*") || trimmed.matches("^\\[\\s*\".*")) {
            // Looks like JSON array
            return false;
        }

        // 6. Check for comma-separated unquoted field:value pairs within brackets
        // This pattern: [field1:value1,field2:value2,field3:value3]
        if (trimmed.matches("^\\[[^\\[\\]]*:[^\\[\\]]*(?:,[^\\[\\]]*:[^\\[\\]]*)*\\]$")) {
            return true;
        }

        // Default to false if we can't determine
        return false;
    }

    /**
     * Validates if a string is likely to be valid JSON format
     * This helps avoid passing non-JSON strings to the JSON parser
     */
    private boolean isValidJsonFormat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // Basic structural checks for JSON
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            // Check if it contains basic JSON structure indicators
            // Must have at least one colon (for key-value pairs)
            if (!trimmed.contains(":")) {
                return false;
            }

            // Enhanced validation to catch common "Prod" token issues
            // Check for unquoted words that might cause parsing errors
            if (containsUnquotedProblemTokens(trimmed)) {
                System.err.println("Input contains unquoted tokens that will cause JSON parsing errors");
                return false;
            }

            // Quick check to see if it has balanced braces
            int braceCount = 0;
            boolean inQuotes = false;
            char prevChar = ' ';

            for (char c : trimmed.toCharArray()) {
                if (c == '"' && prevChar != '\\') {
                    inQuotes = !inQuotes;
                } else if (!inQuotes) {
                    if (c == '{')
                        braceCount++;
                    else if (c == '}')
                        braceCount--;
                }
                prevChar = c;
            }

            return braceCount == 0;
        } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            // JSON array validation

            // Enhanced validation for arrays containing problem tokens
            if (containsUnquotedProblemTokens(trimmed)) {
                System.err.println("Array input contains unquoted tokens that will cause JSON parsing errors");
                return false;
            }

            int bracketCount = 0;
            boolean inQuotes = false;
            char prevChar = ' ';

            for (char c : trimmed.toCharArray()) {
                if (c == '"' && prevChar != '\\') {
                    inQuotes = !inQuotes;
                } else if (!inQuotes) {
                    if (c == '[')
                        bracketCount++;
                    else if (c == ']')
                        bracketCount--;
                }
                prevChar = c;
            }

            return bracketCount == 0;
        }

        return false;
    }

    /**
     * Enhanced method to detect unquoted problematic tokens that cause JSON parsing
     * errors
     */
    private boolean containsUnquotedProblemTokens(String input) {
        // Common problematic tokens that cause "Unrecognized token" errors
        String[] problemTokens = {
                "Prod", "Product", "Production", "Prototype", "Professional",
                "Process", "Program", "Project", "Property", "Provider"
        };

        // Remove all quoted strings first to check only unquoted content
        String unquotedContent = removeQuotedStrings(input);

        for (String token : problemTokens) {
            // Check if the token appears outside of quotes
            if (unquotedContent.contains(token)) {
                System.err.println("Found potentially problematic unquoted token: " + token);
                return true;
            }
        }

        return false;
    }

    /**
     * Helper method to remove quoted strings from input for analysis
     */
    private String removeQuotedStrings(String input) {
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        char prevChar = ' ';

        for (char c : input.toCharArray()) {
            if (c == '"' && prevChar != '\\') {
                inQuotes = !inQuotes;
                result.append(c); // Keep the quote character
            } else if (!inQuotes) {
                result.append(c); // Only keep content outside quotes
            }
            prevChar = c;
        }

        return result.toString();
    }

    private List<List<String>> createChunks(List<String> inputKeys, int maxKeysPerChunk) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < inputKeys.size(); i += maxKeysPerChunk) {
            int end = Math.min(i + maxKeysPerChunk, inputKeys.size());
            chunks.add(inputKeys.subList(i, end));
        }
        return chunks;
    }

    private Map<String, Object> createChunkData(Map<String, Object> fullData, List<String> chunkKeys) {
        Map<String, Object> chunkData = new HashMap<>();
        for (String key : chunkKeys) {
            if (fullData.containsKey(key)) {
                chunkData.put(key, fullData.get(key));
            }
        }
        return chunkData;
    }

    private void mergeChunkResults(List<String> chunkResults) throws SmartServiceException {
        try {
            List<Map<String, Object>> allFields = new ArrayList<>();

            for (String chunkResult : chunkResults) {
                // Parse each chunk result
                JsonNode responseNode = objectMapper.readTree(chunkResult);
                String content = responseNode.path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText();

                if (content != null && !content.trim().isEmpty()) {
                    // Try to extract result array from content
                    try {
                        JsonNode contentNode = objectMapper.readTree(content);

                        // Check if content is already an array (direct format)
                        if (contentNode.isArray()) {
                            for (JsonNode record : contentNode) {
                                Map<String, Object> recordMap = objectMapper.convertValue(record, Map.class);
                                allFields.add(recordMap);
                            }
                        } else {
                            // Look for the result array (fallback format)
                            JsonNode resultNode = contentNode.path("result");
                            if (!resultNode.isMissingNode() && resultNode.isArray()) {
                                for (JsonNode record : resultNode) {
                                    Map<String, Object> recordMap = objectMapper.convertValue(record, Map.class);
                                    allFields.add(recordMap);
                                }
                            }
                        }
                    } catch (JsonProcessingException e) {
                        // Skip malformed chunk result
                    }
                }
            }

            // Create final merged result - return records directly as array
            this.mappedResult = objectMapper.writeValueAsString(allFields);
            this.overallConfidence = calculateOverallConfidence(this.mappedResult);

        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error merging chunk results: " + e.getMessage());
        }
    }

    private void processMultipleRecords() throws SmartServiceException {
        // Declare variables outside try block so they're accessible in catch block
        List<Map<String, Object>> records = null;
        int processedRecords = 0;

        try {
            // Get the input records
            Object value = inputRecords.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Input records value is null");
            }

            // Log the input type for debugging
            System.out.println("Input type: " + value.getClass().getSimpleName());
            if (value instanceof String) {
                String strValue = (String) value;
                System.out.println("Input string length: " + strValue.length());
                System.out.println("Input string preview: "
                        + (strValue.length() > 100 ? strValue.substring(0, 100) + "..." : strValue));

                // Check if this looks like an Appian Dictionary format
                if (strValue.startsWith("[*") && strValue.contains(":*")) {
                    System.out.println("Detected potential Appian Dictionary format");
                }
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                System.out.println("Input is a Map with " + mapValue.size() + " entries");
                System.out.println("Map keys: " + mapValue.keySet());
            }

            records = new ArrayList<>();

            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> recordList = (List<Object>) value;
                for (Object record : recordList) {
                    if (record instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> recordMap = (Map<String, Object>) record;
                        records.add(recordMap);
                    }
                }
            } else if (value instanceof Map) {
                // Single record case
                @SuppressWarnings("unchecked")
                Map<String, Object> recordMap = (Map<String, Object>) value;
                records.add(recordMap);
            } else if (value instanceof String) {
                // Handle string input (JSON or custom format)
                String inputString = (String) value;
                try {
                    // Trim the string and check if it's empty
                    inputString = inputString.trim();
                    if (inputString.isEmpty()) {
                        throw new SmartServiceException(
                                IntelliMap.class,
                                null,
                                "Input string is empty");
                    }

                    // Check if it looks like Appian Dictionary format
                    // Appian Dictionary can start with [* or just [ followed by field names
                    if (inputString.startsWith("[*") || isAppianDictionaryFormat(inputString)) {
                        // Try to parse as Appian Dictionary format
                        System.out.println("Parsing Appian Dictionary format");
                        System.out.println("Input format preview: "
                                + (inputString.length() > 200 ? inputString.substring(0, 200) + "..." : inputString));

                        // Always try to parse multiple records first for Appian Dictionary format
                        List<Map<String, Object>> multipleRecords = parseMultipleRecordsFromAppianFormat(
                                inputString);
                        if (!multipleRecords.isEmpty()) {
                            records.addAll(multipleRecords);
                            System.out.println("Successfully parsed " + multipleRecords.size()
                                    + " records from Appian Dictionary format");
                        } else {
                            // Fall back to single record parsing
                            Map<String, Object> recordMap = parseCustomDelimitedFormat(inputString);
                            records.add(recordMap);
                            System.out.println("Successfully parsed 1 record from Appian Dictionary format");
                        }
                    } else if (inputString.startsWith("{")
                            || (inputString.startsWith("[") && !isAppianDictionaryFormat(inputString))) {
                        // Try to parse as JSON (but not Appian Dictionary format)
                        try {
                            // Add validation before attempting JSON parsing
                            if (!isValidJsonFormat(inputString)) {
                                System.err.println(
                                        "Input appears to start with JSON markers but is not valid JSON format");
                                throw new SmartServiceException(
                                        IntelliMap.class,
                                        null,
                                        "Input appears to be malformed JSON. If your data starts with words like 'Prod', 'Product', etc., it should be formatted as Appian Dictionary format with [*field:value] structure.");
                            }

                            JsonNode jsonNode = objectMapper.readTree(inputString);
                            if (jsonNode.isArray()) {
                                // It's a JSON array of records
                                System.out.println("Parsing JSON array with " + jsonNode.size() + " elements");
                                for (JsonNode node : jsonNode) {
                                    if (node.isObject()) {
                                        Map<String, Object> recordMap = objectMapper.convertValue(node, Map.class);
                                        records.add(recordMap);
                                    } else {
                                        System.err.println("Skipping non-object element in JSON array");
                                    }
                                }
                            } else if (jsonNode.isObject()) {
                                // It's a single JSON object
                                System.out.println("Parsing single JSON object");
                                Map<String, Object> recordMap = objectMapper.convertValue(jsonNode, Map.class);
                                records.add(recordMap);
                            } else {
                                throw new SmartServiceException(
                                        IntelliMap.class,
                                        null,
                                        "JSON string must contain an object or array, got: " + jsonNode.getNodeType());
                            }
                            System.out.println("Successfully parsed " + records.size() + " records from JSON string");
                        } catch (JsonProcessingException e) {
                            // Provide specific guidance for the 'Prod' token error
                            String errorMsg = e.getMessage();
                            if (errorMsg != null && errorMsg.contains("Unrecognized token 'Prod'")) {
                                throw new SmartServiceException(
                                        IntelliMap.class,
                                        e,
                                        "JSON parsing failed due to unrecognized token 'Prod'. This typically means your input data contains text starting with 'Prod' (like 'Production', 'Product') but is not properly formatted as JSON. Please use Appian Dictionary format [*field:value] or ensure your JSON is properly quoted.");
                            } else {
                                throw new SmartServiceException(
                                        IntelliMap.class,
                                        e,
                                        "JSON parsing failed: " + errorMsg
                                                + ". Please check that your input is valid JSON format.");
                            }
                        }
                    } else {
                        // Try to parse as other custom delimited format
                        System.out.println("Parsing other custom delimited format");
                        System.out.println("Input format preview: "
                                + (inputString.length() > 200 ? inputString.substring(0, 200) + "..." : inputString));
                        Map<String, Object> recordMap = parseCustomDelimitedFormat(inputString);
                        records.add(recordMap);
                        System.out.println("Successfully parsed 1 record from custom delimited format");
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    if (errorMsg == null) {
                        errorMsg = e.getClass().getSimpleName() + " occurred";
                    }
                    throw new SmartServiceException(
                            IntelliMap.class,
                            e,
                            "Error parsing string input: " + errorMsg);
                }
            } else {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Input must be a list of records, a single record, or a JSON string, got: "
                                + value.getClass().getSimpleName());
            }

            // Process each record
            List<Map<String, Object>> allResults = new ArrayList<>();
            double totalConfidence = 0.0;
            int currentRecordIndex = 0;

            for (Map<String, Object> record : records) {
                currentRecordIndex++;
                try {
                    if (record == null) {
                        System.err.println("Skipping null record " + currentRecordIndex);
                        continue;
                    }

                    // Convert record to JSON string
                    String recordJson = convertMapToJsonString(record);
                    if (recordJson == null || recordJson.trim().isEmpty()) {
                        System.err.println("Skipping record " + currentRecordIndex + " with empty JSON");
                        continue;
                    }

                    // Process single record
                    System.out.println("Processing record " + currentRecordIndex + " of " + records.size());
                    String openAIResponse = callAzureOpenAIWithRetry(recordJson);
                    if (openAIResponse == null || openAIResponse.trim().isEmpty()) {
                        System.err.println("Received null or empty response from OpenAI for record");
                        continue;
                    }

                    // Parse the response
                    JsonNode responseNode = null;
                    try {
                        responseNode = objectMapper.readTree(openAIResponse);
                    } catch (Exception e) {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("Unrecognized token 'Prod'")) {
                            System.err.println(
                                    "OpenAI response contains unrecognized 'Prod' token. This usually means the AI returned malformed JSON. Response preview: "
                                            +
                                            (openAIResponse.length() > 300 ? openAIResponse.substring(0, 300) + "..."
                                                    : openAIResponse));
                        } else {
                            System.err.println("Error parsing OpenAI response JSON: " + errorMsg);
                        }
                        System.err.println("Skipping this record due to malformed OpenAI response");
                        continue;
                    }

                    String content = null;
                    try {
                        content = responseNode.path("choices")
                                .path(0)
                                .path("message")
                                .path("content")
                                .asText();
                    } catch (Exception e) {
                        System.err.println("Error extracting content from response: " + e.getMessage());
                        continue;
                    }

                    if (content != null && !content.trim().isEmpty()) {
                        // Log the AI response content for debugging
                        System.out.println("AI Response Content Preview: " +
                                (content.length() > 200 ? content.substring(0, 200) + "..." : content));

                        // Extract result array from content
                        JsonNode contentNode = null;
                        try {
                            contentNode = objectMapper.readTree(content);
                        } catch (Exception e) {
                            String errorMsg = e.getMessage();
                            if (errorMsg != null && errorMsg.contains("Unrecognized token 'Prod'")) {
                                System.err.println(
                                        "AI returned content with unrecognized 'Prod' token. The AI response is not valid JSON.");
                                System.err.println("Content preview: "
                                        + (content.length() > 300 ? content.substring(0, 300) + "..." : content));
                                System.err.println(
                                        "This usually means the AI needs better instructions to return valid JSON format.");
                            } else {
                                System.err.println("Error parsing AI response content as JSON: " + errorMsg);
                            }
                            System.err.println("Skipping this record due to malformed AI response content");
                            continue;
                        }

                        // Try to find the result array - check both direct array format and wrapped in
                        // "result" field
                        JsonNode resultNode = null;
                        if (contentNode.isArray()) {
                            // Direct array format as specified in requirements
                            resultNode = contentNode;
                            System.out.println("Found direct array format with " + resultNode.size() + " elements");
                        } else {
                            // Check for wrapped format
                            resultNode = contentNode.path("result");
                            if (!resultNode.isMissingNode() && resultNode.isArray()) {
                                System.out.println("Found result array with " + resultNode.size() + " elements");
                            }
                        }

                        if (resultNode != null && resultNode.isArray()) {
                            for (JsonNode resultRecord : resultNode) {
                                if (resultRecord != null) {
                                    try {
                                        Map<String, Object> recordMap = objectMapper.convertValue(resultRecord,
                                                Map.class);
                                        if (recordMap != null && !recordMap.isEmpty()) {
                                            allResults.add(recordMap);
                                            System.out.println("Added record with " + recordMap.size() + " fields");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Error converting record to map: " + e.getMessage());
                                        // Continue with next record
                                    }
                                }
                            }

                            // Calculate confidence for this record
                            try {
                                double recordConfidence = calculateOverallConfidence(content);
                                totalConfidence += recordConfidence;
                                processedRecords++;
                            } catch (Exception e) {
                                System.err.println("Error calculating confidence: " + e.getMessage());
                                // Continue without confidence calculation
                            }
                        } else {
                            System.err.println("No valid result array found in response content");
                        }
                    }

                    // Rate limiting between records
                    if (records.size() > 1) {
                        try {
                            rateLimiter.checkRateLimit(clientId);
                        } catch (Exception e) {
                            System.err.println("Rate limiting error: " + e.getMessage());
                            // Continue without rate limiting
                        }
                    }

                } catch (Exception e) {
                    // Log error but continue processing other records
                    String errorMsg = e.getMessage();
                    if (errorMsg == null) {
                        errorMsg = e.getClass().getSimpleName() + " occurred";
                    }
                    System.err.println("Error processing record: " + errorMsg);
                    // Log the full stack trace for debugging
                    e.printStackTrace();
                }
            }

            // Create final result - return records directly as array
            try {
                System.out.println("Final results count: " + allResults.size());
                this.mappedResult = objectMapper.writeValueAsString(allResults);
                if (this.mappedResult == null) {
                    this.mappedResult = "[]";
                }
                System.out.println("Final mapped result length: " + this.mappedResult.length());
            } catch (Exception e) {
                System.err.println("Error creating final JSON result: " + e.getMessage());
                this.mappedResult = "[]";
            }

            this.overallConfidence = processedRecords > 0 ? totalConfidence / processedRecords : 0.0;

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }

            // Log the full exception for debugging
            System.err.println("Full exception in processMultipleRecords:");
            e.printStackTrace();

            // Provide more context about what might have failed
            String contextMessage = "Error processing multiple records";
            if (records != null) {
                contextMessage += " (processing " + records.size() + " records)";
            }
            if (processedRecords > 0) {
                contextMessage += " (successfully processed " + processedRecords + " records)";
            }

            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    contextMessage + ": " + errorMessage);
        }
    }

    private void validateInputs() throws SmartServiceException {
        if (inputRecords == null) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "Input records are required");
        }

        // Validate inputRecords has a value
        try {
            Object value = inputRecords.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Input records value is null");
            }
        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error accessing input records value: " + e.getMessage());
        }

        if (azureOpenAIEndpoint == null || azureOpenAIEndpoint.trim().isEmpty()) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "Azure OpenAI endpoint is required");
        }

        if (azureOpenAIKey == null || azureOpenAIKey.trim().isEmpty()) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "Azure OpenAI key is required");
        }

        if (azureOpenAIDeploymentName == null || azureOpenAIDeploymentName.trim().isEmpty()) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "Azure OpenAI deployment name is required");
        }

        if (azureOpenAIApiVersion == null || azureOpenAIApiVersion.trim().isEmpty()) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "Azure OpenAI API version is required");
        }

        if (targetFields == null) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "Target fields are required");
        }

        // Validate targetFields has a value
        try {
            Object value = targetFields.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Target fields value is null");
            }
        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error accessing target fields value: " + e.getMessage());
        }

        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    null,
                    "User prompt is required");
        }
    }

    private String convertInputDictionaryToString(TypedValue inputDictionary) throws SmartServiceException {
        try {
            Object value = inputDictionary.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) value;
                return convertMapToJsonString(dataMap);
            } else {
                return value.toString();
            }
        } catch (Exception e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error converting input dictionary: " + e.getMessage());
        }
    }

    private String convertMapToJsonString(Map<String, Object> map) {
        if (map == null) {
            return "{}";
        }

        try {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry == null) {
                    continue;
                }

                if (!first) {
                    json.append(",");
                }
                String key = entry.getKey();
                Object value = entry.getValue();

                // Handle null or empty keys
                if (key == null || key.trim().isEmpty()) {
                    key = "unknown_key_" + System.currentTimeMillis();
                }

                json.append("\"").append(key.replace("\"", "\\\"")).append("\":");

                if (value == null) {
                    json.append("null");
                } else if (value instanceof String) {
                    String strValue = value.toString();
                    json.append("\"").append(strValue.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"))
                            .append("\"");
                } else {
                    json.append(value.toString());
                }
                first = false;
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            System.err.println("Error converting map to JSON string: " + e.getMessage());
            return "{}";
        }
    }

    private String callAzureOpenAI(String inputData) throws SmartServiceException {
        try {
            // Enhanced validation and logging
            System.out.println("=== Azure OpenAI Request Details ===");
            System.out.println("Endpoint: " + (azureOpenAIEndpoint != null ? azureOpenAIEndpoint : "NULL"));
            System.out
                    .println("Deployment: " + (azureOpenAIDeploymentName != null ? azureOpenAIDeploymentName : "NULL"));
            System.out.println("API Version: " + (azureOpenAIApiVersion != null ? azureOpenAIApiVersion : "NULL"));
            System.out.println("Has API Key: " + (azureOpenAIKey != null && !azureOpenAIKey.trim().isEmpty()));

            // Validate configuration before making request
            if (azureOpenAIEndpoint == null || azureOpenAIEndpoint.trim().isEmpty()) {
                throw new SmartServiceException(IntelliMap.class, null, "Azure OpenAI endpoint is null or empty");
            }

            if (azureOpenAIKey == null || azureOpenAIKey.trim().isEmpty()) {
                throw new SmartServiceException(IntelliMap.class, null, "Azure OpenAI API key is null or empty");
            }

            if (azureOpenAIDeploymentName == null || azureOpenAIDeploymentName.trim().isEmpty()) {
                throw new SmartServiceException(IntelliMap.class, null,
                        "Azure OpenAI deployment name is null or empty");
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(IntelliMapConfig.DEFAULT_TIMEOUT)
                    .build();

            // Prepare the request body with enhanced error handling
            String requestBody = buildOpenAIRequestBody(inputData);
            System.out.println("Request body length: " + requestBody.length() + " characters");

            // Build the request URL with validation
            String url = azureOpenAIEndpoint.replaceAll("/+$", "") + "/openai/deployments/" + azureOpenAIDeploymentName
                    + "/chat/completions?api-version=" + azureOpenAIApiVersion;

            System.out.println("Request URL: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", IntelliMapConfig.CONTENT_TYPE_JSON)
                    .header(IntelliMapConfig.API_KEY_HEADER, azureOpenAIKey)
                    .timeout(Duration.ofSeconds(60)) // Extended timeout for complex requests
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println("Sending request to Azure OpenAI...");

            // Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response status code: " + response.statusCode());

            if (response.statusCode() != 200) {
                String responseBody = response.body();
                if (responseBody == null) {
                    responseBody = "No response body";
                }

                // Enhanced error message based on status code
                String errorMessage = "OpenAI request failed. Status: " + response.statusCode();

                switch (response.statusCode()) {
                    case 401:
                        errorMessage += " - AUTHENTICATION ERROR: Check your API key";
                        break;
                    case 403:
                        errorMessage += " - FORBIDDEN: Check your API key permissions and deployment access";
                        break;
                    case 404:
                        errorMessage += " - NOT FOUND: Check your endpoint URL and deployment name";
                        break;
                    case 429:
                        errorMessage += " - RATE LIMIT EXCEEDED: Too many requests, please wait and retry";
                        break;
                    case 500:
                        errorMessage += " - INTERNAL SERVER ERROR: Azure OpenAI service issue";
                        break;
                    default:
                        errorMessage += " - Response: " + responseBody;
                }

                System.err.println(errorMessage);
                System.err.println("Full response body: " + responseBody);

                throw new SmartServiceException(IntelliMap.class, null, errorMessage);
            }

            String responseBody = response.body();
            if (responseBody == null) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Received null response body from OpenAI");
            }

            System.out.println("Successfully received response from Azure OpenAI");
            System.out.println("Response length: " + responseBody.length() + " characters");

            return responseBody;

        } catch (IOException e) {
            String errorMsg = "Network error connecting to OpenAI: " + e.getMessage();
            System.err.println(errorMsg);
            throw new SmartServiceException(IntelliMap.class, e, errorMsg);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "Request to OpenAI was interrupted: " + e.getMessage();
            System.err.println(errorMsg);
            throw new SmartServiceException(IntelliMap.class, e, errorMsg);
        } catch (SmartServiceException e) {
            // Re-throw SmartServiceException as-is
            throw e;
        } catch (Exception e) {
            String errorMsg = "Unexpected error calling OpenAI: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new SmartServiceException(IntelliMap.class, e, errorMsg);
        }
    }

    private String buildOpenAIRequestBody(String inputData) throws SmartServiceException {
        try {
            if (inputData == null) {
                inputData = "{}";
            }

            // Create the request structure
            Map<String, Object> requestMap = new HashMap<>();

            // Create messages array
            List<Map<String, String>> messages = new ArrayList<>();

            // Add system message with detailed instructions
            String systemPrompt = buildSystemPrompt(userPrompt);
            if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "System prompt is null or empty");
            }

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            // Add user message
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "InputDictionary:\n" + inputData);
            messages.add(userMessage);

            requestMap.put("messages", messages);
            requestMap.put("max_tokens", IntelliMapConfig.MAX_TOKENS); // Use configurable max tokens
            requestMap.put("temperature", IntelliMapConfig.TEMPERATURE);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            if (requestBody == null || requestBody.trim().isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Generated request body is null or empty");
            }

            return requestBody;

        } catch (JsonProcessingException e) {
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error building request body: " + e.getMessage());
        }
    }

    private String buildSystemPrompt(String userPrompt) throws SmartServiceException {
        if (userPrompt == null) {
            userPrompt = "";
        }
        StringBuilder prompt = new StringBuilder(userPrompt);

        // ---- dynamic section ① -- Target fields ------------------------------
        prompt.append("TargetFields:\n");
        try {
            // Parse target fields from TypedValue
            Map<String, String> targetFieldsMap = parseTargetFieldsFromTypedValue(targetFields);
            if (targetFieldsMap == null || targetFieldsMap.isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "No target fields found after parsing");
            }

            for (Map.Entry<String, String> entry : targetFieldsMap.entrySet()) {
                if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                    prompt.append('[')
                            .append(entry.getKey())
                            .append("] ")
                            .append(entry.getValue())
                            .append('\n');
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error parsing target fields: " + errorMessage);
        }

        // Enhanced final instruction to ensure proper output format and prevent JSON
        // parsing errors
        prompt.append("\n\n=== CRITICAL OUTPUT REQUIREMENTS ===\n");
        prompt.append("1. RETURN ONLY A VALID JSON ARRAY - No explanations, comments, or additional text\n");
        prompt.append("2. ALL FIELD VALUES MUST BE PROPERLY QUOTED STRINGS\n");
        prompt.append("3. NEVER use unquoted words like 'Product', 'Production', 'Process', etc.\n");
        prompt.append("4. ALL strings containing words starting with 'Prod' MUST be quoted: \"Production Item\"\n");
        prompt.append("5. Include a confidence_level field (0-100) for each mapped record\n");
        prompt.append(
                "6. Example correct format: [{\"field1\":\"Production Ready\",\"field2\":\"Product Code\",\"confidence_level\":85}]\n");
        prompt.append(
                "7. VALIDATE your JSON before returning - ensure all brackets, braces, and quotes are balanced\n");
        prompt.append("8. If you're unsure about a mapping, use null: {\"field1\":null,\"confidence_level\":30}\n");
        prompt.append("\nREMEMBER: Any unquoted text will cause parsing errors. Everything must be valid JSON!");

        String finalPrompt = prompt.toString();

        // Log the system prompt
        System.out.println("=== SYSTEM PROMPT START ===");
        System.out.println(finalPrompt);
        System.out.println("=== SYSTEM PROMPT END ===");
        System.out.println("System prompt length: " + finalPrompt.length() + " characters");

        return finalPrompt;
    }

    private String callAzureOpenAIWithRetry(String inputData) throws SmartServiceException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= IntelliMapConfig.MAX_RETRIES; attempt++) {
            try {
                String response = callAzureOpenAI(inputData);
                if (response == null || response.trim().isEmpty()) {
                    throw new SmartServiceException(
                            IntelliMap.class,
                            null,
                            "Received null or empty response from OpenAI");
                }
                return response;
            } catch (Exception e) {
                lastException = e;

                if (attempt < IntelliMapConfig.MAX_RETRIES) {
                    try {
                        // Exponential backoff: wait 2^attempt seconds
                        long delayMs = (long) Math.pow(2, attempt) * 1000;
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SmartServiceException(
                                IntelliMap.class,
                                ie,
                                "Processing interrupted");
                    }
                }
            }
        }

        String errorMessage = "OpenAI request failed after " + IntelliMapConfig.MAX_RETRIES + " attempts";
        if (lastException != null) {
            String lastErrorMsg = lastException.getMessage();
            if (lastErrorMsg != null) {
                errorMessage += ": " + lastErrorMsg;
            } else {
                errorMessage += ": " + lastException.getClass().getSimpleName();
            }
        }

        throw new SmartServiceException(
                IntelliMap.class,
                lastException,
                errorMessage);
    }

    private void parseOpenAIResponseWithJackson(String response) throws SmartServiceException {
        try {
            if (response == null || response.trim().isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Response is null or empty");
            }

            // First, extract the content from the OpenAI response
            JsonNode responseNode;
            try {
                responseNode = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("Unrecognized token 'Prod'")) {
                    throw new SmartServiceException(
                            IntelliMap.class,
                            e,
                            "Failed to parse OpenAI response due to unrecognized token 'Prod'. This suggests the API returned malformed JSON. Please check your Azure OpenAI configuration and try again.");
                } else {
                    throw new SmartServiceException(
                            IntelliMap.class,
                            e,
                            "Failed to parse OpenAI response as JSON: " + errorMsg);
                }
            }

            String content = responseNode.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.trim().isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "No response content from OpenAI");
            }

            // Log the AI response for debugging
            System.out.println("AI Response Content Preview: " +
                    (content.length() > 500 ? content.substring(0, 500) + "..." : content));
            System.out.println("AI Response Content Length: " + content.length());

            // Check if content looks like JSON
            String trimmedContent = content.trim();
            if (trimmedContent.startsWith("[") || trimmedContent.startsWith("{")) {
                System.out.println("Content appears to be JSON format");
            } else {
                System.out.println("Content does NOT appear to be JSON format");
            }

            // Enhanced JSON parsing with comprehensive error handling
            try {
                // Pre-validate content before parsing
                if (!isValidJsonFormat(content)) {
                    System.err.println("AI returned content that doesn't appear to be valid JSON format");
                    System.err.println("Content preview: "
                            + (content.length() > 300 ? content.substring(0, 300) + "..." : content));

                    // Try to extract JSON from mixed content
                    String extractedJson = extractJsonFromMixedContent(content);
                    if (extractedJson != null && !extractedJson.trim().isEmpty()) {
                        System.out.println("Successfully extracted JSON from mixed content");
                        content = extractedJson;
                    } else {
                        // Fallback: return as plain text with low confidence
                        this.mappedResult = "[]"; // Return empty array instead of malformed content
                        this.overallConfidence = 0.1;
                        System.err.println("Could not extract valid JSON, returning empty result");
                        return;
                    }
                }

                JsonNode contentNode = objectMapper.readTree(content);

                // Check if content is already an array (direct format)
                if (contentNode.isArray()) {
                    this.mappedResult = content;
                    this.overallConfidence = calculateOverallConfidence(content);
                } else {
                    // Look for the result array (fallback format)
                    JsonNode resultNode = contentNode.path("result");
                    if (resultNode.isMissingNode()) {
                        // Try alternative parsing if result is not found
                        this.mappedResult = content;
                        this.overallConfidence = calculateOverallConfidence(content);
                    } else {
                        this.mappedResult = resultNode.toString();
                        this.overallConfidence = calculateOverallConfidence(resultNode.toString());
                    }
                }

            } catch (JsonProcessingException e) {
                String errorMsg = e.getMessage();
                System.err.println("JSON parsing error: " + errorMsg);
                System.err.println("Content that failed to parse: "
                        + (content.length() > 500 ? content.substring(0, 500) + "..." : content));

                if (errorMsg != null && errorMsg.contains("Unrecognized token 'Prod'")) {
                    System.err.println("COMMON ISSUE: AI response contains unquoted 'Prod' tokens");
                    System.err.println("This usually means the AI didn't follow JSON formatting instructions");

                    // Try to fix common "Prod" token issues
                    String fixedContent = fixCommonJsonIssues(content);
                    if (fixedContent != null && !fixedContent.equals(content)) {
                        try {
                            System.out.println("Attempting to parse fixed content...");
                            JsonNode fixedNode = objectMapper.readTree(fixedContent);
                            this.mappedResult = fixedContent;
                            this.overallConfidence = calculateOverallConfidence(fixedContent);
                            System.out.println("Successfully parsed fixed content");
                            return;
                        } catch (Exception fixException) {
                            System.err.println("Fixed content still failed to parse: " + fixException.getMessage());
                        }
                    }
                }

                // Ultimate fallback: return empty array with error info in logs
                this.mappedResult = "[]";
                this.overallConfidence = 0.1;
                System.err.println("Falling back to empty result due to unparseable AI response");
            }

        } catch (SmartServiceException e) {
            throw e; // Re-throw SmartServiceExceptions as-is
        } catch (Exception e) {
            // Fallback: treat the entire response as mapped data
            this.mappedResult = response;
            this.overallConfidence = 0.3;
        }
    }

    private double calculateOverallConfidence(String jsonContent) {
        try {
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                return IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
            }

            JsonNode contentNode = objectMapper.readTree(jsonContent);
            if (contentNode.isArray()) {
                double totalConfidence = 0;
                int validRecords = 0;

                for (JsonNode record : contentNode) {
                    JsonNode confidenceNode = record.path("confidence_level");
                    if (!confidenceNode.isMissingNode()) {
                        try {
                            totalConfidence += Double.parseDouble(confidenceNode.asText());
                            validRecords++;
                        } catch (NumberFormatException e) {
                            // Skip invalid confidence values
                        }
                    } else {
                        // If no confidence_level found, try to calculate from individual field
                        // confidences
                        double recordConfidence = 0;
                        int fieldCount = 0;
                        for (JsonNode field : record) {
                            if (!field.getNodeType().toString().equals("VALUE_STRING") ||
                                    !field.asText().equals("confidence_level")) {
                                fieldCount++;
                            }
                        }
                        if (fieldCount > 0) {
                            recordConfidence = 75.0; // Default confidence if not specified
                            totalConfidence += recordConfidence;
                            validRecords++;
                        }
                    }
                }

                return validRecords > 0 ? totalConfidence / validRecords : IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
            }
        } catch (Exception e) {
            // If parsing fails, return default confidence
        }
        return IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
    }

    /**
     * Attempts to extract valid JSON from mixed content that may contain
     * explanatory text
     */
    private String extractJsonFromMixedContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        try {
            // Look for JSON array patterns
            int arrayStart = content.indexOf('[');
            int arrayEnd = content.lastIndexOf(']');

            if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
                String potentialJson = content.substring(arrayStart, arrayEnd + 1);

                // Basic validation before returning
                if (isValidJsonFormat(potentialJson)) {
                    System.out.println("Extracted JSON array from mixed content");
                    return potentialJson;
                }
            }

            // Look for JSON object patterns
            int objectStart = content.indexOf('{');
            int objectEnd = content.lastIndexOf('}');

            if (objectStart != -1 && objectEnd != -1 && objectEnd > objectStart) {
                String potentialJson = content.substring(objectStart, objectEnd + 1);

                // Basic validation before returning
                if (isValidJsonFormat(potentialJson)) {
                    System.out.println("Extracted JSON object from mixed content");
                    return "[" + potentialJson + "]"; // Wrap single object in array
                }
            }

        } catch (Exception e) {
            System.err.println("Error extracting JSON from mixed content: " + e.getMessage());
        }

        return null;
    }

    /**
     * Attempts to fix common JSON formatting issues, particularly with "Prod"
     * tokens
     */
    private String fixCommonJsonIssues(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        try {
            String fixed = content;

            // Common fix patterns for unquoted tokens
            String[] problemPatterns = {
                    "Product", "Production", "Process", "Program", "Project",
                    "Property", "Provider", "Professional", "Prototype"
            };

            for (String pattern : problemPatterns) {
                // Fix unquoted tokens like: field: Product -> field: "Product"
                fixed = fixed.replaceAll(":\\s*" + pattern + "\\b", ": \"" + pattern + "\"");
                // Fix unquoted tokens like: "field": Product -> "field": "Product"
                fixed = fixed.replaceAll("\":\\s*" + pattern + "\\b", "\": \"" + pattern + "\"");
            }

            // Fix missing quotes around field values that look like identifiers
            fixed = fixed.replaceAll(":\\s*([A-Za-z][A-Za-z0-9\\s]+)(?=[,}])", ": \"$1\"");

            // Remove any trailing commas that might cause issues
            fixed = fixed.replaceAll(",\\s*([}\\]])", "$1");

            if (!fixed.equals(content)) {
                System.out.println("Applied common JSON fixes");
                System.out
                        .println("Original: " + (content.length() > 100 ? content.substring(0, 100) + "..." : content));
                System.out.println("Fixed: " + (fixed.length() > 100 ? fixed.substring(0, 100) + "..." : fixed));
            }

            return fixed;

        } catch (Exception e) {
            System.err.println("Error applying JSON fixes: " + e.getMessage());
            return content; // Return original if fixing fails
        }
    }

    private String generateClientId() {
        try {
            return "intellimap_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
        } catch (Exception e) {
            // Fallback to a simple client ID if there's any issue
            return "intellimap_" + System.nanoTime();
        }
    }

    private String generateRecordId(Map<String, Object> record) {
        try {
            if (record == null) {
                return "null_record_" + System.nanoTime();
            }

            // Try to use DOC_ID as the primary identifier
            Object docId = record.get("DOC_ID");
            if (docId != null) {
                return "doc_" + docId.toString();
            }

            // Fallback: create a hash of the record content
            StringBuilder content = new StringBuilder();
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    content.append(entry.getKey()).append(":").append(entry.getValue()).append("|");
                }
            }
            return "record_" + content.toString().hashCode();
        } catch (Exception e) {
            return "record_" + System.nanoTime();
        }
    }

    private List<Map<String, Object>> parseMultipleRecordsFromAppianFormat(String inputString)
            throws SmartServiceException {
        List<Map<String, Object>> records = new ArrayList<>();

        try {
            System.out.println("Attempting to parse multiple records from Appian Dictionary format");
            System.out.println("Total input length: " + inputString.length());

            // Generic approach: split by the standard Appian Dictionary record separator ];
            // [
            if (inputString.contains("]; [")) {
                // Records separated by ]; [ pattern - this is the most common case
                String[] recordStarts = inputString.split("\\];\\s*\\[");
                System.out.println("Found " + recordStarts.length + " records separated by ']; [' pattern");

                // Process each record
                for (int i = 0; i < recordStarts.length; i++) {
                    String recordData = recordStarts[i].trim();

                    // Add back the brackets that were removed by split
                    if (i == 0 && !recordData.startsWith("[")) {
                        recordData = "[" + recordData;
                    }
                    if (i == recordStarts.length - 1 && !recordData.endsWith("]")) {
                        recordData = recordData + "]";
                    }
                    if (i > 0 && i < recordStarts.length - 1) {
                        recordData = "[" + recordData + "]";
                    }

                    try {
                        System.out.println("Parsing record " + (i + 1) + " data: " +
                                (recordData.length() > 100 ? recordData.substring(0, 100) + "..." : recordData));
                        Map<String, Object> recordMap = parseCustomDelimitedFormat(recordData);
                        if (recordMap != null && !recordMap.isEmpty()) {
                            records.add(recordMap);
                            System.out.println("Parsed record " + (i + 1) + " with " + recordMap.size() + " fields");
                        } else {
                            System.err.println("Record " + (i + 1) + " parsed but resulted in empty map");
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing record " + (i + 1) + ": " + e.getMessage());
                        // Continue with next record
                    }
                }
            } else {
                // Single record or other separation pattern
                // Try to detect if there are multiple records by looking for pattern like "] ["
                // or "],["
                String[] possibleRecords = null;

                if (inputString.contains("] [")) {
                    // Alternative separator pattern
                    possibleRecords = inputString.split("\\]\\s*\\[");
                    System.out.println(
                            "Found " + possibleRecords.length + " potential records separated by '] [' pattern");
                } else if (inputString.contains("],[")) {
                    // Another alternative separator pattern
                    possibleRecords = inputString.split("\\],\\s*\\[");
                    System.out.println("Found " + possibleRecords.length + " potential records separated by '],['");
                }

                if (possibleRecords != null && possibleRecords.length > 1) {
                    // Process multiple records
                    for (int i = 0; i < possibleRecords.length; i++) {
                        String recordData = possibleRecords[i].trim();

                        // Add back brackets
                        if (!recordData.startsWith("[")) {
                            recordData = "[" + recordData;
                        }
                        if (!recordData.endsWith("]")) {
                            recordData = recordData + "]";
                        }

                        try {
                            System.out.println("Parsing alternative record " + (i + 1) + " data: " +
                                    (recordData.length() > 100 ? recordData.substring(0, 100) + "..." : recordData));
                            Map<String, Object> recordMap = parseCustomDelimitedFormat(recordData);
                            if (recordMap != null && !recordMap.isEmpty()) {
                                records.add(recordMap);
                                System.out.println("Parsed alternative record " + (i + 1) + " with " + recordMap.size()
                                        + " fields");
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing alternative record " + (i + 1) + ": " + e.getMessage());
                        }
                    }
                } else {
                    // Single record - parse as-is
                    System.out.println("No clear record separators found, treating as single record");
                    try {
                        Map<String, Object> recordMap = parseCustomDelimitedFormat(inputString);
                        if (recordMap != null && !recordMap.isEmpty()) {
                            records.add(recordMap);
                            System.out.println("Parsed single record with " + recordMap.size() + " fields");
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing single record: " + e.getMessage());
                    }
                }
            }

            System.out.println("Successfully parsed " + records.size() + " records from Appian Dictionary format");
            return records;

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error parsing multiple records from Appian Dictionary format: " + errorMsg);
        }
    }

    private Map<String, Object> parseCustomDelimitedFormat(String inputString) throws SmartServiceException {
        Map<String, Object> recordMap = new HashMap<>();

        try {
            // Remove any leading/trailing brackets and split by comma
            String cleanString = inputString.trim();
            if (cleanString.startsWith("[") && cleanString.endsWith("]")) {
                cleanString = cleanString.substring(1, cleanString.length() - 1);
            }

            // Use a more sophisticated approach to parse field entries
            // Look for patterns like "FieldName:Value" or "*FieldName:Value"
            List<String> fieldEntries = new ArrayList<>();
            int start = 0;
            int depth = 0;

            for (int i = 0; i < cleanString.length(); i++) {
                char c = cleanString.charAt(i);
                if (c == '[')
                    depth++;
                else if (c == ']')
                    depth--;
                else if (c == ',' && depth == 0) {
                    // This is a field separator
                    String fieldEntry = cleanString.substring(start, i).trim();
                    if (!fieldEntry.isEmpty()) {
                        fieldEntries.add(fieldEntry);
                    }
                    start = i + 1;
                }
            }

            // Add the last field entry
            String lastFieldEntry = cleanString.substring(start).trim();
            if (!lastFieldEntry.isEmpty()) {
                fieldEntries.add(lastFieldEntry);
            }

            System.out.println("Found " + fieldEntries.size() + " field entries to parse");

            for (int i = 0; i < fieldEntries.size(); i++) {
                String fieldEntry = fieldEntries.get(i).trim();
                if (fieldEntry.isEmpty()) {
                    continue;
                }

                try {
                    // Split by colon to separate field name and value
                    String[] parts = fieldEntry.split(":", 2);
                    if (parts.length == 2) {
                        String fieldName = parts[0].trim();
                        String fieldValue = parts[1].trim();

                        // Remove any leading asterisk from field name
                        if (fieldName.startsWith("*")) {
                            fieldName = fieldName.substring(1);
                        }

                        // Skip empty field names
                        if (!fieldName.isEmpty()) {
                            // Handle special cases for Appian Dictionary values
                            if (fieldValue.equals("null") || fieldValue.isEmpty()) {
                                recordMap.put(fieldName, null);
                            } else {
                                recordMap.put(fieldName, fieldValue);
                            }
                        }
                    } else if (parts.length == 1) {
                        // Field with no value (just a name)
                        String fieldName = parts[0].trim();
                        if (fieldName.startsWith("*")) {
                            fieldName = fieldName.substring(1);
                        }
                        if (!fieldName.isEmpty()) {
                            recordMap.put(fieldName, null);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing field entry " + i + ": '" + fieldEntry + "' - " + e.getMessage());
                    // Continue with next field
                }
            }

            if (recordMap.isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "No valid fields found in custom delimited format");
            }

            System.out.println("Parsed " + recordMap.size() + " fields from custom delimited format");
            System.out.println("Sample fields: " + recordMap.keySet().stream().limit(5).toList());
            return recordMap;

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error parsing custom delimited format: " + errorMsg);
        }
    }

    private Map<String, String> parseTargetFieldsFromTypedValue(TypedValue targetFields) throws SmartServiceException {
        Map<String, String> targetFieldsMap = new HashMap<>();

        try {
            if (targetFields == null) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Target fields TypedValue is null");
            }

            Object value = targetFields.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Target fields value is null");
            }

            // Handle both List and String[] cases
            if (value instanceof List) {
                List<?> targetFieldsList = (List<?>) value;
                for (Object field : targetFieldsList) {
                    if (field instanceof String) {
                        String fieldString = (String) field;
                        if (fieldString != null && !fieldString.trim().isEmpty()) {
                            // Parse format: "F20:External Material Group" or "F20 - External Material
                            // Group"
                            String[] parts = fieldString.split("[:\\-]", 2);
                            if (parts.length == 2) {
                                String code = parts[0].trim();
                                String name = parts[1].trim();
                                if (!code.isEmpty() && !name.isEmpty()) {
                                    targetFieldsMap.put(code, name);
                                }
                            } else {
                                // If no separator found, treat the whole string as name with auto-generated
                                // code
                                String code = "F" + (targetFieldsMap.size() + 1);
                                targetFieldsMap.put(code, fieldString.trim());
                            }
                        }
                    }
                }
            } else if (value instanceof String[]) {
                // Handle String array case
                String[] targetFieldsArray = (String[]) value;
                for (String fieldString : targetFieldsArray) {
                    if (fieldString != null && !fieldString.trim().isEmpty()) {
                        // Parse format: "F20:External Material Group" or "F20 - External Material
                        // Group"
                        String[] parts = fieldString.split("[:\\-]", 2);
                        if (parts.length == 2) {
                            String code = parts[0].trim();
                            String name = parts[1].trim();
                            if (!code.isEmpty() && !name.isEmpty()) {
                                targetFieldsMap.put(code, name);
                            }
                        } else {
                            // If no separator found, treat the whole string as name with auto-generated
                            // code
                            String code = "F" + (targetFieldsMap.size() + 1);
                            targetFieldsMap.put(code, fieldString.trim());
                        }
                    }
                }
            } else {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "Target fields must be a list of strings or string array, got: "
                                + value.getClass().getSimpleName());
            }

            if (targetFieldsMap.isEmpty()) {
                throw new SmartServiceException(
                        IntelliMap.class,
                        null,
                        "No valid target fields found in the list");
            }

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }
            throw new SmartServiceException(
                    IntelliMap.class,
                    e,
                    "Error parsing target fields from TypedValue: " + errorMessage);
        }

        return targetFieldsMap;
    }
}
