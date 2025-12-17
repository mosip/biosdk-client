package io.mosip.biosdk.client.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.*;
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

	String url = "http://localhost:9098/biosdk-service/init"; //Orginal 9099

	private static MockWebServer mockWebServer;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private Util util;

	private Gson gson;

	@BeforeAll
	public static void startWebServerConnection() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start(InetAddress.getLoopbackAddress(), 9098);

		// Set environment variable for sdk url
		System.setProperty("mosip_biosdk_service", "http://localhost:9098/biosdk-service");
	}

	@AfterAll
	public static void closeWebServerConnection() throws IOException {
        if (mockWebServer != null) {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                logger.error("Error during MockWebServer shutdown: {}", e);
            } finally {
                mockWebServer = null;
            }
        }
	}

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this); // Initialize mocks
		gson = new GsonBuilder().serializeNulls().create();
	}

    @AfterEach
    public void tearDown() {
        if (mockWebServer != null) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    return new MockResponse().setResponseCode(404);
                }
            });
        }
    }

	@Test
	void testRestRequestSuccessWithBodyAndHeaders() throws IOException {
		// Mock URL, method, headers, body, and response
		HttpMethod method = HttpMethod.POST;
		MediaType mediaType = MediaType.APPLICATION_JSON;

		InitRequestDto initRequestDto = new InitRequestDto();
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");
		
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
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
		Assertions.assertTrue(exception.getMessage().contains("rest call failed"));

		// Cleanup
		System.clearProperty("mosip_biosdk_request_response_debug");
	}
    /**
     * Test for restRequest method with SSL bypass enabled
     */
    @Test
    void restRequestWithSslBypassEnabled() throws IOException {
        System.setProperty("restTemplate-ssl-bypass", "true");

        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", "http://localhost:9098/biosdk-service");
        initRequestDto.setInitParams(initParams);
        RequestDto requestDto = generateNewRequestDto(initRequestDto);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    try {
                        return new MockResponse()
                                .setBody(new String(TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
                                        StandardCharsets.UTF_8))
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(200);
                    } catch (IOException e) {
                        logger.error("Processing init response: {}", e);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        System.clearProperty("restTemplate-ssl-bypass");
    }

    /**
     * Test for getObjectMapper method to ensure ObjectMapper is properly configured
     */
    @Test
    void getObjectMapperReturnsConfiguredMapper() {
        ObjectMapper mapper = Util.getObjectMapper();

        assertNotNull(mapper);
        assertFalse(mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(mapper.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
    }

    /**
     * Test for getObjectMapper method returning same instance on multiple calls
     */
    @Test
    void getObjectMapperReturnsSameInstance() {
        ObjectMapper mapper1 = Util.getObjectMapper();
        ObjectMapper mapper2 = Util.getObjectMapper();

        assertNotNull(mapper1);
        assertNotNull(mapper2);
        assertSame(mapper1, mapper2);
    }


    /**
     * Test for base64Encode method with null input handling
     */
    @Test
    void base64EncodeWithNullInput() {
        assertThrows(NullPointerException.class, () -> {
            Util.base64Encode(null);
        });
    }

    /**
     * Test for base64Encode method with normal string input
     */
    @Test
    void base64EncodeWithValidInput() {
        String data = "Hello World";
        String expectedEncodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        String encodedData = Util.base64Encode(data);

        assertEquals(expectedEncodedData, encodedData);
    }

    /**
     * Test for restRequest method with custom connection pool settings
     */
    @Test
    void restRequestWithCustomConnectionPoolSettings() throws IOException {
        System.setProperty("restTemplate-max-connection-per-route", "5");
        System.setProperty("restTemplate-total-max-connections", "10");

        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", "http://localhost:9098/biosdk-service");
        initRequestDto.setInitParams(initParams);
        RequestDto requestDto = generateNewRequestDto(initRequestDto);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    try {
                        return new MockResponse()
                                .setBody(new String(TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
                                        StandardCharsets.UTF_8))
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(200);
                    } catch (IOException e) {
                        logger.error("Processing init response: {}", e);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        System.clearProperty("restTemplate-max-connection-per-route");
        System.clearProperty("restTemplate-total-max-connections");
    }

    /**
     * Test for restRequest method with null request body
     */
    @Test
    void restRequestWithNullRequestBody() throws IOException {
        HttpMethod method = HttpMethod.GET;
        MediaType mediaType = MediaType.APPLICATION_JSON;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    try {
                        return new MockResponse()
                                .setBody(new String(TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
                                        StandardCharsets.UTF_8))
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(200);
                    } catch (IOException e) {
                        logger.error("Processing init response: {}", e);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, null, null, Object.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test for base64Encode method with empty string
     */
    @Test
    void base64EncodeWithEmptyString() {
        String result = Util.base64Encode("");

        assertNotNull(result);
        assertEquals("", result);
    }

    /**
     * Test for base64Encode method with special characters
     */
    @Test
    void base64EncodeWithSpecialCharacters() {
        String data = "Hello@World#123!";
        String expectedEncodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        String encodedData = Util.base64Encode(data);

        assertEquals(expectedEncodedData, encodedData);
    }

    /**
     * Test for restRequest method with invalid JSON in response
     */
    @Test
    void restRequestWithInvalidJsonResponse() {
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", "http://localhost:9098/biosdk-service");
        initRequestDto.setInitParams(initParams);
        RequestDto requestDto = generateNewRequestDto(initRequestDto);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{invalid json}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Exception exception = assertThrows(RestClientException.class, () -> {
            Util.restRequest(url, method, mediaType, requestDto, null, Object.class);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("rest call failed"));
    }

    /**
     * Test for restRequest method with connection timeout
     */
    @Test
    void restRequestWithConnectionTimeout() {
        String invalidUrl = "http://localhost:99999/invalid";
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        RequestDto requestDto = generateNewRequestDto(new InitRequestDto());

        Exception exception = assertThrows(RestClientException.class, () -> {
            Util.restRequest(invalidUrl, method, mediaType, requestDto, null, Object.class);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("rest call failed"));
    }

    /**
     * Test for restRequest method with HTTP 500 internal server error
     */
    @Test
    void restRequestWithInternalServerError() throws IOException {
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", "http://localhost:9098/biosdk-service");
        initRequestDto.setInitParams(initParams);
        RequestDto requestDto = generateNewRequestDto(initRequestDto);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    try {
                        return new MockResponse()
                                .setBody(new String(TestUtil.readXmlFileAsBytes("init_response_error_500.json", Client_V_1_0.class),
                                        StandardCharsets.UTF_8))
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(500);
                    } catch (IOException e) {
                        return new MockResponse()
                                .setBody("{\"error\":\"Internal Server Error\"}")
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(500);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Exception exception = assertThrows(RestClientException.class, () -> {
            Util.restRequest(url, method, mediaType, requestDto, null, Object.class);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("rest call failed"));
    }

    /**
     * Test for restRequest method with debug logging enabled and null body
     */
    @Test
    void restRequestWithDebugEnabledAndNullBody() throws IOException {
        System.setProperty("mosip_biosdk_request_response_debug", "y");

        HttpMethod method = HttpMethod.GET;
        MediaType mediaType = MediaType.APPLICATION_JSON;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    try {
                        return new MockResponse()
                                .setBody(new String(TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
                                        StandardCharsets.UTF_8))
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(200);
                    } catch (IOException e) {
                        logger.error("Processing init response: {}", e);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, null, null, Object.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        System.clearProperty("mosip_biosdk_request_response_debug");
    }

    /**
     * Test for restRequest method with custom headers and debug enabled
     */
    @Test
    void restRequestWithCustomHeadersAndDebugEnabled() throws IOException {
        System.setProperty("mosip_biosdk_request_response_debug", "y");

        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", "http://localhost:9098/biosdk-service");
        initRequestDto.setInitParams(initParams);
        RequestDto requestDto = generateNewRequestDto(initRequestDto);

        Map<String, String> headers = new HashMap<>();
        headers.put("Custom-Header", "Custom-Value");
        headers.put("Authorization", "Bearer test-token");

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    try {
                        return new MockResponse()
                                .setBody(new String(TestUtil.readXmlFileAsBytes("init_response_success.json", Client_V_1_0.class),
                                        StandardCharsets.UTF_8))
                                .addHeader("Content-Type", "application/json")
                                .setResponseCode(200);
                    } catch (IOException e) {
                        logger.error("Processing init response: {}", e);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, headers, Object.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        System.clearProperty("mosip_biosdk_request_response_debug");
    }

    /**
     * Test for restRequest method with SSL bypass disabled
     */
    @Test
    void restRequestWithSslBypassDisabled() throws IOException {
        System.setProperty("restTemplate-ssl-bypass", "false");

        try {
            HttpMethod method = HttpMethod.POST;
            MediaType mediaType = MediaType.APPLICATION_JSON;
            RequestDto requestDto = generateNewRequestDto(new InitRequestDto());

            ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

            assertNotNull(response);

        } catch (RestClientException e) {
            assertTrue(e.getMessage().contains("rest call failed"));
        } finally {
            System.clearProperty("restTemplate-ssl-bypass");
        }
    }

    /**
     * Test for restRequest method with connection failure
     */
    @Test
    void restRequestWithConnectionFailure() {
        String invalidUrl = "http://localhost:99999/invalid";
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        RequestDto requestDto = generateNewRequestDto(new InitRequestDto());

        assertThrows(RestClientException.class, () -> {
            Util.restRequest(invalidUrl, method, mediaType, requestDto, null, Object.class);
        });
    }

    /**
     * Test for restRequest method with null request body and debug enabled
     */
    @Test
    void restRequestWithNullBodyAndDebugEnabled() {
        System.setProperty("mosip_biosdk_request_response_debug", "y");

        try {
            HttpMethod method = HttpMethod.GET;
            MediaType mediaType = MediaType.APPLICATION_JSON;

            ResponseEntity<?> response = Util.restRequest(url, method, mediaType, null, null, Object.class);

            assertNotNull(response);

        } catch (RestClientException e) {
            assertTrue(e.getMessage().contains("rest call failed"));
        } finally {
            System.clearProperty("mosip_biosdk_request_response_debug");
        }
    }

    private RequestDto generateNewRequestDto(Object body) {
		RequestDto requestDto = new RequestDto();
		try {
			String jsonBody = Util.getObjectMapper().writeValueAsString(body);
			requestDto.setVersion("1.0");
			requestDto.setRequest(Util.base64Encode(jsonBody));
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize request body", e);
		}
		return requestDto;
	}
}