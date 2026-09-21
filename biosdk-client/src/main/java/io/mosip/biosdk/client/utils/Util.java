package io.mosip.biosdk.client.utils;

import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_IDTYPE;
import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_SESSIONID;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * Utility class providing helper methods for MOSIP BioSDK client operations.
 * <p>
 * Spring Framework 7 / Boot 4 HTTP client: Apache HttpClient 5 pooling,
 * optional SSL bypass, Jackson 2 {@link ObjectMapper}, and Base64 encoding.
 * </p>
 *
 * @author MOSIP Development Team
 * @since 1.0
 */
public final class Util {

	private static final Logger UTIL_LOGGER = LoggerConfig.logConfig(Util.class);

	private static final String MAX_CONN_PER_ROUTE = "restTemplate-max-connection-per-route";
	private static final String MAX_TOT_CONN = "restTemplate-total-max-connections";
	private static final String SSL_BYPASS = "restTemplate-ssl-bypass";

	private static RestTemplate restTemplate;
	private static ObjectMapper mapper;
	private static boolean sslBypass = true;

	private Util() {
	}

	/**
	 * Provides a singleton {@link ObjectMapper} configured for BioSDK usage.
	 *
	 * @return Configured {@link ObjectMapper} instance.
	 */
	public static ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new AfterburnerModule());
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		}
		return mapper;
	}

	/**
	 * Executes an HTTP REST request to the specified URL with given parameters.
	 *
	 * @param url           Target API endpoint URL.
	 * @param httpMethodType HTTP method (GET, POST, PUT, DELETE, etc.).
	 * @param mediaType     Content type of the request body.
	 * @param body          Request payload (nullable for GET requests).
	 * @param headersMap    Additional request headers (nullable).
	 * @param responseClass Expected response type.
	 * @return {@link ResponseEntity} containing response data.
	 * @throws RestClientException If the REST call fails due to connection or server errors.
	 */
	public static ResponseEntity<?> restRequest(String url, HttpMethod httpMethodType, MediaType mediaType, Object body,
			Map<String, String> headersMap, Class<?> responseClass) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(mediaType);
			if (headersMap != null) {
				headersMap.forEach(headers::add);
			}
			HttpEntity<?> entity = body != null ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

			String debugFlag = getDebugRequestResponse();
			if ("y".equalsIgnoreCase(debugFlag)) {
				UTIL_LOGGER.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Request: ",
						getObjectMapper().writeValueAsString(entity.getBody()));
			}

			ResponseEntity<?> response = getRestTemplate().exchange(url, httpMethodType, entity, responseClass);

			if ("y".equalsIgnoreCase(debugFlag)) {
				UTIL_LOGGER.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Response: ",
						getObjectMapper().writeValueAsString(response.getBody()));
			}
			return response;
		} catch (Exception ex) {
			UTIL_LOGGER.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error ", ex);
			throw new RestClientException("rest call failed: " + ex.getMessage(), ex);
		}
	}

	private static synchronized RestTemplate getRestTemplate()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		if (restTemplate == null) {
			PoolingHttpClientConnectionManager connectionManager;
			if (Boolean.TRUE.equals(getSSLBypassFromEnv())) {
				SSLContext sslContext = SSLContexts.custom()
						.loadTrustMaterial(null, (chain, authType) -> true)
						.build();
				connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
						.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE))
						.setMaxConnPerRoute(getMaxConnectionPerRouteFromEnv())
						.setMaxConnTotal(getTotalMaxConnectionsFromEnv())
						.setDefaultConnectionConfig(ConnectionConfig.custom()
								.setConnectTimeout(Timeout.ofSeconds(5))
								.setSocketTimeout(Timeout.ofSeconds(30))
								.build())
						.build();
			} else {
				connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
						.setTlsSocketStrategy(new DefaultClientTlsStrategy(SSLContexts.createSystemDefault(),
								new DefaultHostnameVerifier()))
						.setMaxConnPerRoute(getMaxConnectionPerRouteFromEnv())
						.setMaxConnTotal(getTotalMaxConnectionsFromEnv())
						.setDefaultConnectionConfig(ConnectionConfig.custom()
								.setConnectTimeout(Timeout.ofSeconds(5))
								.setSocketTimeout(Timeout.ofSeconds(30))
								.build())
						.build();
			}

			CloseableHttpClient httpClient = HttpClients.custom()
					.setConnectionManager(connectionManager)
					.setDefaultRequestConfig(RequestConfig.custom()
							.setConnectionRequestTimeout(Timeout.ofSeconds(5))
							.setResponseTimeout(Timeout.ofSeconds(30))
							.build())
					.disableAutomaticRetries()
					.disableCookieManagement()
					.evictExpiredConnections()
					.build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
					httpClient);
			restTemplate = new RestTemplate(requestFactory);
		}
		return restTemplate;
	}

	private static Integer getMaxConnectionPerRouteFromEnv() {
		Integer value = System.getProperty(MAX_CONN_PER_ROUTE) != null
				? Integer.parseInt(System.getProperty(MAX_CONN_PER_ROUTE))
				: 20;
		UTIL_LOGGER.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Maximum Connection per Host: ", value.toString());
		return value;
	}

	private static Integer getTotalMaxConnectionsFromEnv() {
		Integer value = System.getProperty(MAX_TOT_CONN) != null
				? Integer.parseInt(System.getProperty(MAX_TOT_CONN))
				: 100;
		UTIL_LOGGER.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Total Maximum Connection: ", value.toString());
		return value;
	}

	private static Boolean getSSLBypassFromEnv() {
		Boolean value = System.getProperty(SSL_BYPASS) != null
				? BooleanUtils.toBoolean(System.getProperty(SSL_BYPASS))
				: sslBypass;
		UTIL_LOGGER.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "SSL Bypass Flag: ", value.toString());
		return value;
	}

	public static String base64Encode(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	public static String getDebugRequestResponse() {
		String property = System.getProperty("mosip_biosdk_request_response_debug");
		return property != null ? property : System.getenv("mosip_biosdk_request_response_debug");
	}
}
