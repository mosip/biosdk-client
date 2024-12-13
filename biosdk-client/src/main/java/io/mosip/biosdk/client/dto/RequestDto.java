package io.mosip.biosdk.client.dto;

import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) representing a request to a service operation.
 * Encapsulates the version of the request format and the actual request data.
 *
 * <p>
 * This class is used to structure requests sent to services, containing the
 * version of the request format and the serialized request data.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@ToString
public class RequestDto {
	/**
	 * The version of the request format.
	 */
	private String version;

	/**
	 * The serialized request data in JSON and base64 encoded.
	 */
	private String request;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RequestDto))
			return false;
		RequestDto that = (RequestDto) o;
		return Objects.equals(version, that.version) && Objects.equals(request, that.request);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, request);
	}

	public boolean canEqual(Object other) {
		return other instanceof RequestDto;
	}
}