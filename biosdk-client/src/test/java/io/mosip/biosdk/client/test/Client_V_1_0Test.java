package io.mosip.biosdk.client.test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.biosdk.client.dto.ErrorDto;
import io.mosip.biosdk.client.dto.RequestDto;
import io.mosip.biosdk.client.utils.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.biosdk.client.exception.BioSdkClientException;
import io.mosip.biosdk.client.impl.spec_1_0.Client_V_1_0;
import io.mosip.biosdk.client.utils.TestUtil;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.core.logger.spi.Logger;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.http.ResponseEntity;

class Client_V_1_0Test {
	private static MockWebServer mockWebServer;
	private static Logger logger = LoggerConfig.logConfig(Client_V_1_0Test.class);

	@BeforeAll
	public static void startWebServerConnection() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start(InetAddress.getLoopbackAddress(), 9098); //Orginal 9099
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
	}

	@Test
	void testConstructorInitialization() {
		Client_V_1_0 client = new Client_V_1_0();
		assertNotNull(client);
	}

	@Test
	void testInit_Successful() throws BioSdkClientException, IOException {
		Client_V_1_0 client = new Client_V_1_0();

		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

		SDKInfo result = client.init(initParams);

		assertNotNull(result);
	}

	@Test
	void testCheckQuality_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(TestUtil.getBIRDataFromXMLType(
				TestUtil.readXmlFileAsBytes("check_quality_request.xml", Client_V_1_0.class), "Face"));

		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

		// Mock response for /biosdk-service/init
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
				} else if (request.getPath().equals("/biosdk-service/check-quality")) {
					try {
						return new MockResponse()
								.setBody(new String(TestUtil.readXmlFileAsBytes("check_quality_success_response.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json")
								.setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing match response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

		client.init(initParams);

		Response<QualityCheck> response = client.checkQuality(sample, modalities, flags);

		// Assert the response
		assertNotNull(response);
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getResponse());
		assertEquals(100, response.getResponse().getScores().get(BiometricType.FACE).getScore());
	}

	// Test checkQuality method - Exception handling
	@Test
	void testCheckQuality_Exception() {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute and expect an exception
		BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
			client.checkQuality(sample, modalities, flags);
		});

		// Verify exception message
		assertNotNull(exception);
		assertEquals("500", exception.getErrorCode());
	}

	// Test match method - Successful case
	@Test
	void testMatch_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(TestUtil.getBIRDataFromXMLType(
				TestUtil.readXmlFileAsBytes("matcher_request_probe.xml", Client_V_1_0.class), "Face"));
		BiometricRecord[] gallery = new BiometricRecord[1];
		BiometricRecord galleryInfo = new BiometricRecord();
		galleryInfo.setSegments(TestUtil.getBIRDataFromXMLType(
				TestUtil.readXmlFileAsBytes("matcher_request_gallery.xml", Client_V_1_0.class), "Face"));
		gallery[0] = galleryInfo;
		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
				} else if (request.getPath().equals("/biosdk-service/match")) {
					try {
						return new MockResponse()
								.setBody(new String(TestUtil.readXmlFileAsBytes("match_success_not_match_response.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json")
								.setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing match response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

		client.init(initParams);
		Response<MatchDecision[]> response = client.match(sample, gallery, modalities, flags);

		// Verify the results
		assertNotNull(response);
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getResponse());
		assertEquals(1, response.getResponse().length);
		assertEquals(Match.NOT_MATCHED, response.getResponse()[0].getDecisions().get(BiometricType.FACE).getMatch());
	}

	// Test match method - Exception handling
	@Test
	void testMatch_Exception() {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		BiometricRecord[] gallery = new BiometricRecord[1];
		gallery[0] = new BiometricRecord();
		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Execute and expect an exception
		BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
			client.match(sample, gallery, modalities, flags);
		});

		// Verify exception message
		assertNotNull(exception);
		assertEquals("500", exception.getErrorCode());

		// Cleanup
		System.clearProperty("mosip_biosdk_service");
	}

	// Test extractTemplate method - Successful case
	@Test
	void testExtractTemplate_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(TestUtil.getBIRDataFromXMLType(
				TestUtil.readXmlFileAsBytes("extract_request_probe.xml", Client_V_1_0.class), "Finger"));
		List<BiometricType> modalitiesToExtract = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
				} else if (request.getPath().equals("/biosdk-service/extract-template")) {
					try {
						return new MockResponse()
								.setBody(new String(TestUtil.readXmlFileAsBytes("extract_template_success_response.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json")
								.setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing extract template response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

		client.init(initParams);
		Response<BiometricRecord> response = client.extractTemplate(sample, modalitiesToExtract, flags);

		// Verify the results
		assertNotNull(response);
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getResponse());
	}

	// Test extractTemplate method - Exception handling
	@Test
	void testExtractTemplate_Exception() {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalitiesToExtract = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute and expect an exception
		BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
			client.extractTemplate(sample, modalitiesToExtract, flags);
		});

		// Verify exception message
		assertNotNull(exception);
		assertEquals("500", exception.getErrorCode());
	}

	@Test
	void testConvertFormatV2_Success() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare inputs
		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(TestUtil.getBIRDataFromXMLType(
				TestUtil.readXmlFileAsBytes("convert_request_probe.xml", Client_V_1_0.class), "Face"));

		String sourceFormat = "ISO19794_5_2011";
		String targetFormat = "IMAGE/PNG";
		Map<String, String> sourceParams = new HashMap<>();
		Map<String, String> targetParams = new HashMap<>();
		List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FACE);

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://localhost:9098/biosdk-service");

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
				} else if (request.getPath().equals("/biosdk-service/convert-format")) {
					try {
						return new MockResponse()
								.setBody(new String(TestUtil.readXmlFileAsBytes("convert_format_success_response.json", Client_V_1_0.class),
										StandardCharsets.UTF_8))
								.addHeader("Content-Type", "application/json")
								.setResponseCode(200);
					} catch (IOException e) {
						logger.error("Processing convert format response: {}", e);
					}
				}
				return new MockResponse().setResponseCode(404); // Fallback for unmatched requests
			}
		});

		client.init(initParams);

		Response<BiometricRecord> response = client.convertFormatV2(sample, sourceFormat, targetFormat, sourceParams,
				targetParams, modalitiesToConvert);

		// Verify the results
		assertNotNull(response);
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getResponse());
	}

	@Test
	void testConvertFormatV2_UnexpectedException() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare inputs
		BiometricRecord sample = new BiometricRecord();
		String sourceFormat = "sourceFormat";
		String targetFormat = "targetFormat";
		Map<String, String> sourceParams = new HashMap<>();
		Map<String, String> targetParams = new HashMap<>();
		List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FINGER);

		// Execute and expect an exception
		BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
			client.convertFormatV2(sample, sourceFormat, targetFormat, sourceParams, targetParams, modalitiesToConvert);
		});

		// Verify exception message
		assertNotNull(exception);
		assertEquals("500", exception.getErrorCode());
	}

	@Test
	void testGetSdkUrls_DefaultConfigured() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare input
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.default", "http://default-url.com");
		initParams.put("format.url.test", "http://test-url.com");

		// Access private method using reflection
		Method getSdkUrls = Client_V_1_0.class.getDeclaredMethod("getSdkUrls", Map.class);
		getSdkUrls.setAccessible(true);

		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>) getSdkUrls.invoke(client, initParams);

		// Assert results
		assertNotNull(result);
		assertEquals("http://default-url.com", result.get("default"));
		assertEquals("http://test-url.com", result.get("test"));
	}

	@Test
	void testGetSdkUrls_NoDefaultUrlUsesFirstUrl() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare input without "default"
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", "http://test-url.com");

		// Access private method using reflection
		Method getSdkUrls = Client_V_1_0.class.getDeclaredMethod("getSdkUrls", Map.class);
		getSdkUrls.setAccessible(true);

		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>) getSdkUrls.invoke(client, initParams);

		// Assert results
		assertNotNull(result);
		assertEquals("http://test-url.com", result.get("test"));
	}

	@Test
	void testGetSdkUrls_NoValidUrlsThrowsException() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare input without any valid URLs
		Map<String, String> initParams = new HashMap<>();

		// Access private method using reflection
		Method getSdkUrls = Client_V_1_0.class.getDeclaredMethod("getSdkUrls", Map.class);
		getSdkUrls.setAccessible(true);

		// Assert exception is thrown
		Exception exception = assertThrows(Exception.class, () -> {
			getSdkUrls.invoke(client, initParams);
		});
		assertTrue(exception.getCause() instanceof Exception);
		assertEquals("No valid sdk service url configured", exception.getCause().getMessage());
	}

	@Test
	void testGetAggregatedSdkInfo_SingleElement() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		Client_V_1_0 client = new Client_V_1_0();

		List<SDKInfo> sdkInfos = new ArrayList<>();
		SDKInfo sdkInfo = new SDKInfo("1.0", "1.0", "OrganizationA", "TypeA");
		sdkInfos.add(sdkInfo);

		// Use reflection to access the private method
		Method method = Client_V_1_0.class.getDeclaredMethod("getAggregatedSdkInfo", List.class);
		method.setAccessible(true);

		SDKInfo result = (SDKInfo) method.invoke(client, sdkInfos);

		// Since there is only one element, it should return that element
		assertNotNull(result);
		assertEquals("1.0", result.getApiVersion());
		assertEquals("1.0", result.getSdkVersion());
		assertEquals("OrganizationA", result.getProductOwner().getOrganization());
		assertEquals("TypeA", result.getProductOwner().getType());
	}

	@Test
	void testGetAggregatedSdkInfo_MultipleElements() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		Client_V_1_0 client = new Client_V_1_0();

		List<SDKInfo> sdkInfos = new ArrayList<>();
		SDKInfo sdkInfo1 = new SDKInfo("1.0", "1.0", "OrganizationA", "TypeA");
		SDKInfo sdkInfo2 = new SDKInfo("1.1", "1.1", "OrganizationB", "TypeB");
		sdkInfos.add(sdkInfo1);
		sdkInfos.add(sdkInfo2);

		// Use reflection to access the private method
		Method method = Client_V_1_0.class.getDeclaredMethod("getAggregatedSdkInfo", List.class);
		method.setAccessible(true);

		SDKInfo result = (SDKInfo) method.invoke(client, sdkInfos);

		// Since there are multiple elements, it should return the first one
		assertNotNull(result);
		assertEquals("1.0", result.getApiVersion()); // Should return the first element
		assertEquals("1.0", result.getSdkVersion());
		assertEquals("OrganizationA", result.getProductOwner().getOrganization());
		assertEquals("TypeA", result.getProductOwner().getType());
	}

	@Test
	void testGetAggregatedSdkInfo_EmptyList() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		Client_V_1_0 client = new Client_V_1_0();

		List<SDKInfo> sdkInfos = new ArrayList<>();

		// Use reflection to access the private method
		Method method = Client_V_1_0.class.getDeclaredMethod("getAggregatedSdkInfo", List.class);
		method.setAccessible(true);

		SDKInfo result = (SDKInfo) method.invoke(client, sdkInfos);
		// Since the list is empty, it should return null
		assertNull(result);
	}

	@Test
	void testGetDefaultSdkServiceUrlFromEnv_WithSystemProperty() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Set system property for MOSIP_BIOSDK_SERVICE
		setSystemProperty("mosip_biosdk_service", "http://localhost:9098");

		// Use reflection to access the private method
		Method method = Client_V_1_0.class.getDeclaredMethod("getDefaultSdkServiceUrlFromEnv");
		method.setAccessible(true);

		// Invoke the private method
		String result = (String) method.invoke(client);

		// Assert that the system property value is returned
		assertEquals("http://localhost:9098", result);

		// Clear the system property after the test
		clearSystemProperty("mosip_biosdk_service");
	}

	@Test
	void testGetDefaultSdkServiceUrlFromEnv_NoValue() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Clear system property and environment variable
		clearSystemProperty("mosip_biosdk_service");

		// Use reflection to access the private method
		Method method = Client_V_1_0.class.getDeclaredMethod("getDefaultSdkServiceUrlFromEnv");
		method.setAccessible(true);

        // Invoke the private method
        String result = (String) method.invoke(client);

        // Assert that null is returned if neither the system property nor the environment variable is set
        assertNull(result);
    }

    /**
     * Test for segment method when client is not initialized, expecting BioSdkClientException
     */
    @Test
    void segment_Exception() {
        Client_V_1_0 client = spy(new Client_V_1_0());

        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
        Map<String, String> flags = new HashMap<>();

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.segment(sample, modalities, flags);
        });

        assertNotNull(exception);
        assertEquals("500", exception.getErrorCode());
    }

    /**
     * Test for successful convertFormat operation
     */
    @Test
    void convertFormat_Success() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());

        BiometricRecord sample = new BiometricRecord();
        sample.setSegments(new ArrayList<>());

        String sourceFormat = "ISO19794_5_2011";
        String targetFormat = "IMAGE/PNG";
        Map<String, String> sourceParams = new HashMap<>();
        Map<String, String> targetParams = new HashMap<>();
        List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FACE);

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/convert-format")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"statusCode\":200,\"statusMessage\":\"Success\",\"response\":{}}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);
        BiometricRecord result = client.convertFormat(sample, sourceFormat, targetFormat,
                sourceParams, targetParams, modalitiesToConvert);

        assertNotNull(result);
    }

    /**
     * Test for convertFormat method when client is not initialized, expecting BioSdkClientException
     */
    @Test
    void convertFormat_Exception() {
        Client_V_1_0 client = spy(new Client_V_1_0());

        BiometricRecord sample = new BiometricRecord();
        String sourceFormat = "sourceFormat";
        String targetFormat = "targetFormat";
        Map<String, String> sourceParams = new HashMap<>();
        Map<String, String> targetParams = new HashMap<>();
        List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FINGER);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.convertFormat(sample, sourceFormat, targetFormat, sourceParams, targetParams, modalitiesToConvert);
        });

        assertNotNull(exception);
        assertEquals("500", exception.getErrorCode());
    }

    /**
     * Test errorHandler method with null errors list
     */
    @Test
    void errorHandler_WithNullErrors() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Method errorHandlerMethod = Client_V_1_0.class.getDeclaredMethod("errorHandler", List.class);
        errorHandlerMethod.setAccessible(true);

        assertDoesNotThrow(() -> {
            errorHandlerMethod.invoke(client, (List<ErrorDto>) null);
        });
    }

    /**
     * Test errorHandler method with list containing null ErrorDto
     */
    @Test
    void errorHandler_WithNullErrorDto() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        List<ErrorDto> errors = new ArrayList<>();
        errors.add(null);

        Method errorHandlerMethod = Client_V_1_0.class.getDeclaredMethod("errorHandler", List.class);
        errorHandlerMethod.setAccessible(true);

        assertDoesNotThrow(() -> {
            errorHandlerMethod.invoke(client, errors);
        });
    }

    /**
     * Test getAggregatedSdkInfo method with null productOwner
     */
    @Test
    void getAggregatedSdkInfo_WithNullProductOwner() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        List<SDKInfo> sdkInfos = new ArrayList<>();
        SDKInfo sdkInfo = new SDKInfo("1.0", "1.0", null, null);
        sdkInfo.setProductOwner(null);
        sdkInfos.add(sdkInfo);

        Method method = Client_V_1_0.class.getDeclaredMethod("getAggregatedSdkInfo", List.class);
        method.setAccessible(true);

        SDKInfo result = (SDKInfo) method.invoke(client, sdkInfos);

        assertNotNull(result);
        assertEquals("1.0", result.getApiVersion());
        assertEquals("1.0", result.getSdkVersion());
    }

    /**
     * Test addOtherSdkInfoDetails method with all null fields
     */
    @Test
    void addOtherSdkInfoDetails_AllNullFields() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        SDKInfo sourceSdkInfo = new SDKInfo("1.0", "1.0", "org", "type");
        sourceSdkInfo.setOtherInfo(null);
        sourceSdkInfo.setSupportedMethods(null);
        sourceSdkInfo.setSupportedModalities(null);

        SDKInfo aggregatedSdkInfo = new SDKInfo("1.0", "1.0", "org", "type");

        Method method = Client_V_1_0.class.getDeclaredMethod("addOtherSdkInfoDetails", SDKInfo.class, SDKInfo.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> {
            method.invoke(client, sourceSdkInfo, aggregatedSdkInfo);
        });
    }

    /**
     * Test successful segment operation
     */
    @Test
    void segment_Success() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();

        sample.setSegments(new ArrayList<>());

        List<BiometricType> modalitiesToSegment = Arrays.asList(BiometricType.FACE);
        Map<String, String> flags = new HashMap<>();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/segment")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"statusCode\":200,\"statusMessage\":\"Success\",\"response\":{\"segments\":[]}}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);
        Response<BiometricRecord> response = client.segment(sample, modalitiesToSegment, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getResponse());
    }

    /**
     * Test checkQuality method with non-2xx HTTP response
     */
    @Test
    void checkQuality_Non2xxResponse() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
        Map<String, String> flags = new HashMap<>();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/check-quality")) {
                    return new MockResponse().setResponseCode(500);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.checkQuality(sample, modalities, flags);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("500") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test match method with non-2xx HTTP response
     */
    @Test
    void match_Non2xxResponse() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        BiometricRecord[] gallery = new BiometricRecord[1];
        gallery[0] = new BiometricRecord();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
        Map<String, String> flags = new HashMap<>();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/match")) {
                    return new MockResponse().setResponseCode(400);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.match(sample, gallery, modalities, flags);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("400") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test extractTemplate method with non-2xx HTTP response
     */
    @Test
    void extractTemplate_Non2xxResponse() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FINGER);
        Map<String, String> flags = new HashMap<>();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/extract-template")) {
                    return new MockResponse().setResponseCode(503);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.extractTemplate(sample, modalities, flags);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("503") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test segment method with non-2xx HTTP response
     */
    @Test
    void segment_Non2xxResponse() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
        Map<String, String> flags = new HashMap<>();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/segment")) {
                    return new MockResponse().setResponseCode(502);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.segment(sample, modalities, flags);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("502") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test convertFormatV2 method with non-2xx HTTP response
     */
    @Test
    void convertFormatV2_Non2xxResponse() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        String sourceFormat = "ISO19794_5_2011";
        String targetFormat = "IMAGE/PNG";
        Map<String, String> sourceParams = new HashMap<>();
        Map<String, String> targetParams = new HashMap<>();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/convert-format")) {
                    return new MockResponse().setResponseCode(500);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.convertFormatV2(sample, sourceFormat, targetFormat, sourceParams, targetParams, modalities);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("500") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test convertFormat method with non-2xx HTTP response
     */
    @Test
    void convertFormat_Non2xxResponse() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        String sourceFormat = "ISO19794_5_2011";
        String targetFormat = "IMAGE/PNG";
        Map<String, String> sourceParams = new HashMap<>();
        Map<String, String> targetParams = new HashMap<>();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/convert-format")) {
                    return new MockResponse().setResponseCode(500);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.convertFormat(sample, sourceFormat, targetFormat, sourceParams, targetParams, modalities);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("500") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test getSdkServiceUrl method with format from flags
     */
    @Test
    void getSdkServiceUrl_WithFormatFromFlags() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        String baseUrl = mockWebServer.url("/biosdk-service").toString();
        initParams.put("format.url.default", baseUrl);
        initParams.put("format.url.finger", baseUrl);
        initParams.put("format.url.face", baseUrl);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                        .addHeader("Content-Type", "application/json")
                        .setResponseCode(200);
            }
        });

        client.init(initParams);

        Map<String, String> flags = new HashMap<>();
        flags.put("finger.format", "finger");

        Method getSdkServiceUrl = Client_V_1_0.class.getDeclaredMethod("getSdkServiceUrl", BiometricType.class, Map.class);
        getSdkServiceUrl.setAccessible(true);

        String result = (String) getSdkServiceUrl.invoke(client, BiometricType.FINGER, flags);
        assertEquals(baseUrl, result);
    }

    /**
     * Test getSdkServiceUrl method with null modality
     */
    @Test
    void getSdkServiceUrl_WithNullModality() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        String baseUrl = mockWebServer.url("/biosdk-service").toString();
        initParams.put("format.url.default", baseUrl);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                        .addHeader("Content-Type", "application/json")
                        .setResponseCode(200);
            }
        });

        client.init(initParams);

        Method getSdkServiceUrl = Client_V_1_0.class.getDeclaredMethod("getSdkServiceUrl", BiometricType.class, Map.class);
        getSdkServiceUrl.setAccessible(true);

        String result = (String) getSdkServiceUrl.invoke(client, null, new HashMap<>());
        assertEquals(baseUrl, result);
    }

    /**
     * Test getSdkServiceUrl method with empty modalities and finger flag
     */
    @Test
    void getSdkServiceUrl_EmptyModalitiesWithFingerFlag() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        String baseUrl = mockWebServer.url("/biosdk-service").toString();
        initParams.put("format.url.default", baseUrl);
        initParams.put("format.url.finger", baseUrl);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                        .addHeader("Content-Type", "application/json")
                        .setResponseCode(200);
            }
        });

        client.init(initParams);

        Map<String, String> flags = new HashMap<>();
        flags.put("fingerTemplate", "someValue");

        Method getSdkServiceUrl = Client_V_1_0.class.getDeclaredMethod("getSdkServiceUrl", List.class, Map.class);
        getSdkServiceUrl.setAccessible(true);

        String result = (String) getSdkServiceUrl.invoke(client, new ArrayList<BiometricType>(), flags);
        assertEquals(baseUrl, result);
    }

    /**
     * Test getSdkServiceUrl method with empty modalities and iris flag
     */
    @Test
    void getSdkServiceUrl_EmptyModalitiesWithIrisFlag() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        String baseUrl = mockWebServer.url("/biosdk-service").toString();
        initParams.put("format.url.default", baseUrl);
        initParams.put("format.url.iris", baseUrl);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                        .addHeader("Content-Type", "application/json")
                        .setResponseCode(200);
            }
        });

        client.init(initParams);

        Map<String, String> flags = new HashMap<>();
        flags.put("irisTemplate", "someValue");

        Method getSdkServiceUrl = Client_V_1_0.class.getDeclaredMethod("getSdkServiceUrl", List.class, Map.class);
        getSdkServiceUrl.setAccessible(true);

        String result = (String) getSdkServiceUrl.invoke(client, new ArrayList<BiometricType>(), flags);
        assertEquals(baseUrl, result);
    }

    /**
     * Test getSdkServiceUrl method with empty modalities and face flag
     */
    @Test
    void getSdkServiceUrl_EmptyModalitiesWithFaceFlag() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        String baseUrl = mockWebServer.url("/biosdk-service").toString();
        initParams.put("format.url.default", baseUrl);
        initParams.put("format.url.face", baseUrl);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                        .addHeader("Content-Type", "application/json")
                        .setResponseCode(200);
            }
        });

        client.init(initParams);

        Map<String, String> flags = new HashMap<>();
        flags.put("faceTemplate", "someValue");

        Method getSdkServiceUrl = Client_V_1_0.class.getDeclaredMethod("getSdkServiceUrl", List.class, Map.class);
        getSdkServiceUrl.setAccessible(true);

        String result = (String) getSdkServiceUrl.invoke(client, new ArrayList<BiometricType>(), flags);
        assertEquals(baseUrl, result);
    }

    /**
     * Test setConfigParameters method functionality
     */
    @Test
    void setConfigParameters() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("config.parameter.testParam1", "value1");
        initParams.put("config.parameter.testParam2", "value2");
        initParams.put("format.url.default", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                        .addHeader("Content-Type", "application/json")
                        .setResponseCode(200);
            }
        });

        client.init(initParams);

        assertEquals("value1", System.getProperty("testParam1"));
        assertEquals("value2", System.getProperty("testParam2"));

        System.clearProperty("testParam1");
        System.clearProperty("testParam2");
    }

    /**
     * Test errorHandler method with actual errors
     */
    @Test
    void errorHandler_WithErrors() throws Exception {
        Client_V_1_0 client = new Client_V_1_0();

        List<ErrorDto> errors = new ArrayList<>();
        ErrorDto error1 = new ErrorDto();
        error1.setCode("ERR001");
        error1.setMessage("First error");

        ErrorDto error2 = new ErrorDto();
        error2.setCode("ERR002");
        error2.setMessage("Second error");

        errors.add(error1);
        errors.add(error2);

        Method errorHandlerMethod = Client_V_1_0.class.getDeclaredMethod("errorHandler", List.class);
        errorHandlerMethod.setAccessible(true);

        try {
            errorHandlerMethod.invoke(client, errors);
            fail("Expected BioSdkClientException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof BioSdkClientException);
            BioSdkClientException bioException = (BioSdkClientException) cause;
            assertTrue(bioException.getMessage().contains("ERR001"));
            assertTrue(bioException.getMessage().contains("First error"));
            assertTrue(bioException.getMessage().contains("ERR002"));
            assertTrue(bioException.getMessage().contains("Second error"));
        }
    }

    /**
     * Test init method with non-2xx HTTP response
     */
    @Test
    void init_Non2xxResponse() {
        Client_V_1_0 client = new Client_V_1_0();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse().setResponseCode(500);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
            client.init(initParams);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("500") || exception.getMessage().contains("HTTP status"));
    }

    /**
     * Test match method response with null fields
     */
    @Test
    void match_ResponseWithNullFields() throws Exception {
        Client_V_1_0 client = spy(new Client_V_1_0());
        BiometricRecord sample = new BiometricRecord();
        BiometricRecord[] gallery = new BiometricRecord[1];
        gallery[0] = new BiometricRecord();
        List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
        Map<String, String> flags = new HashMap<>();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("format.url.test", mockWebServer.url("/biosdk-service").toString());

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/biosdk-service/init")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"apiVersion\":\"1.0\",\"sdkVersion\":\"1.0\"}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                } else if (request.getPath().equals("/biosdk-service/match")) {
                    return new MockResponse()
                            .setBody("{\"response\":{\"statusCode\":null,\"statusMessage\":null,\"response\":null}}")
                            .addHeader("Content-Type", "application/json")
                            .setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        client.init(initParams);
        Response<MatchDecision[]> response = client.match(sample, gallery, modalities, flags);

        assertNotNull(response);
        assertNull(response.getStatusCode());
        assertEquals("", response.getStatusMessage());
        assertNull(response.getResponse());
    }

	// Helper method to set a system property for testing
	private void setSystemProperty(String key, String value) {
		System.setProperty(key, value);
	}

	// Helper method to clear system property after the test
	private void clearSystemProperty(String key) {
		System.clearProperty(key);
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