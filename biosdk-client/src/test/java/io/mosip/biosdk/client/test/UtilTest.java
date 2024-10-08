package io.mosip.biosdk.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.biosdk.client.dto.InitRequestDto;
import io.mosip.biosdk.client.dto.RequestDto;
import io.mosip.biosdk.client.utils.Util;

class UtilTest {

    String url = "http://localhost:9099/biosdk-service/init";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private Util util;

	private Gson gson;

	@BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
		gson = new GsonBuilder().serializeNulls().create();
    }

    @Test
    void testRestRequestSuccessWithBodyAndHeaders() throws JsonProcessingException {
        // Mock URL, method, headers, body, and response
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("param1", "value1");
        initParams.put("param2", "value2");
        initRequestDto.setInitParams(initParams); // Set initialization params
        
        RequestDto requestDto = generateNewRequestDto(initRequestDto);        
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Bearer token");

        // Call the method under test
        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, headersMap, Object.class);

        // Assert that the response is as expected
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRestRequestSuccessWithNullBody() {
        // Mock URL, method, headers, and response
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;

        // Call the method under test with null body
        RestClientException exception = assertThrows(RestClientException.class, () -> {
            Util.restRequest(url, method, mediaType, null, null, Object.class);
        });

        // Assert that the exception is as expected
        assertNotNull(exception);
    }

    @Test
    void testRestRequestSuccessWithoutHeaders() {
        // Mock URL, method, body, and response
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;
        
        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("param1", "value1");
        initParams.put("param2", "value2");
        initRequestDto.setInitParams(initParams); // Set initialization params
        RequestDto requestDto = generateNewRequestDto(initRequestDto);        

        // Call the method under test without headers
        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

        // Assert that the response is as expected
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRestRequestThrowsRestClientException() {
        // Mock URL, method, and exception
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;

        // Mock the exchange method to throw an exception
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenThrow(new RestClientException("rest call failed"));

        // Assert that the RestClientException is thrown
        assertThrows(RestClientException.class, () -> {
            Util.restRequest(url, method, mediaType, null, null, Object.class);
        });
    }

    @Test
    void testBase64Encode() {
        String data = "test";
        String expectedEncodedData = "dGVzdA==";  // Base64 encoded value of "test"

        String encodedData = Util.base64Encode(data);
        assertEquals(expectedEncodedData, encodedData);
    }

    @Test
    void testDebugRequestResponseLoggingEnabled() {
        // Set environment variable for debugging
        System.setProperty("mosip_biosdk_request_response_debug", "y");

        // Mock URL, method, headers, body, and response
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;

        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("param1", "value1");
        initParams.put("param2", "value2");
        initRequestDto.setInitParams(initParams); // Set initialization params
        RequestDto requestDto = generateNewRequestDto(initRequestDto);        

        // Call the method under test with debugging enabled
        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

        // Assert that the response is as expected
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Cleanup
        System.clearProperty("mosip_biosdk_request_response_debug");
    }

    @Test
    void testDebugRequestResponseLoggingDisabled() {
        // Set environment variable for debugging (disabled)
        System.setProperty("mosip_biosdk_request_response_debug", "n");

        // Mock URL, method, body, and response
        HttpMethod method = HttpMethod.POST;
        MediaType mediaType = MediaType.APPLICATION_JSON;

        InitRequestDto initRequestDto = new InitRequestDto();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("param1", "value1");
        initParams.put("param2", "value2");
        initRequestDto.setInitParams(initParams); // Set initialization params
        RequestDto requestDto = generateNewRequestDto(initRequestDto);        

        // Call the method under test with debugging disabled
        ResponseEntity<?> response = Util.restRequest(url, method, mediaType, requestDto, null, Object.class);

        // Assert that the response is as expected
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Cleanup
        System.clearProperty("mosip_biosdk_request_response_debug");
    }

	private RequestDto generateNewRequestDto(Object body) {
		RequestDto requestDto = new RequestDto();
		requestDto.setVersion("1.0");
		requestDto.setRequest(Util.base64Encode(gson.toJson(body)));
		return requestDto;
	}
}
