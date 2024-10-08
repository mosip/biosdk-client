package io.mosip.biosdk.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.mosip.biosdk.client.dto.CheckQualityRequestDto;
import io.mosip.biosdk.client.dto.ConvertFormatRequestDto;
import io.mosip.biosdk.client.dto.ErrorDto;
import io.mosip.biosdk.client.dto.ExtractTemplateRequestDto;
import io.mosip.biosdk.client.dto.InitRequestDto;
import io.mosip.biosdk.client.dto.MatchRequestDto;
import io.mosip.biosdk.client.dto.RequestDto;
import io.mosip.biosdk.client.dto.ResponseDto;
import io.mosip.biosdk.client.dto.SegmentRequestDto;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

class DtoTest {
	@Test
	void testCheckQualityRequestDto() {
		// Create an instance of CheckQualityRequestDto
		CheckQualityRequestDto dto = new CheckQualityRequestDto();

		// Create test data
		BiometricRecord sample = new BiometricRecord();
		dto.setSample(sample);

		BiometricType type1 = BiometricType.FINGER;
		BiometricType type2 = BiometricType.FACE;
		List<BiometricType> modalities = Arrays.asList(type1, type2);
		dto.setModalitiesToCheck(modalities);

		Map<String, String> flags = new HashMap<>();
		flags.put("qualityFlag", "high");
		dto.setFlags(flags);

		// Test getters
		assertEquals(sample, dto.getSample());
		assertEquals(2, dto.getModalitiesToCheck().size());
		assertEquals("high", dto.getFlags().get("qualityFlag"));
	}

	@Test
	public void testCheckQualityRequestDtoNoArgsConstructor() {
		// Testing no-argument constructor
		CheckQualityRequestDto dto = new CheckQualityRequestDto();

		// Values should be null by default
		assertNull(dto.getSample());
		assertNull(dto.getModalitiesToCheck());
		assertNull(dto.getFlags());
	}

	@Test
	void testCheckQualityRequestDtoToString() {
		// Create an instance of CheckQualityRequestDto
		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalitiesToCheck = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("flag1", "value1");

		CheckQualityRequestDto dto = new CheckQualityRequestDto();
		dto.setSample(sample);
		dto.setModalitiesToCheck(modalitiesToCheck);
		dto.setFlags(flags);

		// Test the toString method
		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("sample="));
		assertTrue(toStringOutput.contains("modalitiesToCheck=[FINGER]"));
		assertTrue(toStringOutput.contains("flags={flag1=value1}"));
	}

	@Test
	void testConvertFormatRequestDto() {
		// Create an instance of ConvertFormatRequestDto
		ConvertFormatRequestDto dto = new ConvertFormatRequestDto();

		// Create test data
		BiometricRecord sample = new BiometricRecord();
		dto.setSample(sample);

		String sourceFormat = "FORMAT_A";
		String targetFormat = "FORMAT_B";
		dto.setSourceFormat(sourceFormat);
		dto.setTargetFormat(targetFormat);

		Map<String, String> sourceParams = new HashMap<>();
		sourceParams.put("param1", "value1");
		dto.setSourceParams(sourceParams);

		Map<String, String> targetParams = new HashMap<>();
		targetParams.put("param2", "value2");
		dto.setTargetParams(targetParams);

		BiometricType modality1 = BiometricType.FINGER;
		BiometricType modality2 = BiometricType.IRIS;
		List<BiometricType> modalities = Arrays.asList(modality1, modality2);
		dto.setModalitiesToConvert(modalities);

		// Test getters
		assertEquals(sample, dto.getSample());
		assertEquals("FORMAT_A", dto.getSourceFormat());
		assertEquals("FORMAT_B", dto.getTargetFormat());
		assertEquals("value1", dto.getSourceParams().get("param1"));
		assertEquals("value2", dto.getTargetParams().get("param2"));
		assertEquals(2, dto.getModalitiesToConvert().size());
	}

	@Test
	void testConvertFormatRequestDtoNoArgsConstructor() {
		// Testing the no-argument constructor
		ConvertFormatRequestDto dto = new ConvertFormatRequestDto();
		assertNull(dto.getSample());
		assertNull(dto.getSourceFormat());
		assertNull(dto.getTargetFormat());
		assertNull(dto.getSourceParams());
		assertNull(dto.getTargetParams());
		assertNull(dto.getModalitiesToConvert());
	}

	@Test
	void testConvertFormatRequestDtoToString() {
		// Create an instance of ConvertFormatRequestDto
		ConvertFormatRequestDto dto = new ConvertFormatRequestDto();

		BiometricRecord sample = new BiometricRecord();
		dto.setSample(sample);

		String sourceFormat = "FORMAT_A";
		String targetFormat = "FORMAT_B";
		dto.setSourceFormat(sourceFormat);
		dto.setTargetFormat(targetFormat);

		Map<String, String> sourceParams = new HashMap<>();
		sourceParams.put("param1", "value1");
		dto.setSourceParams(sourceParams);

		Map<String, String> targetParams = new HashMap<>();
		targetParams.put("param2", "value2");
		dto.setTargetParams(targetParams);

		BiometricType modality = BiometricType.FINGER;
		dto.setModalitiesToConvert(Arrays.asList(modality));

		// Test the toString method
		String toString = dto.toString();
		assertTrue(toString.contains("sample="));
		assertTrue(toString.contains("sourceFormat=FORMAT_A"));
		assertTrue(toString.contains("targetFormat=FORMAT_B"));
		assertTrue(toString.contains("sourceParams={param1=value1}"));
		assertTrue(toString.contains("targetParams={param2=value2}"));
		assertTrue(toString.contains("modalitiesToConvert=[FINGER]"));
	}

	@Test
	void testErrorDto() {
		// Create an instance of ErrorDto
		ErrorDto errorDto = new ErrorDto();

		// Set values
		String code = "404";
		String message = "Not Found";
		errorDto.setCode(code);
		errorDto.setMessage(message);

		// Test getters
		assertEquals("404", errorDto.getCode());
		assertEquals("Not Found", errorDto.getMessage());
	}

	@Test
	void testErrorDtoNoArgsConstructor() {
		// Testing no-argument constructor
		ErrorDto errorDto = new ErrorDto();

		// Values should be null by default
		assertNull(errorDto.getCode());
		assertNull(errorDto.getMessage());
	}

	@Test
	void testErrorDtoToString() {
		// Create an instance of ErrorDto
		ErrorDto errorDto = new ErrorDto("400", "Bad Request");

		// Test the toString method
		String toStringOutput = errorDto.toString();
		assertTrue(toStringOutput.contains("code=400"));
		assertTrue(toStringOutput.contains("message=Bad Request"));
	}

	@Test
	void testExtractTemplateRequestDto() {
		// Create an instance of ExtractTemplateRequestDto
		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();

		// Set values
		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER, BiometricType.IRIS);
		Map<String, String> flags = Map.of("flag1", "value1");

		dto.setSample(sample);
		dto.setModalitiesToExtract(modalities);
		dto.setFlags(flags);

		// Test getters
		assertEquals(sample, dto.getSample());
		assertEquals(modalities, dto.getModalitiesToExtract());
		assertEquals(flags, dto.getFlags());
	}

	@Test
	void testExtractTemplateRequestDtoNoArgsConstructor() {
		// Testing no-argument constructor
		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();

		// Values should be null by default
		assertNull(dto.getSample());
		assertNull(dto.getModalitiesToExtract());
		assertNull(dto.getFlags());
	}

	@Test
	void testExtractTemplateRequestDtoToString() {
		// Create an instance of ExtractTemplateRequestDto
		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("flag1", "value1");

		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();
		dto.setSample(sample);
		dto.setModalitiesToExtract(modalities);
		dto.setFlags(flags);

		// Test the toString method
		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("sample="));
		assertTrue(toStringOutput.contains("modalitiesToExtract=[FINGER]"));
		assertTrue(toStringOutput.contains("flags={flag1=value1}"));
	}

	@Test
	void testInitRequestDto() {
		// Create an instance of InitRequestDto
		InitRequestDto dto = new InitRequestDto();

		// Set values
		Map<String, String> initParams = Map.of("param1", "value1", "param2", "value2");
		dto.setInitParams(initParams);

		// Test getters
		assertEquals(initParams, dto.getInitParams());
	}

	@Test
	void testInitRequestDtoNoArgsConstructor() {
		// Testing no-argument constructor
		InitRequestDto dto = new InitRequestDto();

		// Value should be null by default
		assertNull(dto.getInitParams());
	}

	@Test
	void testInitRequestDtoToString() {
		// Create an instance of InitRequestDto
		Map<String, String> initParams = Map.of("param1", "value1", "param2", "value2");

		InitRequestDto dto = new InitRequestDto();
		dto.setInitParams(initParams);

		// Test the toString method
		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("InitRequestDto"));
	}

	@Test
	void testMatchRequestDto() {
		// Create an instance of MatchRequestDto
		MatchRequestDto dto = new MatchRequestDto();

		// Create sample biometric record
		BiometricRecord sampleRecord = new BiometricRecord();
		dto.setSample(sampleRecord);

		// Create gallery of biometric records
		BiometricRecord[] galleryRecords = new BiometricRecord[] { new BiometricRecord(), new BiometricRecord() };
		dto.setGallery(galleryRecords);

		// Create list of modalities to match
		List<BiometricType> modalities = List.of(BiometricType.FACE, BiometricType.FINGER);
		dto.setModalitiesToMatch(modalities);

		// Set flags
		Map<String, String> flags = Map.of("flag1", "value1");
		dto.setFlags(flags);

		// Test getters
		assertEquals(sampleRecord, dto.getSample());
		assertArrayEquals(galleryRecords, dto.getGallery());
		assertEquals(modalities, dto.getModalitiesToMatch());
		assertEquals(flags, dto.getFlags());
	}

	@Test
	void testMatchRequestDtoNoArgsConstructor() {
		// Testing no-argument constructor
		MatchRequestDto dto = new MatchRequestDto();

		// Value should be null by default
		assertNull(dto.getSample());
		assertNull(dto.getGallery());
		assertNull(dto.getModalitiesToMatch());
		assertNull(dto.getFlags());
	}

	@Test
	void testMatchRequestDtoToString() {
		// Create an instance of MatchRequestDto
		BiometricRecord sampleRecord = new BiometricRecord();
		BiometricRecord[] galleryRecords = new BiometricRecord[] { new BiometricRecord(), new BiometricRecord() };
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("flag1", "value1");

		MatchRequestDto dto = new MatchRequestDto();
		dto.setSample(sampleRecord);
		dto.setGallery(galleryRecords);
		dto.setModalitiesToMatch(modalities);
		dto.setFlags(flags);

		// Test the toString method
		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("sample="));
		assertTrue(toStringOutput.contains("gallery="));
		assertTrue(toStringOutput.contains("modalitiesToMatch="));
		assertTrue(toStringOutput.contains("flags={flag1=value1}"));
	}

	@Test
	void testRequestDtoNoArgsConstructor() {
		RequestDto requestDto = new RequestDto();
		assertNotNull(requestDto);
	}

	@Test
	void testRequestDtoAllArgsConstructor() {
		RequestDto requestDto = new RequestDto();
		requestDto.setVersion("1.0");
		requestDto.setRequest("eyJ0ZXN0IjoiZGF0YSJ9"); // Example base64-encoded JSON

		assertEquals("1.0", requestDto.getVersion());
		assertEquals("eyJ0ZXN0IjoiZGF0YSJ9", requestDto.getRequest());
	}

	@Test
	void testRequestDtoToString() {
		RequestDto requestDto = new RequestDto();
		requestDto.setVersion("1.0");
		requestDto.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		String expected = "RequestDto(version=1.0, request=eyJ0ZXN0IjoiZGF0YSJ9)";
		assertEquals(expected, requestDto.toString());
	}

	@Test
	void testRequestDto() {
		RequestDto requestDto = new RequestDto();
		requestDto.setVersion("1.0");
		requestDto.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		assertEquals("1.0", requestDto.getVersion());
		assertEquals("eyJ0ZXN0IjoiZGF0YSJ9", requestDto.getRequest());
	}

	@Test
	void testResponseDto() {
		// Create an instance of ResponseDto
		ResponseDto<String> dto = new ResponseDto<>();

		// Test setters
		dto.setVersion("1.0");
		dto.setResponsetime("2023-10-08T12:00:00");
		dto.setResponse("Success");

		ErrorDto error1 = new ErrorDto("ERROR_CODE_1", "Description 1");
		ErrorDto error2 = new ErrorDto("ERROR_CODE_2", "Description 2");
		List<ErrorDto> errors = Arrays.asList(error1, error2);
		dto.setErrors(errors);

		// Test getters
		assertEquals("1.0", dto.getVersion());
		assertEquals("2023-10-08T12:00:00", dto.getResponsetime());
		assertEquals("Success", dto.getResponse());
		assertEquals(2, dto.getErrors().size());
		assertEquals("ERROR_CODE_1", dto.getErrors().get(0).getCode());
	}

	@Test
	void testResponseDtoNoArgsConstructor() {
		ResponseDto<Object> responseDto = new ResponseDto<>();
		assertNotNull(responseDto);
	}

	@Test
	void testResponseDtoAllArgsConstructor() {
		ErrorDto error = new ErrorDto("404", "Not Found");
		ResponseDto<String> responseDto = new ResponseDto<>();
		responseDto.setVersion("1.0");
		responseDto.setResponsetime("2024-10-08T12:00:00Z");
		responseDto.setResponse("Success");
		responseDto.setErrors(Collections.singletonList(error));

		assertEquals("1.0", responseDto.getVersion());
		assertEquals("2024-10-08T12:00:00Z", responseDto.getResponsetime());
		assertEquals("Success", responseDto.getResponse());
		assertEquals(Collections.singletonList(error), responseDto.getErrors());
	}

	@Test
	void testResponseDtoToString() {
		ErrorDto error = new ErrorDto("404", "Not Found");
		ResponseDto<String> responseDto = new ResponseDto<>();
		responseDto.setVersion("1.0");
		responseDto.setResponsetime("2024-10-08T12:00:00Z");
		responseDto.setResponse("Success");
		responseDto.setErrors(Collections.singletonList(error));

		String expected = "ResponseDto(version=1.0, responsetime=2024-10-08T12:00:00Z, response=Success, errors=[ErrorDto(code=404, message=Not Found)])";
		assertEquals(expected, responseDto.toString());
	}

	@Test
	void testSegmentRequestDtoNoArgsConstructor() {
		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
		assertNotNull(segmentRequestDto);
	}

	@Test
	void testSegmentRequestDtoAllArgsConstructor() {
		// Setup
		BiometricRecord sample = new BiometricRecord();
		BiometricType modality = BiometricType.FINGER; 
		Map<String, String> flags = new HashMap<>();
		flags.put("key1", "value1");

		// Create SegmentRequestDto
		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
		segmentRequestDto.setSample(sample);
		segmentRequestDto.setModalitiesToSegment(Collections.singletonList(modality));
		segmentRequestDto.setFlags(flags);

		// Assertions
		assertEquals(sample, segmentRequestDto.getSample());
		assertEquals(Collections.singletonList(modality), segmentRequestDto.getModalitiesToSegment());
		assertEquals(flags, segmentRequestDto.getFlags());
	}

	@Test
	void testSegmentRequestDtoToString() {
		// Setup
		BiometricRecord sample = new BiometricRecord();
		BiometricType modality = BiometricType.FINGER; 
		Map<String, String> flags = new HashMap<>();
		flags.put("key1", "value1");

		// Create SegmentRequestDto
		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
		segmentRequestDto.setSample(sample);
		segmentRequestDto.setModalitiesToSegment(Collections.singletonList(modality));
		segmentRequestDto.setFlags(flags);

		// Expected string representation
		String expectedString = "SegmentRequestDto(sample=" + sample
				+ ", modalitiesToSegment=[FINGER], flags={key1=value1})"; // Adjust based on toString

		// Assertion
		assertEquals(expectedString, segmentRequestDto.toString());
	}

	@Test
	void testSegmentRequestDto() {
		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();

		BiometricRecord sample = new BiometricRecord();
		segmentRequestDto.setSample(sample);

		BiometricType modality = BiometricType.FINGER; 
		segmentRequestDto.setModalitiesToSegment(Arrays.asList(modality));

		Map<String, String> flags = new HashMap<>();
		flags.put("flag1", "value1");
		segmentRequestDto.setFlags(flags);

		// Assertions
		assertThat(segmentRequestDto.getSample()).isEqualTo(sample);
		assertThat(segmentRequestDto.getModalitiesToSegment()).containsExactly(modality);
		assertThat(segmentRequestDto.getFlags()).isEqualTo(flags);
	}
}
