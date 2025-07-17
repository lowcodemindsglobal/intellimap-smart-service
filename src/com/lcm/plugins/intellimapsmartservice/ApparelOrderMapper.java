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
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

@PaletteInfo(paletteCategory = "Map Tools", palette = "Apparel Order Mapper")
public class ApparelOrderMapper extends AppianSmartService {

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
                    ApparelOrderMapper.class,
                    e,
                    "Error processing apparel order mapping: " + e.getMessage());
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
                    ApparelOrderMapper.class,
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
                    ApparelOrderMapper.class,
                    e,
                    "Processing interrupted during rate limiting");
        }
    }

    private Map<String, Object> parseInputToMap(String inputData) throws SmartServiceException {
        try {
            // Try to parse as JSON first
            if (inputData.trim().startsWith("{")) {
                return objectMapper.readValue(inputData, Map.class);
            } else {
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
            }
        } catch (Exception e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error parsing input data: " + e.getMessage());
        }
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
                        JsonNode resultNode = contentNode.path("result");

                        if (!resultNode.isMissingNode() && resultNode.isArray()) {
                            for (JsonNode field : resultNode) {
                                Map<String, Object> fieldMap = objectMapper.convertValue(field, Map.class);
                                allFields.add(fieldMap);
                            }
                        }
                    } catch (JsonProcessingException e) {
                        // Skip malformed chunk result
                    }
                }
            }

            // Create final merged result
            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("result", allFields);

            this.mappedResult = objectMapper.writeValueAsString(finalResult);
            this.overallConfidence = calculateOverallConfidence(this.mappedResult);

        } catch (Exception e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
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
                        ApparelOrderMapper.class,
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
                                ApparelOrderMapper.class,
                                null,
                                "Input string is empty");
                    }

                    // Check if it looks like Appian Dictionary format (starts with [*)
                    if (inputString.startsWith("[*")) {
                        // Try to parse as Appian Dictionary format
                        System.out.println("Parsing Appian Dictionary format");
                        System.out.println("Input format preview: "
                                + (inputString.length() > 200 ? inputString.substring(0, 200) + "..." : inputString));
                        Map<String, Object> recordMap = parseCustomDelimitedFormat(inputString);
                        records.add(recordMap);
                        System.out.println("Successfully parsed 1 record from Appian Dictionary format");
                    } else if (inputString.startsWith("{") || inputString.startsWith("[")) {
                        // Try to parse as JSON (but not Appian Dictionary format)
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
                                    ApparelOrderMapper.class,
                                    null,
                                    "JSON string must contain an object or array, got: " + jsonNode.getNodeType());
                        }
                        System.out.println("Successfully parsed " + records.size() + " records from JSON string");
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
                            ApparelOrderMapper.class,
                            e,
                            "Error parsing string input: " + errorMsg);
                }
            } else {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "Input must be a list of records, a single record, or a JSON string, got: "
                                + value.getClass().getSimpleName());
            }

            // Process each record
            List<Map<String, Object>> allResults = new ArrayList<>();
            double totalConfidence = 0.0;

            for (Map<String, Object> record : records) {
                try {
                    if (record == null) {
                        System.err.println("Skipping null record");
                        continue;
                    }

                    // Convert record to JSON string
                    String recordJson = convertMapToJsonString(record);
                    if (recordJson == null || recordJson.trim().isEmpty()) {
                        System.err.println("Skipping record with empty JSON");
                        continue;
                    }

                    // Process single record
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
                        System.err.println("Error parsing OpenAI response JSON: " + e.getMessage());
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
                        // Extract result array from content
                        JsonNode contentNode = null;
                        try {
                            contentNode = objectMapper.readTree(content);
                        } catch (Exception e) {
                            System.err.println("Error parsing content as JSON: " + e.getMessage());
                            continue;
                        }

                        JsonNode resultNode = contentNode.path("result");

                        if (!resultNode.isMissingNode() && resultNode.isArray()) {
                            for (JsonNode field : resultNode) {
                                if (field != null) {
                                    try {
                                        Map<String, Object> fieldMap = objectMapper.convertValue(field, Map.class);
                                        if (fieldMap != null) {
                                            allResults.add(fieldMap);
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Error converting field to map: " + e.getMessage());
                                        // Continue with next field
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

            // Create final result
            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("result", allResults);

            try {
                this.mappedResult = objectMapper.writeValueAsString(finalResult);
                if (this.mappedResult == null) {
                    this.mappedResult = "{\"result\":[]}";
                }
            } catch (Exception e) {
                System.err.println("Error creating final JSON result: " + e.getMessage());
                this.mappedResult = "{\"result\":[]}";
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
                    ApparelOrderMapper.class,
                    e,
                    contextMessage + ": " + errorMessage);
        }
    }

    private void validateInputs() throws SmartServiceException {
        if (inputRecords == null) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Input records are required");
        }

        // Validate inputRecords has a value
        try {
            Object value = inputRecords.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "Input records value is null");
            }
        } catch (Exception e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error accessing input records value: " + e.getMessage());
        }

        if (azureOpenAIEndpoint == null || azureOpenAIEndpoint.trim().isEmpty()) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Azure OpenAI endpoint is required");
        }

        if (azureOpenAIKey == null || azureOpenAIKey.trim().isEmpty()) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Azure OpenAI key is required");
        }

        if (azureOpenAIDeploymentName == null || azureOpenAIDeploymentName.trim().isEmpty()) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Azure OpenAI deployment name is required");
        }

        if (azureOpenAIApiVersion == null || azureOpenAIApiVersion.trim().isEmpty()) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Azure OpenAI API version is required");
        }

        if (targetFields == null) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Target fields are required");
        }

        // Validate targetFields has a value
        try {
            Object value = targetFields.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "Target fields value is null");
            }
        } catch (Exception e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error accessing target fields value: " + e.getMessage());
        }

        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
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
                    ApparelOrderMapper.class,
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
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(IntelliMapConfig.DEFAULT_TIMEOUT)
                    .build();

            // Prepare the request body
            String requestBody = buildOpenAIRequestBody(inputData);

            // Build the request URL
            String url = azureOpenAIEndpoint + "/openai/deployments/" + azureOpenAIDeploymentName
                    + "/chat/completions?api-version=" + azureOpenAIApiVersion;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", IntelliMapConfig.CONTENT_TYPE_JSON)
                    .header(IntelliMapConfig.API_KEY_HEADER, azureOpenAIKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String responseBody = response.body();
                if (responseBody == null) {
                    responseBody = "No response body";
                }
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "OpenAI request failed. Status: " + response.statusCode() + " Response: " + responseBody);
            }

            String responseBody = response.body();
            if (responseBody == null) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "Received null response body from OpenAI");
            }

            return responseBody;

        } catch (IOException | InterruptedException e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error connecting to OpenAI: " + e.getMessage());
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
                        ApparelOrderMapper.class,
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
                        ApparelOrderMapper.class,
                        null,
                        "Generated request body is null or empty");
            }

            return requestBody;

        } catch (JsonProcessingException e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
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
                        ApparelOrderMapper.class,
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
                    ApparelOrderMapper.class,
                    e,
                    "Error parsing target fields: " + errorMessage);
        }

        // ---- static section ② -- Steps & output format -----------------------
        prompt.append("""

                    STEPS (follow strictly and in order):
                    1. Review every TargetField (code + name).
                    2. Examine ALL keys and values of the InputDictionary.
                    3. For each TargetField, choose ONE InputDictionary key whose value \
                       best represents the semantic meaning of the TargetField.
                    4. Assign a confidence score from 0-100 for that pairing.
                       • 100 = exact semantic & lexical match
                       • 90-99 = strong match
                       • 70-89 = reasonable / partial match
                       • below 70 = weak match (use only if nothing better)
                    5. If no reasonable match exists, output an empty string ("") for input_key \
                       and value, and set confidence to 0.
                    6. No InputDictionary key may be mapped to more than one TargetField.
                    7. Do not invent, transform, or split values—use what is present.
                    8. Produce the final answer strictly in the Output Format shown below.

                    Output Format (JSON ONLY — no extra keys, comments, or text):
                    result: [
                      {
                        field_code: "<TargetField code>",
                        field_name: "<TargetField name>",
                        input_key: "<matched InputDictionary key or empty string>",
                        value: "<matched value or empty string>",
                        confidence: <integer 0-100>
                      },
                      …
                    ]
                """);

        return prompt.toString();
    }

    private String callAzureOpenAIWithRetry(String inputData) throws SmartServiceException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= IntelliMapConfig.MAX_RETRIES; attempt++) {
            try {
                String response = callAzureOpenAI(inputData);
                if (response == null || response.trim().isEmpty()) {
                    throw new SmartServiceException(
                            ApparelOrderMapper.class,
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
                                ApparelOrderMapper.class,
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
                ApparelOrderMapper.class,
                lastException,
                errorMessage);
    }

    private void parseOpenAIResponseWithJackson(String response) throws SmartServiceException {
        try {
            if (response == null || response.trim().isEmpty()) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "Response is null or empty");
            }

            // First, extract the content from the OpenAI response
            JsonNode responseNode = objectMapper.readTree(response);
            String content = responseNode.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.trim().isEmpty()) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "No response content from OpenAI");
            }

            // Try to parse the content as JSON
            try {
                JsonNode contentNode = objectMapper.readTree(content);

                // Look for the result array
                JsonNode resultNode = contentNode.path("result");
                if (resultNode.isMissingNode()) {
                    // Try alternative parsing if result is not found
                    this.mappedResult = content;
                    this.overallConfidence = calculateOverallConfidence(content);
                } else {
                    this.mappedResult = resultNode.toString();
                    this.overallConfidence = calculateOverallConfidence(resultNode.toString());
                }

            } catch (JsonProcessingException e) {
                // If content is not valid JSON, treat it as plain text
                this.mappedResult = content;
                this.overallConfidence = calculateOverallConfidence(content);
            }

        } catch (JsonProcessingException e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error parsing JSON response: " + e.getMessage());
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
                int validFields = 0;

                for (JsonNode field : contentNode) {
                    JsonNode confidenceNode = field.path("confidence");
                    if (!confidenceNode.isMissingNode()) {
                        totalConfidence += confidenceNode.asDouble();
                        validFields++;
                    }
                }

                return validFields > 0 ? totalConfidence / validFields : IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
            }
        } catch (Exception e) {
            // If parsing fails, return default confidence
        }
        return IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
    }

    private String generateClientId() {
        try {
            return "apparel_mapper_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
        } catch (Exception e) {
            // Fallback to a simple client ID if there's any issue
            return "apparel_mapper_" + System.nanoTime();
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

            // Split by comma to get individual field entries
            String[] fieldEntries = cleanString.split(",");
            System.out.println("Found " + fieldEntries.length + " field entries to parse");

            for (int i = 0; i < fieldEntries.length; i++) {
                String fieldEntry = fieldEntries[i].trim();
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
                        ApparelOrderMapper.class,
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
                    ApparelOrderMapper.class,
                    e,
                    "Error parsing custom delimited format: " + errorMsg);
        }
    }

    private Map<String, String> parseTargetFieldsFromTypedValue(TypedValue targetFields) throws SmartServiceException {
        Map<String, String> targetFieldsMap = new HashMap<>();

        try {
            if (targetFields == null) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "Target fields TypedValue is null");
            }

            Object value = targetFields.getValue();
            if (value == null) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
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
                        ApparelOrderMapper.class,
                        null,
                        "Target fields must be a list of strings or string array, got: "
                                + value.getClass().getSimpleName());
            }

            if (targetFieldsMap.isEmpty()) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "No valid target fields found in the list");
            }

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error parsing target fields from TypedValue: " + errorMessage);
        }

        return targetFieldsMap;
    }
}