package io.mosip.biosdk.client.test;

import io.mosip.biosdk.client.impl.spec_1_0.Client_V_1_0;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.*;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.kernel.biometrics.model.MatchDecision;

import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientRealServerTestFull {

	private static final String REAL_SERVER_URL = "http://localhost:9099/biosdk-service";
	private static Logger logger = LoggerConfig.logConfig(ClientRealServerTestFull.class);

	private Client_V_1_0 client;
	private BiometricRecord sampleRecord;

	@BeforeAll
	static void configureRealServer() {
		System.setProperty("mosip_biosdk_service", REAL_SERVER_URL);
		System.out.println("✅ Using real BioSDK server: " + REAL_SERVER_URL);
	}

	@BeforeEach
	void initClient() throws Exception {
		client = new Client_V_1_0();
		Map<String, String> initParams = new HashMap<>();
		initParams.put("format.url.test", REAL_SERVER_URL);
		io.mosip.kernel.biometrics.model.SDKInfo sdkInfo = client.init(initParams);
		assertNotNull(sdkInfo, "Init must succeed before tests");

		byte[] bdb = Files.readAllBytes(Paths.get("src/test/resources/info_left_index_auth_jp2000.iso"));

		QualityType qualityType = new QualityType();
		RegistryIDType algorithmType = new RegistryIDType("MOSIP", "ISO_FINGER_QUALITY_V1");
		qualityType.setAlgorithm(algorithmType);
		qualityType.setScore(85L);
		qualityType.setQualityCalculationFailed(null);

		BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
				.withIndex("1")
				.withEncryption(false)
				.withType(Collections.singletonList(BiometricType.FINGER))
				.withSubtype(Collections.singletonList("UNKNOWN"))
				.withLevel(ProcessedLevelType.RAW)
				.withPurpose(PurposeType.ENROLL)
				.withQuality(qualityType)
				.build();

		BIRInfo birInfo = new BIRInfo.BIRInfoBuilder()
				.withCreator("TestClient")
				.withIndex("1")
				.build();

		BIR bir = new BIR.BIRBuilder()
				.withBdb(bdb)
				.withBirInfo(birInfo)
				.withBdbInfo(bdbInfo)
				.build();

		sampleRecord = new BiometricRecord();
		sampleRecord.setSegments(Collections.singletonList(bir));
	}

	//@Test
	@Order(1)
	@DisplayName("Test extractTemplate() with real server")
	void testExtractTemplate() throws Exception {
		List<BiometricType> modalities = Collections.singletonList(BiometricType.FINGER);
		Response<BiometricRecord> response = client.extractTemplate(sampleRecord, modalities, null);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertFalse(response.getResponse().getSegments().isEmpty());
		logger.info("✅ extractTemplate returned: {}", response);
	}

	//@Test
	@Order(2)
	@DisplayName("Test segment() with real server")
	void testSegment() throws Exception {
		List<BiometricType> modalities = Collections.singletonList(BiometricType.FINGER);
		Response<BiometricRecord> response = client.segment(sampleRecord, modalities, null);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		logger.info("✅ segment returned: {}", response);
	}

	//@Test
	@Order(3)
	@DisplayName("Test convertFormatV2() with real server")
	void testConvertFormatV2() throws Exception {
		Map<String, String> sourceParams = new HashMap<>();
		Map<String, String> targetParams = new HashMap<>();

		Response<BiometricRecord> response = client.convertFormatV2(
				sampleRecord,
				"ISO19794_4_2011",
				"IMAGE/PNG",
				sourceParams,
				targetParams,
				Collections.singletonList(BiometricType.FINGER)
		);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		logger.info("✅ convertFormatV2 returned: {}", response);
	}

	//@Test
	@Order(4)
	@DisplayName("Test match() with real server")
	void testMatch() throws Exception {
		// Prepare gallery with one record (could duplicate sample for testing)
		BiometricRecord galleryRecord = new BiometricRecord();
		galleryRecord.setSegments(sampleRecord.getSegments());

		BiometricRecord[] galleryArray = new BiometricRecord[]{galleryRecord};

		List<BiometricType> modalities = Collections.singletonList(BiometricType.FINGER);

		Response<MatchDecision[]> response = client.match(
				sampleRecord,
				galleryArray,
				modalities,
				null
		);

		assertNotNull(response, "Match response should not be null");
		assertNotNull(response.getResponse(), "MatchDecision array should not be null");
		assertTrue(response.getResponse().length > 0, "At least one MatchDecision expected");

		logger.info("✅ match response: {}", Arrays.toString(response.getResponse()));
	}

	//@Test
	@Order(5)
	@DisplayName("Test checkQuality() with real server")
	void testCheckQuality() throws Exception {
		Response<QualityCheck> response = client.checkQuality(
				sampleRecord,
				Collections.singletonList(BiometricType.FINGER),
				null
		);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		logger.info("✅ checkQuality response: ", response.getResponse());
	}
}