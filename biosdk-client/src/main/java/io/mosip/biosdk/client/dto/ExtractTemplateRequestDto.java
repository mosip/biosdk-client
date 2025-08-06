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
 * Data Transfer Object (DTO) representing a request to extract biometric
 * templates. Encapsulates the biometric sample, modalities to extract, and
 * optional flags.
 *
 * <p>
 * This class is used to structure requests for extracting biometric templates,
 * containing the biometric sample, a list of biometric types to extract
 * templates for, and optional flags for customization.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@ToString
public class ExtractTemplateRequestDto {
	/**
	 * Biometric record sample from which templates are to be extracted.
	 */
	private BiometricRecord sample;

	/**
	 * List of biometric types for which templates should be extracted.
	 */
	private List<BiometricType> modalitiesToExtract;

	/**
	 * Optional flags providing additional parameters for template extraction.
	 */
	private Map<String, String> flags;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExtractTemplateRequestDto))
			return false;

		ExtractTemplateRequestDto other = (ExtractTemplateRequestDto) obj;

		return Objects.equals(sample, other.sample) &&  
				Objects.equals(modalitiesToExtract, other.modalitiesToExtract) &&  
				Objects.equals(flags, other.flags); 
	}

	@Override
	public int hashCode() {
		return Objects.hash(sample, modalitiesToExtract, flags); 
	}

	public boolean canEqual(Object other) {
		return other instanceof ExtractTemplateRequestDto;
	}
}