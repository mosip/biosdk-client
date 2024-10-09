package io.mosip.biosdk.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
		CheckQualityRequestDto dto = new CheckQualityRequestDto();

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
	void testCheckQualityRequestDtoNoArgsConstructor() {
		CheckQualityRequestDto dto = new CheckQualityRequestDto();

		assertNull(dto.getSample());
		assertNull(dto.getModalitiesToCheck());
		assertNull(dto.getFlags());
	}

	@Test
	void testCheckQualityRequestDtoToString() {
		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalitiesToCheck = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("flag1", "value1");

		CheckQualityRequestDto dto = new CheckQualityRequestDto();
		dto.setSample(sample);
		dto.setModalitiesToCheck(modalitiesToCheck);
		dto.setFlags(flags);

		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("sample="));
		assertTrue(toStringOutput.contains("modalitiesToCheck=[FINGER]"));
		assertTrue(toStringOutput.contains("flags={flag1=value1}"));
	}

	@Test
	void testCheckQualityRequestDtoEquals() {
		CheckQualityRequestDto dto1 = new CheckQualityRequestDto();
		dto1.setSample(new BiometricRecord());
		dto1.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto1.setFlags(new HashMap<>());
		CheckQualityRequestDto dto2 = new CheckQualityRequestDto();
		dto2.setSample(new BiometricRecord());
		dto2.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto2.setFlags(new HashMap<>());

		assertTrue(dto1.equals(dto2));
	}

	@Test
	void testCheckQualityRequestDtoEqualsNull() {
		CheckQualityRequestDto dto1 = new CheckQualityRequestDto();
		dto1.setSample(new BiometricRecord());
		dto1.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto1.setFlags(new HashMap<>());

		assertFalse(dto1.equals(null));
	}

	@Test
	void testCheckQualityRequestDtoHashCode() {
		CheckQualityRequestDto dto1 = new CheckQualityRequestDto();
		dto1.setSample(new BiometricRecord());
		dto1.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto1.setFlags(new HashMap<>());
		CheckQualityRequestDto dto2 = new CheckQualityRequestDto();
		dto2.setSample(new BiometricRecord());
		dto2.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto2.setFlags(new HashMap<>());

		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void testCheckQualityRequestDtoCanEqual() {
		CheckQualityRequestDto dto1 = new CheckQualityRequestDto();
		dto1.setSample(new BiometricRecord());
		dto1.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto1.setFlags(new HashMap<>());
		CheckQualityRequestDto dto2 = new CheckQualityRequestDto();
		dto2.setSample(new BiometricRecord());
		dto2.setModalitiesToCheck(List.of(BiometricType.FINGER));
		dto2.setFlags(new HashMap<>());

		assertTrue(dto1.canEqual(dto2));

		assertFalse(dto1.canEqual(new Object()));
	}

	@Test
	void testConvertFormatRequestDto() {
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

		BiometricType modality1 = BiometricType.FINGER;
		BiometricType modality2 = BiometricType.IRIS;
		List<BiometricType> modalities = Arrays.asList(modality1, modality2);
		dto.setModalitiesToConvert(modalities);

		assertEquals(sample, dto.getSample());
		assertEquals("FORMAT_A", dto.getSourceFormat());
		assertEquals("FORMAT_B", dto.getTargetFormat());
		assertEquals("value1", dto.getSourceParams().get("param1"));
		assertEquals("value2", dto.getTargetParams().get("param2"));
		assertEquals(2, dto.getModalitiesToConvert().size());
	}

	@Test
	void testConvertFormatRequestDtoNoArgsConstructor() {
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

		String toString = dto.toString();
		assertTrue(toString.contains("sample="));
		assertTrue(toString.contains("sourceFormat=FORMAT_A"));
		assertTrue(toString.contains("targetFormat=FORMAT_B"));
		assertTrue(toString.contains("sourceParams={param1=value1}"));
		assertTrue(toString.contains("targetParams={param2=value2}"));
		assertTrue(toString.contains("modalitiesToConvert=[FINGER]"));
	}

	@Test
	void testConvertFormatRequestDtoEquals() {
		ConvertFormatRequestDto dto1 = new ConvertFormatRequestDto();
		dto1.setSample(new BiometricRecord()); // Assuming BiometricRecord is properly defined
		dto1.setSourceFormat("format1");
		dto1.setTargetFormat("format2");
		dto1.setSourceParams(new HashMap<>());
		dto1.setTargetParams(new HashMap<>());
		dto1.setModalitiesToConvert(List.of(BiometricType.FINGER)); // Example type

		ConvertFormatRequestDto dto2 = new ConvertFormatRequestDto();
		dto2.setSample(new BiometricRecord());
		dto2.setSourceFormat("format1");
		dto2.setTargetFormat("format2");
		dto2.setSourceParams(new HashMap<>());
		dto2.setTargetParams(new HashMap<>());
		dto2.setModalitiesToConvert(List.of(BiometricType.FINGER));

		assertTrue(dto1.equals(dto2));
	}

	@Test
	void testConvertFormatRequestDtoHashCode() {
		ConvertFormatRequestDto dto1 = new ConvertFormatRequestDto();
		dto1.setSample(new BiometricRecord()); // Assuming BiometricRecord is properly defined
		dto1.setSourceFormat("format1");
		dto1.setTargetFormat("format2");
		dto1.setSourceParams(new HashMap<>());
		dto1.setTargetParams(new HashMap<>());
		dto1.setModalitiesToConvert(List.of(BiometricType.FINGER)); // Example type

		ConvertFormatRequestDto dto2 = new ConvertFormatRequestDto();
		dto2.setSample(new BiometricRecord());
		dto2.setSourceFormat("format1");
		dto2.setTargetFormat("format2");
		dto2.setSourceParams(new HashMap<>());
		dto2.setTargetParams(new HashMap<>());
		dto2.setModalitiesToConvert(List.of(BiometricType.FINGER));

		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void testConvertFormatRequestDtoCanEqual() {
		ConvertFormatRequestDto dto1 = new ConvertFormatRequestDto();
		dto1.setSample(new BiometricRecord()); // Assuming BiometricRecord is properly defined
		dto1.setSourceFormat("format1");
		dto1.setTargetFormat("format2");
		dto1.setSourceParams(new HashMap<>());
		dto1.setTargetParams(new HashMap<>());
		dto1.setModalitiesToConvert(List.of(BiometricType.FINGER)); // Example type

		ConvertFormatRequestDto dto2 = new ConvertFormatRequestDto();
		dto2.setSample(new BiometricRecord());
		dto2.setSourceFormat("format1");
		dto2.setTargetFormat("format2");
		dto2.setSourceParams(new HashMap<>());
		dto2.setTargetParams(new HashMap<>());
		dto2.setModalitiesToConvert(List.of(BiometricType.FINGER));

		assertTrue(dto1.canEqual(dto2));

		assertFalse(dto1.canEqual(new Object()));
	}

	@Test
	void testConvertFormatRequestDtoEqualsNull() {
		ConvertFormatRequestDto dto1 = new ConvertFormatRequestDto();
		dto1.setSample(new BiometricRecord()); // Assuming BiometricRecord is properly defined
		dto1.setSourceFormat("format1");
		dto1.setTargetFormat("format2");
		dto1.setSourceParams(new HashMap<>());
		dto1.setTargetParams(new HashMap<>());
		dto1.setModalitiesToConvert(List.of(BiometricType.FINGER)); // Example type

		assertFalse(dto1.equals(null));
	}

	@Test
	void testErrorDto() {
		ErrorDto errorDto = new ErrorDto();

		String code = "404";
		String message = "Not Found";
		errorDto.setCode(code);
		errorDto.setMessage(message);

		assertEquals("404", errorDto.getCode());
		assertEquals("Not Found", errorDto.getMessage());
	}

	@Test
	void testErrorDtoNoArgsConstructor() {
		ErrorDto errorDto = new ErrorDto();

		assertNull(errorDto.getCode());
		assertNull(errorDto.getMessage());
	}

	@Test
	void testErrorDtoToString() {
		ErrorDto errorDto = new ErrorDto("400", "Bad Request");

		String toStringOutput = errorDto.toString();
		assertTrue(toStringOutput.contains("code=400"));
		assertTrue(toStringOutput.contains("message=Bad Request"));
	}

	@Test
	void testErrorDtoEquals() {
		ErrorDto dto1 = new ErrorDto("404", "Not Found");
		ErrorDto dto2 = new ErrorDto("404", "Not Found");

		assertEquals(dto1, dto2);
		assertTrue(dto1.equals(dto2));
	}

	@Test
	void testErrorDtoEqualsNull() {
		ErrorDto dto1 = new ErrorDto("404", "Not Found");

		assertNotEquals(dto1, null); // Should not be equal to null
	}

	@Test
	void testErrorDtoHashCode() {
		ErrorDto dto1 = new ErrorDto("404", "Not Found");
		ErrorDto dto2 = new ErrorDto("404", "Not Found");

		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void testErrorDtoCanEqual() {
		ErrorDto dto1 = new ErrorDto("404", "Not Found");
		ErrorDto dto2 = new ErrorDto("404", "Not Found");

		assertTrue(dto1.canEqual(dto2));

		assertFalse(dto1.canEqual(new Object()));
	}

	@Test
	void testExtractTemplateRequestDto() {
		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();

		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER, BiometricType.IRIS);
		Map<String, String> flags = Map.of("flag1", "value1");

		dto.setSample(sample);
		dto.setModalitiesToExtract(modalities);
		dto.setFlags(flags);

		assertEquals(sample, dto.getSample());
		assertEquals(modalities, dto.getModalitiesToExtract());
		assertEquals(flags, dto.getFlags());
	}

	@Test
	void testExtractTemplateRequestDtoNoArgsConstructor() {
		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();

		assertNull(dto.getSample());
		assertNull(dto.getModalitiesToExtract());
		assertNull(dto.getFlags());
	}

	@Test
	void testExtractTemplateRequestDtoToString() {
		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("flag1", "value1");

		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();
		dto.setSample(sample);
		dto.setModalitiesToExtract(modalities);
		dto.setFlags(flags);

		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("sample="));
		assertTrue(toStringOutput.contains("modalitiesToExtract=[FINGER]"));
		assertTrue(toStringOutput.contains("flags={flag1=value1}"));
	}

	@Test
	void testExtractTemplateRequestDtoEquals() {
		BiometricRecord record = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("param1", "value1");

		ExtractTemplateRequestDto dto1 = new ExtractTemplateRequestDto();
		dto1.setSample(record);
		dto1.setModalitiesToExtract(modalities);
		dto1.setFlags(flags);

		ExtractTemplateRequestDto dto2 = new ExtractTemplateRequestDto();
		dto2.setSample(record);
		dto2.setModalitiesToExtract(modalities);
		dto2.setFlags(flags);

		assertThat(dto1.equals(dto2)).isTrue();
	}

	@Test
	void testExtractTemplateRequestDtoEqualsNull() {
		ExtractTemplateRequestDto dto = new ExtractTemplateRequestDto();
		assertThat(dto.equals(null)).isFalse();
	}

	@Test
	void testExtractTemplateRequestDtoHashCode() {
		BiometricRecord record = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("param1", "value1");

		ExtractTemplateRequestDto dto1 = new ExtractTemplateRequestDto();
		dto1.setSample(record);
		dto1.setModalitiesToExtract(modalities);
		dto1.setFlags(flags);

		ExtractTemplateRequestDto dto2 = new ExtractTemplateRequestDto();
		dto2.setSample(record);
		dto2.setModalitiesToExtract(modalities);
		dto2.setFlags(flags);

		assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
	}

	@Test
	void testExtractTemplateRequestDtoCanEqual() {
		BiometricRecord record = new BiometricRecord();
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("param1", "value1");

		ExtractTemplateRequestDto dto1 = new ExtractTemplateRequestDto();
		dto1.setSample(record);
		dto1.setModalitiesToExtract(modalities);
		dto1.setFlags(flags);

		ExtractTemplateRequestDto dto2 = new ExtractTemplateRequestDto();
		dto2.setSample(record);
		dto2.setModalitiesToExtract(modalities);
		dto2.setFlags(flags);

		assertTrue(dto1.canEqual(dto2));

		assertFalse(dto1.canEqual(new Object()));
	}

	@Test
	void testInitRequestDto() {
		InitRequestDto dto = new InitRequestDto();

		Map<String, String> initParams = Map.of("param1", "value1", "param2", "value2");
		dto.setInitParams(initParams);

		assertEquals(initParams, dto.getInitParams());
	}

	@Test
	void testInitRequestDtoNoArgsConstructor() {
		InitRequestDto dto = new InitRequestDto();

		assertNull(dto.getInitParams());
	}

	@Test
	void testInitRequestDtoToString() {
		Map<String, String> initParams = Map.of("param1", "value1", "param2", "value2");

		InitRequestDto dto = new InitRequestDto();
		dto.setInitParams(initParams);

		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("InitRequestDto"));
	}

	@Test
	void testInitRequestDtoEquals() {
		Map<String, String> initParams1 = new HashMap<>();
		initParams1.put("key1", "value1");
		InitRequestDto dto1 = new InitRequestDto();
		dto1.setInitParams(initParams1);

		Map<String, String> initParams2 = new HashMap<>();
		initParams2.put("key1", "value1");
		InitRequestDto dto2 = new InitRequestDto();
		dto2.setInitParams(initParams2);

		assertTrue(dto1.equals(dto2));
	}

	@Test
	void testInitRequestDtoEqualsDifferent() {
		Map<String, String> initParams1 = new HashMap<>();
		initParams1.put("key1", "value1");
		InitRequestDto dto1 = new InitRequestDto();
		dto1.setInitParams(initParams1);

		Map<String, String> initParams2 = new HashMap<>();
		initParams2.put("key2", "value2");
		InitRequestDto dto2 = new InitRequestDto();
		dto2.setInitParams(initParams2);

		assertFalse(dto1.equals(dto2));
	}

	@Test
	void testInitRequestDtoEqualsNull() {
		InitRequestDto dto = new InitRequestDto();
		assertThat(dto.equals(null)).isFalse();
	}

	@Test
	void testInitRequestDtoHashCode() {
		Map<String, String> initParams1 = new HashMap<>();
		initParams1.put("key1", "value1");
		InitRequestDto dto1 = new InitRequestDto();
		dto1.setInitParams(initParams1);

		Map<String, String> initParams2 = new HashMap<>();
		initParams2.put("key1", "value1");
		InitRequestDto dto2 = new InitRequestDto();
		dto2.setInitParams(initParams2);

		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void testInitRequestDtoCanEqual() {
		Map<String, String> initParams1 = new HashMap<>();
		initParams1.put("key1", "value1");
		InitRequestDto dto1 = new InitRequestDto();
		dto1.setInitParams(initParams1);

		Map<String, String> initParams2 = new HashMap<>();
		initParams2.put("key1", "value1");
		InitRequestDto dto2 = new InitRequestDto();
		dto2.setInitParams(initParams2);

		assertTrue(dto1.canEqual(dto2));

		assertFalse(dto1.canEqual(new Object()));
	}

	@Test
	void testMatchRequestDto() {
		MatchRequestDto dto = new MatchRequestDto();

		BiometricRecord sampleRecord = new BiometricRecord();
		dto.setSample(sampleRecord);

		BiometricRecord[] galleryRecords = new BiometricRecord[] { new BiometricRecord(), new BiometricRecord() };
		dto.setGallery(galleryRecords);

		List<BiometricType> modalities = List.of(BiometricType.FACE, BiometricType.FINGER);
		dto.setModalitiesToMatch(modalities);

		Map<String, String> flags = Map.of("flag1", "value1");
		dto.setFlags(flags);

		assertEquals(sampleRecord, dto.getSample());
		assertArrayEquals(galleryRecords, dto.getGallery());
		assertEquals(modalities, dto.getModalitiesToMatch());
		assertEquals(flags, dto.getFlags());
	}

	@Test
	void testMatchRequestDtoNoArgsConstructor() {
		MatchRequestDto dto = new MatchRequestDto();

		assertNull(dto.getSample());
		assertNull(dto.getGallery());
		assertNull(dto.getModalitiesToMatch());
		assertNull(dto.getFlags());
	}

	@Test
	void testMatchRequestDtoToString() {
		BiometricRecord sampleRecord = new BiometricRecord();
		BiometricRecord[] galleryRecords = new BiometricRecord[] { new BiometricRecord(), new BiometricRecord() };
		List<BiometricType> modalities = List.of(BiometricType.FINGER);
		Map<String, String> flags = Map.of("flag1", "value1");

		MatchRequestDto dto = new MatchRequestDto();
		dto.setSample(sampleRecord);
		dto.setGallery(galleryRecords);
		dto.setModalitiesToMatch(modalities);
		dto.setFlags(flags);

		String toStringOutput = dto.toString();
		assertTrue(toStringOutput.contains("sample="));
		assertTrue(toStringOutput.contains("gallery="));
		assertTrue(toStringOutput.contains("modalitiesToMatch="));
		assertTrue(toStringOutput.contains("flags={flag1=value1}"));
	}

	@Test
	void testMatchRequestDtoEquals() {
		MatchRequestDto dto1 = new MatchRequestDto();
		MatchRequestDto dto2 = new MatchRequestDto();
		assertEquals(dto1, dto2);

		dto1.setSample(new BiometricRecord());
		assertNotEquals(dto1, dto2);

		dto2.setSample(dto1.getSample());
		assertEquals(dto1, dto2);
	}

	@Test
	void testMatchRequestDtoEqualsNull() {
		MatchRequestDto dto1 = new MatchRequestDto();
		assertThat(dto1.equals(null)).isFalse();
	}

	@Test
	void testMatchRequestDtoHashCode() {
		MatchRequestDto dto1 = new MatchRequestDto();
		MatchRequestDto dto2 = new MatchRequestDto();
		assertEquals(dto1.hashCode(), dto2.hashCode()); // Check hash code for two empty DTOs

		dto1.setSample(new BiometricRecord(/* parameters */));
		assertNotEquals(dto1.hashCode(), dto2.hashCode()); // Check for inequality

		dto2.setSample(dto1.getSample());
		assertEquals(dto1.hashCode(), dto2.hashCode()); // Should be equal now
	}

	@Test
	void testMatchRequestDtoCanEqual() {
		MatchRequestDto dto = new MatchRequestDto();
		assertTrue(dto.canEqual(new MatchRequestDto())); // Should return true for the same type
		assertFalse(dto.canEqual(new Object())); // Should return false for different types
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
	void testRequestDtoEquals() {
		RequestDto dto1 = new RequestDto();
		dto1.setVersion("1.0");
		dto1.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		RequestDto dto2 = new RequestDto();
		dto2.setVersion("1.0");
		dto2.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		assertTrue(dto1.equals(dto2), "Should be equal to itself");
	}

	@Test
	void testRequestDtoEqualsNull() {
		RequestDto dto1 = new RequestDto();
		dto1.setVersion("1.0");
		dto1.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		assertFalse(dto1.equals(null), "Should not be equal to null");
	}

	@Test
	void testRequestDtoHashCode() {
		RequestDto dto1 = new RequestDto();
		dto1.setVersion("1.0");
		dto1.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		RequestDto dto2 = new RequestDto();
		dto2.setVersion("1.0");
		dto2.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		assertEquals(dto1.hashCode(), dto2.hashCode(), "Hash codes should be equal for same values");
	}

	@Test
	void testRequestDtoCanEqual() {
		RequestDto dto1 = new RequestDto();
		dto1.setVersion("1.0");
		dto1.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		RequestDto dto2 = new RequestDto();
		dto2.setVersion("1.0");
		dto2.setRequest("eyJ0ZXN0IjoiZGF0YSJ9");

		assertTrue(dto1.canEqual(dto2), "Should return true for same type");
		assertFalse(dto1.canEqual("String"), "Should return false for different types");
	}

	@Test
	void testResponseDto() {
		ResponseDto<String> dto = new ResponseDto<>();

		dto.setVersion("1.0");
		dto.setResponsetime("2023-10-08T12:00:00");
		dto.setResponse("Success");

		ErrorDto error1 = new ErrorDto("ERROR_CODE_1", "Description 1");
		ErrorDto error2 = new ErrorDto("ERROR_CODE_2", "Description 2");
		List<ErrorDto> errors = Arrays.asList(error1, error2);
		dto.setErrors(errors);

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
	void testResponseDtoEquals() {
		ResponseDto<String> dto1 = new ResponseDto<>();
		ResponseDto<String> dto2 = new ResponseDto<>();
		dto1.setVersion("1.0");
		dto1.setResponsetime("2024-10-09T12:00:00Z");
		dto1.setResponse("Success");
		dto1.setErrors(new ArrayList<>());

		dto2.setVersion("1.0");
		dto2.setResponsetime("2024-10-09T12:00:00Z");
		dto2.setResponse("Success");
		dto2.setErrors(new ArrayList<>());

		assertTrue(dto1.equals(dto2), "Should be equal if all fields are the same");
	}

	@Test
	void testResponseDtoEqualsNull() {
		ResponseDto<String> dto = new ResponseDto<>();
		assertFalse(dto.equals(null), "Should not be equal to null");
	}

	@Test
	void testResponseDtoEqualsDifferent() {
		ResponseDto<String> dto = new ResponseDto<>();
		assertFalse(dto.equals("String"), "Should not be equal to a different class");
	}

	@Test
	void testResponseDtoHashCode() {
		ResponseDto<String> dto1 = new ResponseDto<>();
		ResponseDto<String> dto2 = new ResponseDto<>();
		dto1.setVersion("1.0");
		dto1.setResponsetime("2024-10-09T12:00:00Z");
		dto1.setResponse("Success");
		dto1.setErrors(new ArrayList<>());

		dto2.setVersion("1.0");
		dto2.setResponsetime("2024-10-09T12:00:00Z");
		dto2.setResponse("Success");
		dto2.setErrors(new ArrayList<>());

		assertEquals(dto1.hashCode(), dto2.hashCode(), "Hash codes should be equal for same values");
	}

	@Test
	void testCanEqual() {
		ResponseDto<String> dto1 = new ResponseDto<>();
		ResponseDto<String> dto2 = new ResponseDto<>();
		dto1.setVersion("1.0");
		dto1.setResponsetime("2024-10-09T12:00:00Z");
		dto1.setResponse("Success");
		dto1.setErrors(new ArrayList<>());

		dto2.setVersion("1.0");
		dto2.setResponsetime("2024-10-09T12:00:00Z");
		dto2.setResponse("Success");
		dto2.setErrors(new ArrayList<>());

		assertTrue(dto1.canEqual(dto2), "Should return true for same type");
		assertFalse(dto1.canEqual("String"), "Should return false for different types");
	}

	@Test
	void testSegmentRequestDtoNoArgsConstructor() {
		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
		assertNotNull(segmentRequestDto);
	}

	@Test
	void testSegmentRequestDtoAllArgsConstructor() {
		BiometricRecord sample = new BiometricRecord();
		BiometricType modality = BiometricType.FINGER;
		Map<String, String> flags = new HashMap<>();
		flags.put("key1", "value1");

		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
		segmentRequestDto.setSample(sample);
		segmentRequestDto.setModalitiesToSegment(Collections.singletonList(modality));
		segmentRequestDto.setFlags(flags);

		assertEquals(sample, segmentRequestDto.getSample());
		assertEquals(Collections.singletonList(modality), segmentRequestDto.getModalitiesToSegment());
		assertEquals(flags, segmentRequestDto.getFlags());
	}

	@Test
	void testSegmentRequestDtoToString() {
		BiometricRecord sample = new BiometricRecord();
		BiometricType modality = BiometricType.FINGER;
		Map<String, String> flags = new HashMap<>();
		flags.put("key1", "value1");

		SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
		segmentRequestDto.setSample(sample);
		segmentRequestDto.setModalitiesToSegment(Collections.singletonList(modality));
		segmentRequestDto.setFlags(flags);

		String expectedString = "SegmentRequestDto(sample=" + sample
				+ ", modalitiesToSegment=[FINGER], flags={key1=value1})"; // Adjust based on toString

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

	@Test
	void testSegmentRequestDtoEquals() {
		SegmentRequestDto dto1 = new SegmentRequestDto();
		SegmentRequestDto dto2 = new SegmentRequestDto();

		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = new ArrayList<>();
		Map<String, String> flags = new HashMap<>();

		dto1.setSample(sample);
		dto1.setModalitiesToSegment(modalities);
		dto1.setFlags(flags);

		dto2.setSample(sample);
		dto2.setModalitiesToSegment(modalities);
		dto2.setFlags(flags);

		assertTrue(dto1.equals(dto2), "Should be equal if all fields are the same");
	}

	@Test
	void testSegmentRequestDtoEqualsDifferent() {
		SegmentRequestDto dto1 = new SegmentRequestDto();
		SegmentRequestDto dto2 = new SegmentRequestDto();

		dto1.setSample(new BiometricRecord());
		BiometricType modalityFinger = BiometricType.FINGER;
		dto1.setModalitiesToSegment(Arrays.asList(modalityFinger));
		dto1.setFlags(new HashMap<>());

		dto2.setSample(new BiometricRecord());
		BiometricType modalityIris = BiometricType.IRIS;
		dto2.setModalitiesToSegment(Arrays.asList(modalityIris));
		dto2.setFlags(new HashMap<>());

		assertFalse(dto1.equals(dto2), "Should not be equal if any field is different");
	}

	@Test
	void testSegmentRequestDtoHashCode() {
		SegmentRequestDto dto1 = new SegmentRequestDto();
		SegmentRequestDto dto2 = new SegmentRequestDto();

		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = new ArrayList<>();
		Map<String, String> flags = new HashMap<>();

		dto1.setSample(sample);
		dto1.setModalitiesToSegment(modalities);
		dto1.setFlags(flags);

		dto2.setSample(sample);
		dto2.setModalitiesToSegment(modalities);
		dto2.setFlags(flags);

		assertEquals(dto1.hashCode(), dto2.hashCode(), "Hash codes should be equal for same values");
	}
	
	@Test
    void testSegmentRequestDtoCanEqual() {
		SegmentRequestDto dto1 = new SegmentRequestDto();
		SegmentRequestDto dto2 = new SegmentRequestDto();

		BiometricRecord sample = new BiometricRecord();
		List<BiometricType> modalities = new ArrayList<>();
		Map<String, String> flags = new HashMap<>();

		dto1.setSample(sample);
		dto1.setModalitiesToSegment(modalities);
		dto1.setFlags(flags);

		dto2.setSample(sample);
		dto2.setModalitiesToSegment(modalities);
		dto2.setFlags(flags);

		assertTrue(dto1.canEqual(dto2), "Should return true for same type");
        assertFalse(dto1.canEqual("String"), "Should return false for different types");
    }
}
