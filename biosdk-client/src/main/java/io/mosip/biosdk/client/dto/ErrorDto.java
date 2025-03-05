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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ErrorDto errorDto = (ErrorDto) o;
		return Objects.equals(code, errorDto.code) && Objects.equals(message, errorDto.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, message);
	}

	public boolean canEqual(Object other) {
		return other instanceof ErrorDto;
	}
}