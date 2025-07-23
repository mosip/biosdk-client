package io.mosip.biosdk.client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.biosdk.client.dto.MatchDecisionMixin;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.SDKInfo;
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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_IDTYPE;
import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_SESSIONID;

public class Util {

	private static RestTemplate REST_TEMPLATE = null;

	private static final String debugRequestResponse = System.getenv("mosip_biosdk_request_response_debug");
    private static final String MAX_CONN_PER_ROUTE = "restTemplate-max-connection-per-route";
    private static final String MAX_TOT_CONN = "restTemplate-total-max-connections";
    private static final String SSL_BYPASS = "auth-adapter-ssl-bypass";
    private static boolean sslBypass = true;
    private static Logger utilLogger = LoggerConfig.logConfig(Util.class);
    private static ObjectMapper mapper;

    public static ObjectMapper getObjectMapper() {
        if(mapper == null) {
            mapper = new ObjectMapper().registerModule(new AfterburnerModule());
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
            module.addSerializer(byte[].class, new BytesToStringSerializer());
            module.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeSerializer());
            module.addDeserializer(SDKInfo.class, new SDKInfoDeserializer());
            mapper.addMixIn(MatchDecision.class, MatchDecisionMixin.class);
            mapper.registerModule(module);
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return mapper;
    }

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

            if(debugRequestResponse != null && debugRequestResponse.equalsIgnoreCase("y")){
                 utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Request: ", getObjectMapper().writeValueAsString(request.getBody()));
            }

            response = restTemplate.exchange(url, httpMethodType, request, responseClass);

            if(debugRequestResponse != null && debugRequestResponse.equalsIgnoreCase("y")){
                utilLogger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "Response: ", getObjectMapper().writeValueAsString(response.getBody()));
            }
        } catch (RestClientException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            ex.printStackTrace();
            throw new RestClientException("rest call failed" + ExceptionUtils.getStackTrace(ex));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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

    public static String base64Encode(String data){
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}
