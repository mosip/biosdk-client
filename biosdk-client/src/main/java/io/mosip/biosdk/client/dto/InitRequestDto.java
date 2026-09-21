package io.mosip.biosdk.client.dto;

import java.util.Map;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) representing a request to initialize a biometric
 * SDK. Encapsulates initialization parameters required for setting up the SDK.
 *
 * <p>
 * This class is used to structure requests for initializing a biometric SDK,
 * containing a map of initialization parameters needed for SDK setup.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@ToString
public class InitRequestDto {
	/**
	 * Map of initialization parameters required for setting up the biometric SDK.
	 */
	private Map<String, String> initParams;

	/**
	 * Value equality on {@link #initParams}.
	 *
	 * @param o other object
	 * @return {@code true} if maps are equal
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InitRequestDto that = (InitRequestDto) o;
		return Objects.equals(initParams, that.initParams);
	}

	/**
	 * Hash of {@link #initParams}.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(initParams);
	}

	/**
	 * Lombok-style type check used by generated equals.
	 *
	 * @param other candidate
	 * @return {@code true} if {@code other} is this DTO type
	 */
	public boolean canEqual(Object other) {
		return other instanceof InitRequestDto;
	}
}