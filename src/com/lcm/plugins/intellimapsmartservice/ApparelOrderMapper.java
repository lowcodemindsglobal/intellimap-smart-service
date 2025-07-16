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
    private TypedValue inputDictionary;
    private String azureOpenAIEndpoint;
    private String azureOpenAIKey;
    private String azureOpenAIDeploymentName;

    // Output parameters
    private String mappedResult;
    private Double overallConfidence;

    // JSON parsing and utilities
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RateLimiter rateLimiter = new RateLimiter();

    // Client identifier for rate limiting
    private String clientId;

    // Target fields mapping
    private static final Map<String, String> TARGET_FIELDS = new HashMap<>();

    static {
        // Initialize the 106 canonical fields
        TARGET_FIELDS.put("F20", "External Material Group");
        TARGET_FIELDS.put("F2", "Customer Style Number");
        TARGET_FIELDS.put("F2_1", "Cust Style_1");
        TARGET_FIELDS.put("F2_2", "Cust Style_2");
        TARGET_FIELDS.put("F6", "Color Code");
        TARGET_FIELDS.put("F6_1", "Color Code_1");
        TARGET_FIELDS.put("F6_2", "Color Code_2");
        TARGET_FIELDS.put("F6_3", "Color Code_3");
        TARGET_FIELDS.put("C1", "Season");
        TARGET_FIELDS.put("C1_1", "Season_1");
        TARGET_FIELDS.put("C1_2", "Season_2");
        TARGET_FIELDS.put("F5", "Style Description");
        TARGET_FIELDS.put("F7", "Color Description");
        TARGET_FIELDS.put("F52", "FG Criteria (Basic Material)");
        TARGET_FIELDS.put("O9", "Customer Purchase Order");
        TARGET_FIELDS.put("O9_1", "Customer Purchase Order_1");
        TARGET_FIELDS.put("O9_2", "Customer Purchase Order_2");
        TARGET_FIELDS.put("O35", "PO LI");
        TARGET_FIELDS.put("O10", "Vendor Purchase Order");
        TARGET_FIELDS.put("F13", "Unit of Measure (for FG)");
        TARGET_FIELDS.put("F13_1", "Unit of Measure (for FG)_1");
        TARGET_FIELDS.put("F14", "Denominator");
        TARGET_FIELDS.put("F19", "Material Group");
        TARGET_FIELDS.put("F41", "Alternative UOM");
        TARGET_FIELDS.put("F22", "Product Hierarchy Description");
        TARGET_FIELDS.put("F21", "Product Hierarchy");
        TARGET_FIELDS.put("F21_1", "Customer Product Reference_1");
        TARGET_FIELDS.put("F15", "Product Type");
        TARGET_FIELDS.put("F25", "Def. Grid Value");
        TARGET_FIELDS.put("F8", "Master Grid");
        TARGET_FIELDS.put("F44", "Material description");
        TARGET_FIELDS.put("C6", "Customer Department");
        TARGET_FIELDS.put("C6_1", "Customer Department _1");
        TARGET_FIELDS.put("F29", "Gender");
        TARGET_FIELDS.put("F29_1", "Gender_1");
        TARGET_FIELDS.put("F24", "SAP Fabric Content Code Description");
        TARGET_FIELDS.put("F23", "SAP Fabric Content Code");
        TARGET_FIELDS.put("F23_1", "Customer FCC_1");
        TARGET_FIELDS.put("F31", "Customer Reference 1");
        TARGET_FIELDS.put("F32", "Customer Reference 2");
        TARGET_FIELDS.put("F33", "Customer Reference 3");
        TARGET_FIELDS.put("F36", "Special Procurement Type");
        TARGET_FIELDS.put("F46", "Purch Org");
        TARGET_FIELDS.put("F47", "Price Unit");
        TARGET_FIELDS.put("F9", "FG Sizes");
        TARGET_FIELDS.put("O11", "FG Order Quantity");
        TARGET_FIELDS.put("F30", "Product Family");
        TARGET_FIELDS.put("C2", "Sales Organization");
        TARGET_FIELDS.put("C3", "Storage Location");
        TARGET_FIELDS.put("C4", "Distribution Channel");
        TARGET_FIELDS.put("C5", "Division");
        TARGET_FIELDS.put("F18", "Material Type");
        TARGET_FIELDS.put("F26", "HS Code");
        TARGET_FIELDS.put("F26_1", "HS Code_1");
        TARGET_FIELDS.put("F27", "Purchasing Group (for 3rd Party Finished Goods)");
        TARGET_FIELDS.put("F28", "Prod. Stor. Location");
        TARGET_FIELDS.put("O38", "Automation team (Apex/Non-Apex)");
        TARGET_FIELDS.put("O22", "Possible RM InHouse Date");
        TARGET_FIELDS.put("O6", "Customer Number (Sold to Party)");
        TARGET_FIELDS.put("O6_1", "Customer Number (Sold to Party)_1");
        TARGET_FIELDS.put("O6_2", "Customer Number (Sold to Party)_2");
        TARGET_FIELDS.put("O6_3", "Customer Number (Sold to Party)_3");
        TARGET_FIELDS.put("L2", "Ship to party");
        TARGET_FIELDS.put("L2_1", "Ship to party_1");
        TARGET_FIELDS.put("L2_2", "Ship to party_2");
        TARGET_FIELDS.put("L3", "Ship Mode");
        TARGET_FIELDS.put("L3_1", "Ship Mode_1");
        TARGET_FIELDS.put("O39", "Requirement category");
        TARGET_FIELDS.put("L1", "Regions");
        TARGET_FIELDS.put("L1_1", "Regions_1");
        TARGET_FIELDS.put("L1_2", "Regions_2");
        TARGET_FIELDS.put("O16", "Actual Production Plant Allocated");
        TARGET_FIELDS.put("L6", "Requested Delivery Date");
        TARGET_FIELDS.put("L4", "GAC");
        TARGET_FIELDS.put("L7", "NDC");
        TARGET_FIELDS.put("L8", "Order Delivery Tolerance");
        TARGET_FIELDS.put("O55", "Under Delivery Tolerance");
        TARGET_FIELDS.put("O8", "Order Type");
        TARGET_FIELDS.put("O24", "Order Reason");
        TARGET_FIELDS.put("O25", "Purchase Order Type");
        TARGET_FIELDS.put("O25_1", "Purchase Order Type_1");
        TARGET_FIELDS.put("O25_2", "Purchase Order Type_2");
        TARGET_FIELDS.put("O26", "Sales Office");
        TARGET_FIELDS.put("O27", "Sales Group");
        TARGET_FIELDS.put("O12", "Sample Type");
        TARGET_FIELDS.put("O31", "TP Vendor (ThirdParty Orders)");
        TARGET_FIELDS.put("O4", "Buy Sequence");
        TARGET_FIELDS.put("O44", "Inquiry #");
        TARGET_FIELDS.put("O45", "Usage Indicator");
        TARGET_FIELDS.put("P6", "Standard Price (ThirdParty Orders)");
        TARGET_FIELDS.put("P7", "Sizewise PO Pricing (ThirdParty Orders)");
        TARGET_FIELDS.put("O46", "Buy Name");
        TARGET_FIELDS.put("O47", "Buy Program");
        TARGET_FIELDS.put("O2", "Buy Month");
        TARGET_FIELDS.put("O3", "Buy Year");
        TARGET_FIELDS.put("O36", "PP Month");
        TARGET_FIELDS.put("O37", "PP Year");
        TARGET_FIELDS.put("O28", "SO Sales Text");
        TARGET_FIELDS.put("O40", "SO Short Text");
        TARGET_FIELDS.put("S1", "QTY");
        TARGET_FIELDS.put("S2", "FOB");
        TARGET_FIELDS.put("P1", "FG Price (FOB)");
        TARGET_FIELDS.put("P1_1", "FG Price (FOB)_1");
        TARGET_FIELDS.put("F50", "SBU");
        TARGET_FIELDS.put("F42", "Def. Stock Category");
        TARGET_FIELDS.put("F16", "Repeat Style");
    }

    // Setters for input parameters
    @Input(required = Required.ALWAYS)
    public void setInputDictionary(TypedValue inputDictionary) {
        this.inputDictionary = inputDictionary;
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

            // Convert input dictionary to string format
            String inputDataString = convertInputDictionaryToString(inputDictionary);

            // Check rate limits before making API call
            rateLimiter.checkRateLimit(clientId);

            // Call Azure OpenAI with retry logic
            String openAIResponse = callAzureOpenAIWithRetry(inputDataString);

            // Parse the response using proper JSON parsing
            parseOpenAIResponseWithJackson(openAIResponse);

        } catch (SmartServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error processing apparel order mapping: " + e.getMessage());
        }
    }

    private void validateInputs() throws SmartServiceException {
        if (inputDictionary == null) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    null,
                    "Input dictionary is required");
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
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else {
                json.append(value.toString());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
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
                    + "/chat/completions?api-version=" + IntelliMapConfig.AZURE_OPENAI_API_VERSION;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", IntelliMapConfig.CONTENT_TYPE_JSON)
                    .header(IntelliMapConfig.API_KEY_HEADER, azureOpenAIKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SmartServiceException(
                        ApparelOrderMapper.class,
                        null,
                        "OpenAI request failed. Status: " + response.statusCode() + " Response: " + response.body());
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error connecting to OpenAI: " + e.getMessage());
        }
    }

    private String buildOpenAIRequestBody(String inputData) throws SmartServiceException {
        try {
            // Create the request structure
            Map<String, Object> requestMap = new HashMap<>();

            // Create messages array
            List<Map<String, String>> messages = new ArrayList<>();

            // Add system message with detailed instructions
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", buildSystemPrompt());
            messages.add(systemMessage);

            // Add user message
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "InputDictionary:\n" + inputData);
            messages.add(userMessage);

            requestMap.put("messages", messages);
            requestMap.put("max_tokens", 4000); // Increased for larger response
            requestMap.put("temperature", 0.1);

            return objectMapper.writeValueAsString(requestMap);

        } catch (JsonProcessingException e) {
            throw new SmartServiceException(
                    ApparelOrderMapper.class,
                    e,
                    "Error building request body: " + e.getMessage());
        }
    }

    private String buildSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an agent that maps raw order data (passed as an Appian Dictionary) to ");
        prompt.append("a standardized apparel-ordering schema that contains 106 canonical fields. ");
        prompt.append("Your goal is to output, for every canonical field, the best-matching value ");
        prompt.append("found in the input dictionary together with a confidence score.\n\n");

        prompt.append("TargetFields:\n");
        for (Map.Entry<String, String> entry : TARGET_FIELDS.entrySet()) {
            prompt.append("[").append(entry.getKey()).append("] ").append(entry.getValue()).append("\n");
        }

        prompt.append("\nSTEPS (follow strictly and in order):\n");
        prompt.append("1. Review every TargetField (code + name).\n");
        prompt.append("2. Examine ALL keys and values of the InputDictionary.\n");
        prompt.append("3. For each TargetField, choose ONE InputDictionary key whose value ");
        prompt.append("best represents the semantic meaning of the TargetField.\n");
        prompt.append("4. Assign a confidence score from 0-100 for that pairing.\n");
        prompt.append("   • 100 = exact semantic & lexical match\n");
        prompt.append("   • 90-99 = strong match\n");
        prompt.append("   • 70-89 = reasonable / partial match\n");
        prompt.append("   • below 70 = weak match (use only if nothing better)\n");
        prompt.append("5. If no reasonable match exists, output an empty string (\"\") for input_key ");
        prompt.append("and value, and set confidence to 0.\n");
        prompt.append("6. No InputDictionary key may be mapped to more than one TargetField.\n");
        prompt.append("7. Do not invent, transform, or split values—use what is present.\n");
        prompt.append("8. Produce the final answer strictly in the Output Format shown below.\n\n");

        prompt.append("Output Format (JSON ONLY — no extra keys, comments, or text):\n");
        prompt.append("result: [\n");
        prompt.append("  {\n");
        prompt.append("    field_code: \"<TargetField code>\",\n");
        prompt.append("    field_name: \"<TargetField name>\",\n");
        prompt.append("    input_key: \"<matched InputDictionary key or empty string>\",\n");
        prompt.append("    value: \"<matched value or empty string>\",\n");
        prompt.append("    confidence: <integer 0-100>\n");
        prompt.append("  },\n");
        prompt.append("  …\n");
        prompt.append("]");

        return prompt.toString();
    }

    private String callAzureOpenAIWithRetry(String inputData) throws SmartServiceException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= IntelliMapConfig.MAX_RETRIES; attempt++) {
            try {
                return callAzureOpenAI(inputData);
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

        throw new SmartServiceException(
                ApparelOrderMapper.class,
                lastException,
                "OpenAI request failed after " + IntelliMapConfig.MAX_RETRIES + " attempts");
    }

    private void parseOpenAIResponseWithJackson(String response) throws SmartServiceException {
        try {
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

                return validFields > 0 ? totalConfidence / validFields : 0.0;
            }
        } catch (Exception e) {
            // If parsing fails, return default confidence
        }
        return 0.5;
    }

    private String generateClientId() {
        return "apparel_mapper_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}