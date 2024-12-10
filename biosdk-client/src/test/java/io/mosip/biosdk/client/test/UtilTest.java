package io.mosip.biosdk.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.biosdk.client.dto.InitRequestDto;
import io.mosip.biosdk.client.dto.RequestDto;
import io.mosip.biosdk.client.utils.TestUtil;
import io.mosip.biosdk.client.utils.Util;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class UtilTest {

	String url = "http://localhost:9099/biosdk-service/init";

	private static MockWebServer mockWebServer;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private Util util;

	private Gson gson;

	@BeforeAll
	public static void startWebServerConnection() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start(InetAddress.getLoopbackAddress(), 9099);
	}

	@AfterAll
	public static void closeWebServerConnection() throws IOException {
		if (mockWebServer != null) {
			mockWebServer.close();
			mockWebServer.shutdown();
			mockWebServer = null;
		}
	}

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this); // Initialize mocks
		gson = new GsonBuilder().serializeNulls().create();
	}

	@Test
	void testRestRequestSuccessWithBodyAndHeaders() throws IOException {
		// Mock URL, method, headers, body, and response
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		InitRequestDto initRequestDto = new InitRequestDto();
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");
		initRequestDto.setInitParams(initParams); // Set initialization params

		RequestDto requestDto = generateNewRequestDto(initRequestDto);
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put("Authorization", "Bearer token");

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", UtilTest.class),
				StandardCharsets.UTF_8);

		mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json")
				.setResponseCode(200));

		// Call the method under test
		ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, headersMap, Object.class);

		// Assert that the response is as expected
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}


	@Test
	void testRestRequestSuccessWithoutHeaders() throws IOException {
		// Mock URL, method, body, and response
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		InitRequestDto initRequestDto = new InitRequestDto();
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");
		initRequestDto.setInitParams(initParams); // Set initialization params
		RequestDto requestDto = generateNewRequestDto(initRequestDto);

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", UtilTest.class),
				StandardCharsets.UTF_8);

		mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json")
				.setResponseCode(200));

		// Call the method under test without headers
		ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

		// Assert that the response is as expected
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void testBase64Encode() {
		String data = "test";
		String expectedEncodedData = "dGVzdA=="; // Base64 encoded value of "test"

		String encodedData = Util.base64Encode(data);
		assertEquals(expectedEncodedData, encodedData);
	}

	@Test
	void testDebugRequestResponseLoggingEnabled() throws IOException {
		// Set environment variable for debugging
		System.setProperty("mosip_biosdk_request_response_debug", "y");

		// Mock URL, method, headers, body, and response
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		InitRequestDto initRequestDto = new InitRequestDto();
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");
		initRequestDto.setInitParams(initParams); // Set initialization params
		RequestDto requestDto = generateNewRequestDto(initRequestDto);

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", UtilTest.class),
				StandardCharsets.UTF_8);

		mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json")
				.setResponseCode(200));
		
		// Call the method under test with debugging enabled
		ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

		// Assert that the response is as expected
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		// Cleanup
		System.clearProperty("mosip_biosdk_request_response_debug");
	}

	@Test
	void testDebugRequestResponseLoggingDisabled() throws IOException {
		// Set environment variable for debugging (disabled)
		System.setProperty("mosip_biosdk_request_response_debug", "n");

		// Mock URL, method, body, and response
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		InitRequestDto initRequestDto = new InitRequestDto();
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");
		initRequestDto.setInitParams(initParams); // Set initialization params
		RequestDto requestDto = generateNewRequestDto(initRequestDto);

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", UtilTest.class),
				StandardCharsets.UTF_8);

		mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json")
				.setResponseCode(200));

		// Call the method under test with debugging disabled
		ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

		// Assert that the response is as expected
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		// Cleanup
		System.clearProperty("mosip_biosdk_request_response_debug");
	}

	private RequestDto generateNewRequestDto(Object body) {
		RequestDto requestDto = new RequestDto();
		requestDto.setVersion("1.0");
		requestDto.setRequest(Util.base64Encode(gson.toJson(body)));
		return requestDto;
	}
}
