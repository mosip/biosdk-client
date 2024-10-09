package io.mosip.biosdk.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.biosdk.client.exception.BioSdkClientException;
import io.mosip.biosdk.client.impl.spec_1_0.Client_V_1_0;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;

class Client_V_1_0Test {
	// Mock dependencies
	private Gson gson;

	@BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks        
        gson = new GsonBuilder()
        		.serializeNulls().create();
    }
	
	@Test
	void testConstructorInitialization() {
		Client_V_1_0 client = new Client_V_1_0();
		assertNotNull(client);
	}

	@Test
	void testInit_Successful() throws BioSdkClientException {
		Client_V_1_0 client = new Client_V_1_0();

        Map<String, String> initParams = new HashMap<>();
        initParams.put("param1", "value1");
        initParams.put("param2", "value2");

		SDKInfo result = client.init(initParams);

		assertNotNull(result);
	}

	@Test
    void testInitWithValidParams() {
		Client_V_1_0 client = new Client_V_1_0();
		Map<String, String> initParams = new HashMap<>();

		initParams.put("format.url.test", "http://localhost:9099/biosdk-service");

        // Call the method
        SDKInfo result = client.init(initParams);

        // Verify and assert
        assertNotNull(result);
    }
	
	@Test
	void testCheckQuality_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(getBIRDataFromXMLType (readXmlFileAsBytes("check_quality_request.xml"), "Face"));

		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Perform the check quality operation
        Map<String, String> initParams = new HashMap<>();
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
		sample.setSegments(getBIRDataFromXMLType (readXmlFileAsBytes("matcher_request_probe.xml"), "Face"));
		BiometricRecord[] gallery = new BiometricRecord[1];
		BiometricRecord galleryInfo = new BiometricRecord();
		galleryInfo.setSegments(getBIRDataFromXMLType (readXmlFileAsBytes("matcher_request_gallery.xml"), "Face"));
		gallery[0] = galleryInfo;
		List<BiometricType> modalities = Arrays.asList(BiometricType.FACE);
		Map<String, String> flags = new HashMap<>();

		// Execute the method
        Map<String, String> initParams = new HashMap<>();
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
	}

	// Test extractTemplate method - Successful case
	@Test
	void testExtractTemplate_Success() throws Exception {
		Client_V_1_0 client = spy(new Client_V_1_0());

		BiometricRecord sample = new BiometricRecord();
		sample.setSegments(getBIRDataFromXMLType (readXmlFileAsBytes("extract_request_probe.xml"), "Finger"));
		List<BiometricType> modalitiesToExtract = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute the method
        Map<String, String> initParams = new HashMap<>();
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
	void testSegment_Success() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare inputs
		BiometricRecord biometricRecord = new BiometricRecord();
		List<BiometricType> modalitiesToSegment = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute the method
        Map<String, String> initParams = new HashMap<>();
		client.init(initParams);
		Response<BiometricRecord> response = client.segment(biometricRecord, modalitiesToSegment, flags);

		// Verify the results
		assertNotNull(response);
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getResponse());
	}

	@Test
	void testSegment_Exception() throws Exception {
		Client_V_1_0 client = new Client_V_1_0();

		// Prepare inputs
		BiometricRecord biometricRecord = new BiometricRecord();
		List<BiometricType> modalitiesToSegment = Arrays.asList(BiometricType.FINGER);
		Map<String, String> flags = new HashMap<>();

		// Execute and expect an exception
		BioSdkClientException exception = assertThrows(BioSdkClientException.class, () -> {
			client.segment(biometricRecord, modalitiesToSegment, flags);
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
		sample.setSegments(getBIRDataFromXMLType (readXmlFileAsBytes("convert_request_probe.xml"), "Face"));

		String sourceFormat = "ISO19794_5_2011";
		String targetFormat = "IMAGE/PNG";
		Map<String, String> sourceParams = new HashMap<>();
		Map<String, String> targetParams = new HashMap<>();
		List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FACE);

		// Execute the method
        Map<String, String> initParams = new HashMap<>();
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
	
	private static byte[] readXmlFileAsBytes(String fileName) throws IOException {
        // Use getClassLoader to access the resource file
        try (InputStream inputStream = Client_V_1_0Test.class.getClassLoader().getResourceAsStream(fileName);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
             
            if (inputStream == null) {
                throw new IOException("File not found: " + fileName);
            }
            
            // Read the input stream into the byte array output stream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }
	
	public static List<BIR> getBIRDataFromXMLType(byte[] xmlBytes, String type) throws Exception {
		BiometricType biometricType = null;
		List<BIR> updatedBIRList = new ArrayList<>();
		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement<BIR> jaxBir = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(xmlBytes)),
				BIR.class);
		BIR birRoot = jaxBir.getValue();
		for (BIR bir : birRoot.getBirs()) {
			if (type != null) {
				biometricType = getBiometricType(type);
				BDBInfo bdbInfo = bir.getBdbInfo();
				if (bdbInfo != null) {
					List<BiometricType> biometricTypes = bdbInfo.getType();
					if (biometricTypes != null && biometricTypes.contains(biometricType)) {
						updatedBIRList.add(bir);
					}
				}
			}
		}
		return updatedBIRList;
	}
	
	private static BiometricType getBiometricType(String type) {
		if (isInEnum(type, BiometricType.class)) {
			return BiometricType.valueOf(type);
		} else {
			if (type.equals("FMR"))
				return BiometricType.FINGER;
			else
				return BiometricType.fromValue(type);
		}
	}
	
	public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
		for (E e : enumClass.getEnumConstants()) {
			if (e.name().equals(value)) {
				return true;
			}
		}
		return false;
	}
}