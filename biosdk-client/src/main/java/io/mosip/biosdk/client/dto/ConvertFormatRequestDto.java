package io.mosip.biosdk.client.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) for requesting biometric data format conversion in
 * a biometric SDK. Encapsulates parameters required for converting biometric
 * data from one format to another.
 *
 * <p>
 * This class represents a request to convert the format of biometric data,
 * specifying the sample biometric record, source and target formats, source and
 * target parameters, and modalities to be converted.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@ToString
public class ConvertFormatRequestDto {
	/**
	 * The sample biometric record to be converted.
	 */
	private BiometricRecord sample;

	/**
	 * The source format of the biometric data.
	 */
	private String sourceFormat;

	/**
	 * The target format to which the biometric data should be converted.
	 */
	private String targetFormat;

	/**
	 * Parameters specific to the source biometric data format.
	 */
	private Map<String, String> sourceParams;

	/**
	 * Parameters specific to the target biometric data format.
	 */
	private Map<String, String> targetParams;

	/**
	 * List of biometric modalities to be converted.
	 */
	private List<BiometricType> modalitiesToConvert;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ConvertFormatRequestDto that = (ConvertFormatRequestDto) o;
		return Objects.equals(sample, that.sample) && Objects.equals(sourceFormat, that.sourceFormat)
				&& Objects.equals(targetFormat, that.targetFormat) && Objects.equals(sourceParams, that.sourceParams)
				&& Objects.equals(targetParams, that.targetParams)
				&& Objects.equals(modalitiesToConvert, that.modalitiesToConvert);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sample, sourceFormat, targetFormat, sourceParams, targetParams, modalitiesToConvert);
	}

	public boolean canEqual(Object other) {
		return other instanceof ConvertFormatRequestDto;
	}
}