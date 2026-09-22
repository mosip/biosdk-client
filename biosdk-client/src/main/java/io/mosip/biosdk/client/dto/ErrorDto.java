package io.mosip.biosdk.client.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) representing an error in a biometric SDK
 * operation. Encapsulates error code and message for detailed error reporting.
 *
 * <p>
 * This class is used to represent errors encountered during biometric SDK
 * operations. It includes attributes for error code and a descriptive error
 * message.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorDto {
	/**
	 * Error code identifying the specific error condition.
	 */
	private String code;

	/**
	 * Detailed message providing information about the error.
	 */
	private String message;

	/**
	 * Value equality on code and message.
	 *
	 * @param o other object
	 * @return {@code true} if fields match
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ErrorDto errorDto = (ErrorDto) o;
		return Objects.equals(code, errorDto.code) && Objects.equals(message, errorDto.message);
	}

	/**
	 * Hash of code and message.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(code, message);
	}

	/**
	 * Lombok-style type check used by generated equals.
	 *
	 * @param other candidate
	 * @return {@code true} if {@code other} is this DTO type
	 */
	public boolean canEqual(Object other) {
		return other instanceof ErrorDto;
	}
}