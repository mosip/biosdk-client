package io.mosip.biosdk.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.biosdk.client.dto.InitRequestDto;
import io.mosip.biosdk.client.dto.RequestDto;
import io.mosip.biosdk.client.impl.spec_1_0.Client_V_1_0;
import io.mosip.biosdk.client.utils.TestUtil;
import io.mosip.biosdk.client.utils.Util;
import io.mosip.kernel.core.logger.spi.Logger;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class UtilTest {
	private static Logger logger = LoggerConfig.logConfig(UtilTest.class);

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

		// Set environment variable for sdk url
		System.setProperty("mosip_biosdk_service", "http://localhost:9099/biosdk-service");
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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

		initRequestDto.setInitParams(initParams); // Set initialization params

		RequestDto requestDto = generateNewRequestDto(initRequestDto);
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put("Authorization", "Bearer token");

		// Mock response for /biosdk-service/init
		mockWebServer.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				if (request.getPath().equals("/biosdk-service/init")) {
					try {
						return new MockResponse()
								.setBody(new String(
										TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json").setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing init response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");
		
		initRequestDto.setInitParams(initParams); // Set initialization params
		RequestDto requestDto = generateNewRequestDto(initRequestDto);

		// Mock response for /biosdk-service/init
		mockWebServer.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				if (request.getPath().equals("/biosdk-service/init")) {
					try {
						return new MockResponse()
								.setBody(new String(
										TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json").setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing init response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

		initRequestDto.setInitParams(initParams); // Set initialization params
		RequestDto requestDto = generateNewRequestDto(initRequestDto);

		// Mock response for /biosdk-service/init
		mockWebServer.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				if (request.getPath().equals("/biosdk-service/init")) {
					try {
						return new MockResponse()
								.setBody(new String(
										TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json").setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing init response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

		initRequestDto.setInitParams(initParams); // Set initialization params
		RequestDto requestDto = generateNewRequestDto(initRequestDto);

		// Mock response for /biosdk-service/init
		mockWebServer.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				if (request.getPath().equals("/biosdk-service/init")) {
					try {
						return new MockResponse()
								.setBody(new String(
										TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json").setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing init response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

		// Call the method under test with debugging disabled
		ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

		// Assert that the response is as expected
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		// Cleanup
		System.clearProperty("mosip_biosdk_request_response_debug");
	}

	@Test
	void testDebugRequestResponseLoggingDisabledAndNullBody() throws IOException {
		// Set environment variable for debugging (disabled)
		System.setProperty("mosip_biosdk_request_response_debug", "n");

		// Mock URL, method, and media type
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		// Request body set to null
		RequestDto requestDto = null;

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response_error_400.json", UtilTest.class),
				StandardCharsets.UTF_8);

		mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json")
				.setResponseCode(400));

		// Call the method under test and assert exception is thrown
		Exception exception = assertThrows(RestClientException.class, () -> {
			Util.restRequest(url, method, mediaType, requestDto, null, Object.class);
		});

		// Assert that the exception message matches the expected behavior
		assertEquals("rest call failed", exception.getMessage());

		// Cleanup
		System.clearProperty("mosip_biosdk_request_response_debug");
	}

	@Test
	void testDebugRequestResponseWrongURL() throws IOException {
		// Set environment variable for debugging (disabled)
		System.setProperty("mosip_biosdk_request_response_debug", "n");

		// Mock URL, method, and media type
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		// Request body set to null
		RequestDto requestDto = null;

		// Mock response for /biosdk-service/init
		mockWebServer.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				if (request.getPath().equals("/biosdk-service/init/init")) {
					try {
						return new MockResponse()
								.setBody(new String(
										TestUtil.readXmlFileAsBytes("init_response_error_404.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json").setResponseCode(404);
					} catch (IOException e) {
						logger.error("Processing init response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});
				
		// Call the method under test and assert exception is thrown
		Exception exception = assertThrows(RestClientException.class, () -> {
			Util.restRequest(url + "/init", method, mediaType, requestDto, null, Object.class);
		});

		// Assert that the exception message matches the expected behavior
		assertEquals("rest call failed", exception.getMessage());

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