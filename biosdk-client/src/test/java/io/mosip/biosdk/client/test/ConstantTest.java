package io.mosip.biosdk.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.mosip.biosdk.client.constant.AppConstants;

class ConstantTest {
	@Test
	void testAppConstants() {
		assertEquals("BIO-SDK-CLIENT", AppConstants.LOGGER_SESSIONID);
		assertEquals("BIO-SDK-CLIENT", AppConstants.LOGGER_IDTYPE);
	}

	@Test
	void testAppConstantsPrivateConstructor() {
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			new AppConstants(); // This should throw an exception
		});
		assertEquals("AppConstants class", exception.getMessage());
	}
}