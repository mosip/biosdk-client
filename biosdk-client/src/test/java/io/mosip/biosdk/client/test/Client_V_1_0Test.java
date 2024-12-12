package io.mosip.biosdk.client.test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class Client_V_1_0Test {
	private static MockWebServer mockWebServer;
	private static Logger logger = LoggerConfig.logConfig(Client_V_1_0Test.class);

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");
		
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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

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
		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

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
		assertEquals("http://test-url.com", result.get("default"));
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
        setSystemProperty("mosip_biosdk_service", "http://localhost:9099");

        // Use reflection to access the private method
        Method method = Client_V_1_0.class.getDeclaredMethod("getDefaultSdkServiceUrlFromEnv");
        method.setAccessible(true);

        // Invoke the private method
        String result = (String) method.invoke(client);

        // Assert that the system property value is returned
        assertEquals("http://localhost:9099", result);

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
        
    // Helper method to set a system property for testing
    private void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    // Helper method to clear system property after the test
    private void clearSystemProperty(String key) {
        System.clearProperty(key);
    }
}