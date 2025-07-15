package com.lcm.plugins.intellimapsmartservice;

import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.appiancorp.suiteapi.type.TypedValue;

import java.util.ResourceBundle;
import java.util.Map;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Jackson JSON imports for proper JSON parsing
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

// Update internal imports
import com.lcm.plugins.intellimapsmartservice.IntelliMapConfig;
import com.lcm.plugins.intellimapsmartservice.RateLimiter;

@PaletteInfo(paletteCategory = "Map Tools", palette = "IntelliMap Smart Service")
public class IntelliMapSmartService extends AppianSmartService {

	// Input parameters
	private TypedValue customerData;
	private String userPrompt;
	private String azureOpenAIEndpoint;
	private String azureOpenAIKey;
	private String azureOpenAIDeploymentName;

	// Output parameters
	private String mappedData;
	private Double confidenceScore;

	// JSON parsing and utilities
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final RateLimiter rateLimiter = new RateLimiter();

	// Client identifier for rate limiting
	private String clientId;

	// Setters for input parameters
	@Input(required = Required.ALWAYS)
	public void setCustomerData(TypedValue customerData) {
		this.customerData = customerData;
	}

	@Input(required = Required.OPTIONAL)
	public void setUserPrompt(String userPrompt) {
		this.userPrompt = userPrompt;
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
	public String getMappedData() {
		return mappedData;
	}

	public Double getConfidenceScore() {
		return confidenceScore;
	}

	@Override
	public void run() throws SmartServiceException {
		try {
			// Initialize client ID for rate limiting
			clientId = generateClientId();

			// Validate required inputs
			validateInputs();

			// Prepare the prompt
			String prompt = (userPrompt != null && !userPrompt.trim().isEmpty())
					? userPrompt
					: IntelliMapConfig.DEFAULT_PROMPT;

			// Convert customer data to string format
			String customerDataString = convertCustomerDataToString(customerData);

			// Check rate limits before making API call
			rateLimiter.checkRateLimit(clientId);

			// Call Azure OpenAI with retry logic
			String openAIResponse = callAzureOpenAIWithRetry(prompt, customerDataString);

			// Parse the response using proper JSON parsing
			parseOpenAIResponseWithJackson(openAIResponse);

		} catch (SmartServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.processing") + ": " + e.getMessage());
		}
	}

	private void validateInputs() throws SmartServiceException {
		if (customerData == null) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					null,
					getErrorMessage("error.missingCustomerData"));
		}

		if (azureOpenAIEndpoint == null || azureOpenAIEndpoint.trim().isEmpty()) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					null,
					getErrorMessage("error.missingAzureEndpoint"));
		}

		if (azureOpenAIKey == null || azureOpenAIKey.trim().isEmpty()) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					null,
					getErrorMessage("error.missingAzureKey"));
		}

		if (azureOpenAIDeploymentName == null || azureOpenAIDeploymentName.trim().isEmpty()) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					null,
					getErrorMessage("error.missingDeploymentName"));
		}
	}

	private String convertCustomerDataToString(TypedValue customerData) throws SmartServiceException {
		try {
			// Convert the TypedValue to string representation
			Object value = customerData.getValue();
			if (value instanceof Map) {
				// If it's a dictionary/map, convert to simple JSON-like string
				@SuppressWarnings("unchecked")
				Map<String, Object> dataMap = (Map<String, Object>) value;
				return convertMapToJsonString(dataMap);
			} else {
				// For other types, use toString()
				return value.toString();
			}
		} catch (Exception e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.convertingData") + ": " + e.getMessage());
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

	private String callAzureOpenAI(String prompt, String customerData) throws SmartServiceException {
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(IntelliMapConfig.DEFAULT_TIMEOUT)
					.build();

			// Prepare the request body using Jackson
			String requestBody = buildOpenAIRequestBodyWithJackson(prompt, customerData);

			// Build the request URL with configurable API version
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
						IntelliMapSmartService.class,
						null,
						getErrorMessage("error.openAIRequest") + " Status: " + response.statusCode() + " Response: "
								+ response.body());
			}

			return response.body();

		} catch (IOException | InterruptedException e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.openAIConnection") + ": " + e.getMessage());
		} catch (Exception e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.parsingOpenAIResponse") + ": " + e.getMessage());
		}
	}

	private String buildOpenAIRequestBody(String prompt, String customerData) {
		StringBuilder body = new StringBuilder();
		body.append("{");
		body.append("\"messages\":[");
		body.append(
				"{\"role\":\"system\",\"content\":\"You are a data mapping assistant that extracts and maps customer information.\"},");
		body.append("{\"role\":\"user\",\"content\":\"")
				.append(escapeJsonString(prompt + "\\n\\nCustomer Data:\\n" + customerData)).append("\"}");
		body.append("],");
		body.append("\"max_tokens\":1000,");
		body.append("\"temperature\":0.1");
		body.append("}");
		return body.toString();
	}

	private String escapeJsonString(String input) {
		return input.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}

	private String parseOpenAIResponseContent(String responseBody) throws SmartServiceException {
		try {
			// Simple parsing to extract the content from OpenAI response
			int contentStart = responseBody.indexOf("\"content\":\"");
			if (contentStart == -1) {
				throw new SmartServiceException(
						IntelliMapSmartService.class,
						null,
						getErrorMessage("error.noOpenAIResponse"));
			}

			contentStart += 12; // Length of "\"content\":\""
			int contentEnd = responseBody.indexOf("\"", contentStart);

			if (contentEnd == -1) {
				throw new SmartServiceException(
						IntelliMapSmartService.class,
						null,
						getErrorMessage("error.noOpenAIResponse"));
			}

			String content = responseBody.substring(contentStart, contentEnd);
			// Unescape the content
			return unescapeJsonString(content);

		} catch (Exception e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.parsingOpenAIResponse") + ": " + e.getMessage());
		}
	}

	private String unescapeJsonString(String input) {
		return input.replace("\\n", "\n")
				.replace("\\r", "\r")
				.replace("\\t", "\t")
				.replace("\\\"", "\"")
				.replace("\\\\", "\\");
	}

	private void parseOpenAIResponse(String response) throws SmartServiceException {
		try {
			// Try to parse the response for mappedData and confidenceScore
			String mappedData = extractValueFromJson(response, "mappedData");
			String confidenceStr = extractValueFromJson(response, "confidenceScore");

			if (mappedData != null && confidenceStr != null) {
				this.mappedData = mappedData;
				try {
					this.confidenceScore = Double.parseDouble(confidenceStr);
				} catch (NumberFormatException e) {
					this.confidenceScore = 0.5;
				}
			} else {
				// If not in expected format, try to extract JSON from the response
				String jsonMatch = extractJSONFromResponse(response);
				if (jsonMatch != null) {
					String extractedMappedData = extractValueFromJson(jsonMatch, "mappedData");
					String extractedConfidenceStr = extractValueFromJson(jsonMatch, "confidenceScore");

					if (extractedMappedData != null && extractedConfidenceStr != null) {
						this.mappedData = extractedMappedData;
						try {
							this.confidenceScore = Double.parseDouble(extractedConfidenceStr);
						} catch (NumberFormatException e) {
							this.confidenceScore = 0.5;
						}
					} else {
						// Fallback: treat the entire response as mapped data with default confidence
						this.mappedData = response;
						this.confidenceScore = 0.5;
					}
				} else {
					// Fallback: treat the entire response as mapped data with default confidence
					this.mappedData = response;
					this.confidenceScore = 0.5;
				}
			}
		} catch (Exception e) {
			// If parsing fails, treat the response as plain text
			this.mappedData = response;
			this.confidenceScore = 0.3;
		}
	}

	private String extractValueFromJson(String jsonString, String key) {
		try {
			String searchKey = "\"" + key + "\":";
			int keyIndex = jsonString.indexOf(searchKey);
			if (keyIndex == -1) {
				return null;
			}

			int valueStart = keyIndex + searchKey.length();
			// Skip whitespace
			while (valueStart < jsonString.length() && Character.isWhitespace(jsonString.charAt(valueStart))) {
				valueStart++;
			}

			if (valueStart >= jsonString.length()) {
				return null;
			}

			char startChar = jsonString.charAt(valueStart);
			if (startChar == '"') {
				// String value
				valueStart++;
				int valueEnd = jsonString.indexOf('"', valueStart);
				if (valueEnd == -1) {
					return null;
				}
				return unescapeJsonString(jsonString.substring(valueStart, valueEnd));
			} else {
				// Number or boolean value
				int valueEnd = valueStart;
				while (valueEnd < jsonString.length() &&
						(Character.isDigit(jsonString.charAt(valueEnd)) ||
								jsonString.charAt(valueEnd) == '.' ||
								jsonString.charAt(valueEnd) == 'e' ||
								jsonString.charAt(valueEnd) == 'E' ||
								jsonString.charAt(valueEnd) == '+' ||
								jsonString.charAt(valueEnd) == '-' ||
								jsonString.charAt(valueEnd) == 't' ||
								jsonString.charAt(valueEnd) == 'r' ||
								jsonString.charAt(valueEnd) == 'u' ||
								jsonString.charAt(valueEnd) == 'e' ||
								jsonString.charAt(valueEnd) == 'f' ||
								jsonString.charAt(valueEnd) == 'a' ||
								jsonString.charAt(valueEnd) == 'l' ||
								jsonString.charAt(valueEnd) == 's')) {
					valueEnd++;
				}
				return jsonString.substring(valueStart, valueEnd);
			}
		} catch (Exception e) {
			return null;
		}
	}

	private String extractJSONFromResponse(String response) {
		// Simple JSON extraction - look for content between curly braces
		int startIndex = response.indexOf('{');
		int endIndex = response.lastIndexOf('}');

		if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
			return response.substring(startIndex, endIndex + 1);
		}
		return null;
	}

	/**
	 * Generate a unique client ID for rate limiting
	 */
	private String generateClientId() {
		return "client_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
	}

	/**
	 * Call Azure OpenAI with retry logic
	 */
	private String callAzureOpenAIWithRetry(String prompt, String customerData) throws SmartServiceException {
		Exception lastException = null;

		for (int attempt = 1; attempt <= IntelliMapConfig.MAX_RETRIES; attempt++) {
			try {
				return callAzureOpenAI(prompt, customerData);
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
								IntelliMapSmartService.class,
								ie,
								getErrorMessage("error.processing"));
					}
				}
			}
		}

		throw new SmartServiceException(
				IntelliMapSmartService.class,
				lastException,
				getErrorMessage("error.openAIRequest") + " after " + IntelliMapConfig.MAX_RETRIES + " attempts");
	}

	/**
	 * Parse OpenAI response using Jackson JSON library
	 */
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
						IntelliMapSmartService.class,
						null,
						getErrorMessage("error.noOpenAIResponse"));
			}

			// Try to parse the content as JSON
			try {
				JsonNode contentNode = objectMapper.readTree(content);

				// Extract mappedData and confidenceScore
				JsonNode mappedDataNode = contentNode.path(IntelliMapConfig.MAPPED_DATA_KEY);
				JsonNode confidenceNode = contentNode.path(IntelliMapConfig.CONFIDENCE_SCORE_KEY);

				if (!mappedDataNode.isMissingNode()) {
					this.mappedData = mappedDataNode.toString();

					if (!confidenceNode.isMissingNode()) {
						this.confidenceScore = confidenceNode.asDouble();
					} else {
						this.confidenceScore = IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
					}
				} else {
					// If no mappedData found, treat the entire content as mapped data
					this.mappedData = content;
					this.confidenceScore = IntelliMapConfig.DEFAULT_CONFIDENCE_SCORE;
				}

			} catch (JsonProcessingException e) {
				// If content is not valid JSON, treat it as plain text
				this.mappedData = content;
				this.confidenceScore = IntelliMapConfig.FALLBACK_CONFIDENCE_SCORE;
			}

		} catch (JsonProcessingException e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.jsonParsing") + ": " + e.getMessage());
		} catch (Exception e) {
			// Fallback: treat the entire response as mapped data
			this.mappedData = response;
			this.confidenceScore = IntelliMapConfig.FALLBACK_CONFIDENCE_SCORE;
		}
	}

	/**
	 * Build OpenAI request body using Jackson
	 */
	private String buildOpenAIRequestBodyWithJackson(String prompt, String customerData) throws SmartServiceException {
		try {
			// Create the request structure
			Map<String, Object> requestMap = new java.util.HashMap<>();

			// Create messages array
			java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();

			// Add system message
			Map<String, String> systemMessage = new java.util.HashMap<>();
			systemMessage.put("role", IntelliMapConfig.SYSTEM_ROLE);
			systemMessage.put("content", IntelliMapConfig.SYSTEM_MESSAGE);
			messages.add(systemMessage);

			// Add user message
			Map<String, String> userMessage = new java.util.HashMap<>();
			userMessage.put("role", IntelliMapConfig.USER_ROLE);
			userMessage.put("content", prompt + "\n\nCustomer Data:\n" + customerData);
			messages.add(userMessage);

			requestMap.put("messages", messages);
			requestMap.put("max_tokens", IntelliMapConfig.MAX_TOKENS);
			requestMap.put("temperature", IntelliMapConfig.TEMPERATURE);

			return objectMapper.writeValueAsString(requestMap);

		} catch (JsonProcessingException e) {
			throw new SmartServiceException(
					IntelliMapSmartService.class,
					e,
					getErrorMessage("error.jsonParsing") + ": " + e.getMessage());
		}
	}

	/**
	 * Get error message from resource bundle
	 */
	private String getErrorMessage(String key) {
		try {
			// return resourceBundle.getString(key);
		} catch (Exception e) {
			// Fallback to key if resource bundle fails
			return key;
		}
		return key;
	}

}
