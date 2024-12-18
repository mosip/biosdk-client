package io.mosip.biosdk.client.constant;

/**
 * Enumeration representing various HTTP response statuses used by the BioSDK
 * client.
 *
 * This enum defines commonly used HTTP response statuses and their
 * corresponding messages for responses generated by the BioSDK client
 * application. It provides a standardized way to communicate success or error
 * conditions to external clients.
 *
 * @since 1.0.0
 */
public enum ResponseStatus {
	/**
	 * Indicates a successful operation.
	 *
	 * This status code (200) and message ("OK") are used when the BioSDK client
	 * processes a request successfully and has a valid response to return.
	 */
	SUCCESS(200, "OK"),

	/**
	 * Indicates an invalid input parameter in the request.
	 *
	 * This status code (400) and message format ("Invalid Input Parameter - %s")
	 * are used when a required input parameter is missing, has an invalid format,
	 * or violates any defined validation rules. The specific parameter name or
	 * details can be included in the formatted message using String.format().
	 */
	INVALID_INPUT(401, "Invalid Input Parameter - %s"),

	/**
	 * Indicates a missing input parameter in the request.
	 *
	 * This status code (402) and message format ("Missing Input Parameter - %s")
	 * are used when a required input parameter is completely absent from the
	 * request. The specific parameter name can be included in the formatted message
	 * using String.format().
	 */
	MISSING_INPUT(402, "Missing Input Parameter - %s"),

	/**
	 * Indicates a failure during quality check of biometric data.
	 *
	 * This status code (403) and message ("Quality check of Biometric data failed")
	 * are used when the BioSDK client performs quality checks on biometric data and
	 * the data fails to meet the minimum quality requirements.
	 */
	QUALITY_CHECK_FAILED(403, "Quality check of Biometric data failed"),

	/**
	 * Indicates poor quality of provided data.
	 *
	 * This status code (406) and message ("Data provided is of poor quality") are
	 * used when the BioSDK client detects that the provided data, although
	 * technically valid, might be of insufficient quality for further processing.
	 */
	POOR_DATA_QUALITY(406, "Data provided is of poor quality"),

	/**
	 * Indicates an unexpected error occurred on the server.
	 *
	 * This status code (500) and message ("UNKNOWN_ERROR") are used as a catch-all
	 * for any internal server errors that may occur during processing. It's
	 * important to investigate and resolve the root cause of such errors for proper
	 * handling in future versions.
	 */
	UNKNOWN_ERROR(500, "UNKNOWN_ERROR");

	private final int statusCode;
	private final String statusMessage;

	ResponseStatus(int statusCode, String statusMessage) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}

	/**
	 * Retrieves the HTTP status code associated with this response status.
	 *
	 * @return The integer value representing the HTTP status code.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Retrieves the status message associated with this response status.
	 *
	 * @return The String message describing the response status.
	 */
	public String getStatusMessage() {
		return statusMessage;
	}
}