package io.mosip.biosdk.client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_IDTYPE;
import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_SESSIONID;

/**
 * Utility class providing helper methods for MOSIP BioSDK client operations.
 * <p>
 * Includes JSON object mapping, HTTP REST requests with connection pooling,
 * SSL certificate bypass handling, and Base64 encoding utilities.
 * </p>
 *
 * @author
 * MOSIP Development Team
 * @since 1.0
 */
public class Util {

    private static Logger utilLogger = LoggerConfig.logConfig(Util.class);
    private static RestTemplate REST_TEMPLATE = null;

    private static final String debugRequestResponse = System.getenv("mosip_biosdk_request_response_debug");
    private static final String MAX_CONN_PER_ROUTE = "restTemplate-max-connection-per-route";
    private static final String MAX_TOT_CONN = "restTemplate-total-max-connections";
    private static final String SSL_BYPASS = "restTemplate-ssl-bypass";
    private static boolean sslBypass = true;
    private static ObjectMapper mapper;

    /**
     * Provides a singleton {@link ObjectMapper} configured for BioSDK usage.
     * <ul>
     *     <li>Registers Afterburner module for faster processing.</li>
     *     <li>Ignores unknown JSON properties during deserialization.</li>
     *     <li>Prevents writing dates as timestamps.</li>
     * </ul>
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
     * <p>
     * Logs request and response details when debugging is enabled using the
     * environment variable <code>mosip_biosdk_request_response_debug=y</code>.
     * </p>
     *
     * @param url            Target API endpoint URL.
     * @param httpMethodType HTTP method (GET, POST, PUT, DELETE, etc.).
     * @param mediaType      Content type of the request body.
     * @param body           Request payload (nullable for GET requests).
     * @param headersMap     Additional request headers (nullable).
     * @param responseClass  Expected response type.
     * @return {@link ResponseEntity} containing response data.
     * @throws RestClientException If the REST call fails due to connection or server errors.
     */
    public static ResponseEntity<?> restRequest(String url, HttpMethod httpMethodType, MediaType mediaType, Object body,
                                                Map<String, String> headersMap, Class<?> responseClass) {
        ResponseEntity<?> response = null;

        try {
            RestTemplate restTemplate = getRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            HttpEntity<?> request = (body != null) ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers);
            if (headersMap != null) {
                headersMap.forEach(headers::add);
            }

            if ("y".equalsIgnoreCase(debugRequestResponse)) {
                utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Request: ",
                        getObjectMapper().writeValueAsString(request.getBody()));
            }

            response = restTemplate.exchange(url, httpMethodType, request, responseClass);

            if ("y".equalsIgnoreCase(debugRequestResponse)) {
                utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Response: ",
                        getObjectMapper().writeValueAsString(response.getBody()));
            }
        } catch (Exception ex) {
            utilLogger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error ", ex);
            throw new RestClientException("rest call failed" + ExceptionUtils.getStackTrace(ex));
        }
        return response;
    }

    /**
     * Returns a singleton {@link RestTemplate} instance configured with:
     * <ul>
     *     <li>Connection pooling.</li>
     *     <li>Optional SSL certificate validation bypass (for dev/test).</li>
     * </ul>
     *
     * @return Configured {@link RestTemplate} instance.
     * @throws NoSuchAlgorithmException If SSL algorithm is unavailable.
     * @throws KeyStoreException        If keystore initialization fails.
     * @throws KeyManagementException   If SSL context initialization fails.
     */
    private static synchronized RestTemplate getRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (REST_TEMPLATE == null) {
            // Configure connection manager for pooling
            PoolingHttpClientConnectionManager connectionManager;
            if (getSSLBypassFromEnv()) {
                // Create an SSL context that trusts all certificates
                SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial(null, (chain, authType) -> true)
                        .build();
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                        sslContext, new String[]{TLS.V_1_3.toString(), TLS.V_1_2.toString()}, null, NoopHostnameVerifier.INSTANCE);
                connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(socketFactory)
                        .setMaxConnPerRoute(getMaxConnectionPerRouteFromEnv())
                        .setMaxConnTotal(getTotalMaxConnectionsFromEnv())
                        .build();
            } else {
                connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnPerRoute(getMaxConnectionPerRouteFromEnv())
                        .setMaxConnTotal(getTotalMaxConnectionsFromEnv())
                        .build();
            }

            // Configure HttpClient
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .disableCookieManagement()
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            REST_TEMPLATE = new RestTemplate(requestFactory);
        }
        return REST_TEMPLATE;
    }

    /**
     * Reads the maximum allowed concurrent connections per route from system properties.
     * Defaults to 20 if not set.
     *
     * @return Max connections per route.
     */
    private static Integer getMaxConnectionPerRouteFromEnv() {
        Integer value = System.getProperty(MAX_CONN_PER_ROUTE) != null ?
                Integer.parseInt(System.getProperty(MAX_CONN_PER_ROUTE)) : 20;
        utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Maximum Connection per Host: ", value.toString());
        return value;
    }

    /**
     * Reads the total maximum allowed concurrent connections from system properties.
     * Defaults to 100 if not set.
     *
     * @return Total max connections.
     */
    private static Integer getTotalMaxConnectionsFromEnv() {
        Integer value = System.getProperty(MAX_TOT_CONN) != null ?
                Integer.parseInt(System.getProperty(MAX_TOT_CONN)) : 100;
        utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Total Maximum Connection: ", value.toString());
        return value;
    }

    /**
     * Determines whether SSL certificate validation should be bypassed.
     * Defaults to true for non-production environments.
     *
     * @return true if SSL validation is bypassed, otherwise false.
     */
    private static Boolean getSSLBypassFromEnv() {
        Boolean value = System.getProperty(SSL_BYPASS) != null ?
                BooleanUtils.toBoolean(System.getProperty(SSL_BYPASS)) : sslBypass;
        utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "SSL Bypass Flag: ", value.toString());
        return value;
    }

    /**
     * Encodes a given string into its Base64 representation.
     *
     * @param data Input string to be encoded.
     * @return Base64-encoded string.
     */
    public static String base64Encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}