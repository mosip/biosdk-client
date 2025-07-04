package io.mosip.biosdk.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_IDTYPE;
import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_SESSIONID;

import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * Utility class providing helper methods for making RESTful API requests and
 * encoding data. Includes methods for configuring REST templates, making HTTP
 * requests with optional debug logging, and encoding data to Base64 format.
 *
 * @since 1.0.0
 */
public class Util {

	private static RestTemplate REST_TEMPLATE = null;

	private static final String debugRequestResponse = System.getenv("mosip_biosdk_request_response_debug");
    private static final String MAX_CONN_PER_ROUTE = "mosip_biosdk_max_conn_per_host";
    private static final String MAX_TOT_CONN = "mosip_biosdk_max_total_conn";
    private static final String SSL_BYPASS = "mosip_biosdk_ssl_bypass";
    private static boolean sslBypass = true;
    private static Logger utilLogger = LoggerConfig.logConfig(Util.class);

    public static ResponseEntity<?> restRequest(String url, HttpMethod httpMethodType, MediaType mediaType, Object body,
                                             Map<String, String> headersMap, Class<?> responseClass) {
        ResponseEntity<?> response = null;

        try {
            RestTemplate restTemplate = getRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            HttpEntity<?> request = null;
            if (headersMap != null) {
                headersMap.forEach((k, v) -> headers.add(k, v));
            }
            if (body != null) {
                request = new HttpEntity<>(body, headers);
            } else {
                request = new HttpEntity<>(headers);
            }

			if (getDebugRequestResponse() != null && getDebugRequestResponse().equalsIgnoreCase("y")) {
				Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
				utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Request: ", gson.toJson(request.getBody()));
			}
			response = restTemplate.exchange(url, httpMethodType, request, responseClass);

            if(debugRequestResponse != null && debugRequestResponse.equalsIgnoreCase("y")){
                utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Response: ", response.getBody().toString());
            }
        } catch (RestClientException ex) {
            ex.printStackTrace();
            throw new RestClientException("rest call failed" + ExceptionUtils.getStackTrace(ex));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RestClientException("rest call failed" + ExceptionUtils.getStackTrace(e));
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new RestClientException("rest call failed" + ExceptionUtils.getStackTrace(e));
        } catch (KeyManagementException e) {
            e.printStackTrace();
            throw new RestClientException("rest call failed" + ExceptionUtils.getStackTrace(e));
        }
        return response;
	}

    private static RestTemplate getRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if(REST_TEMPLATE == null) {
            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setMaxConnPerRoute(getMaxConnectionPerRouteFromEnv())
                    .setMaxConnTotal(getTotalMaxConnectionsFromEnv()).disableCookieManagement();
            RestTemplate restTemplate = null;
            if (getSSLBypassFromEnv()) {
                TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
                SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                        .loadTrustMaterial(null, acceptingTrustStrategy).build();
                SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
                httpClientBuilder.setSSLSocketFactory(csf);
            }
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClientBuilder.build());
            REST_TEMPLATE = new RestTemplate(requestFactory);
        }

		return REST_TEMPLATE;
	}


    private static Integer getMaxConnectionPerRouteFromEnv() {
        Integer value = System.getProperty(MAX_CONN_PER_ROUTE) != null ? Integer.parseInt(System.getProperty(MAX_CONN_PER_ROUTE)) : 20;
        utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Maximum Connection per Host: ", value.toString());
        return value;
    }

    private static Integer getTotalMaxConnectionsFromEnv() {
        Integer value = System.getProperty(MAX_TOT_CONN) != null ? Integer.parseInt(System.getProperty(MAX_TOT_CONN)) : 100;
        utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Total Maximum Connection: ", value.toString());
        return value;
    }

    private static Boolean getSSLBypassFromEnv() {
        Boolean value = System.getProperty(SSL_BYPASS) != null ? BooleanUtils.toBoolean(System.getProperty(SSL_BYPASS)) : sslBypass;
        utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "SSL Bypass Flag: ", value.toString());
        return value;
    }

//	public static RestTemplate createRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
//
//        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
//
//        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
//                .build();
//
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
//
//        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//
//        requestFactory.setHttpClient(httpClient);
//        return new RestTemplate(requestFactory);
//    }

	/**
	 * Flag indicating whether to log request and response details for debugging
	 * purposes. Set as environment variable 'mosip_biosdk_request_response_debug'.
	 */
	public static String getDebugRequestResponse() {
		if (System.getProperty("mosip_biosdk_request_response_debug") != null)
			return System.getProperty("mosip_biosdk_request_response_debug");

		return System.getenv("mosip_biosdk_request_response_debug");
	}
}