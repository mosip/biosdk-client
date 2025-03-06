package io.mosip.biosdk.client.test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.mosip.biosdk.client.exception.BioSdkClientException;

class ExceptionTest {
	@Test
	void testConstructorWithErrorCodeAndMessage() {
		String errorCode = "ERROR_001";
		String errorMessage = "An error occurred";

		BioSdkClientException exception = new BioSdkClientException(errorCode, errorMessage);

		assertEquals(errorCode, exception.getErrorCode());
		assertTrue(exception.getMessage().contains(errorMessage));
		assertNull(exception.getCause()); // Ensure there is no root cause
	}

	@Test
	void testConstructorWithErrorCodeMessageAndRootCause() {
		String errorCode = "ERROR_002";
		String errorMessage = "Another error occurred";
		Throwable rootCause = new NullPointerException("Null pointer exception");

		BioSdkClientException exception = new BioSdkClientException(errorCode, errorMessage, rootCause);

		assertEquals(errorCode, exception.getErrorCode());
		assertTrue(exception.getMessage().contains(errorMessage));
		assertEquals(rootCause, exception.getCause()); // Ensure the root cause is set
	}

	@Test
	void testExceptionMessage() {
		String errorCode = "ERROR_003";
		String errorMessage = "A third error occurred";

		BioSdkClientException exception = new BioSdkClientException(errorCode, errorMessage);

		String expectedMessage = "ERROR_003: A third error occurred";
		assertTrue(exception.getMessage().contains(errorMessage));
	}
}
