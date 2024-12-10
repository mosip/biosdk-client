package io.mosip.biosdk.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class Client_V_1_0Test {
	private static MockWebServer mockWebServer;

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
	void testConstructorInitialization() {
		Client_V_1_0 client = new Client_V_1_0();
		assertNotNull(client);
	}

	@Test
	void testInit_Successful() throws BioSdkClientException, IOException {
		Client_V_1_0 client = new Client_V_1_0();

		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);
		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));
	    
		SDKInfo result = client.init(initParams);

		assertNotNull(result);
	}

	@Test
	void testCheckQuality_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(TestUtil.getBIRDataFromXMLType(TestUtil.readXmlFileAsBytes("check_quality_request.xml", Client_V_1_0.class), "Face"));

		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));

	    client.init(initParams);
	    
		// Mock response for /biosdk-service/check-quality 
		mockResponse = new String(TestUtil.readXmlFileAsBytes("check_quality_success_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));

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
		sample.setSegments(TestUtil.getBIRDataFromXMLType(TestUtil.readXmlFileAsBytes("matcher_request_probe.xml", Client_V_1_0.class), "Face"));
		BiometricRecord[] gallery = new BiometricRecord[1];
		BiometricRecord galleryInfo = new BiometricRecord();
		galleryInfo.setSegments(TestUtil.getBIRDataFromXMLType(TestUtil.readXmlFileAsBytes("matcher_request_gallery.xml", Client_V_1_0.class), "Face"));
		gallery[0] = galleryInfo;
		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));

	    client.init(initParams);
		
		// Mock response for /biosdk-service/match
		mockResponse = new String(TestUtil.readXmlFileAsBytes("match_success_not_match_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));
	    
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
	}

	// Test extractTemplate method - Successful case
	@Test
	void testExtractTemplate_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(TestUtil.getBIRDataFromXMLType(TestUtil.readXmlFileAsBytes("extract_request_probe.xml", Client_V_1_0.class), "Finger"));
		List<BiometricType> modalitiesToExtract = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));

	    client.init(initParams);
		
		// Mock response for /biosdk-service/extract-template
		mockResponse = new String(TestUtil.readXmlFileAsBytes("extract_template_success_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));

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
		sample.setSegments(TestUtil.getBIRDataFromXMLType(TestUtil.readXmlFileAsBytes("convert_request_probe.xml", Client_V_1_0.class), "Face"));

		String sourceFormat = "ISO19794_5_2011";
		String targetFormat = "IMAGE/PNG";
		Map<String, String> sourceParams = new HashMap<>();
		Map<String, String> targetParams = new HashMap<>();
		List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FACE);

		// Execute the init method
		Map<String, String> initParams = new HashMap<>();
		initParams.put("param1", "value1");
		initParams.put("param2", "value2");

		// Mock response for /biosdk-service/init
		String mockResponse = new String(TestUtil.readXmlFileAsBytes("init_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));

	    client.init(initParams);
	    
		// Mock response for /biosdk-service/convert-format
		mockResponse = new String(TestUtil.readXmlFileAsBytes("convert_format_success_response.json", Client_V_1_0.class), StandardCharsets.UTF_8);		
	    mockWebServer.enqueue(new MockResponse()
	            .setBody(mockResponse)
	            .addHeader("Content-Type", "application/json")
	            .setResponseCode(200));
			    
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

}