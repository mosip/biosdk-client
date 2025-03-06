package io.mosip.biosdk.client.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) representing a request to match biometric samples
 * against a gallery. Encapsulates the sample, gallery, modalities to match, and
 * additional flags for the matching operation.
 *
 * <p>
 * This class is used to structure requests for biometric matching operations,
 * containing the sample biometric record, a gallery of biometric records, the
 * types of modalities to match, and any specific flags or parameters related to
 * the matching process.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@ToString
public class MatchRequestDto {
	/**
	 * The sample biometric record to be matched against the gallery.
	 */
	private BiometricRecord sample;

	/**
	 * An array of biometric records representing the gallery against which the
	 * sample is matched.
	 */
	private BiometricRecord[] gallery;

	/**
	 * The list of biometric modalities/types to be matched.
	 */
	private List<BiometricType> modalitiesToMatch;

	/**
	 * Additional flags or parameters for configuring the matching process.
	 */
	private Map<String, String> flags;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MatchRequestDto))
			return false;

		MatchRequestDto other = (MatchRequestDto) obj;
		return Objects.equals(sample, other.sample) && Arrays.equals(gallery, other.gallery)
				&& Objects.equals(modalitiesToMatch, other.modalitiesToMatch) && Objects.equals(flags, other.flags);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(sample, modalitiesToMatch, flags); // Generate a hash code based on the fields
		result = 31 * result + Arrays.hashCode(gallery); // Incorporate gallery array into the hash code
		return result;
	}

	public boolean canEqual(Object other) {
		return other instanceof MatchRequestDto; // Check if the object is of the same class
	}
}