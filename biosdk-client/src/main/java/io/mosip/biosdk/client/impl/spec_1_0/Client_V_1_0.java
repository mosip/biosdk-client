package io.mosip.biosdk.client.impl.spec_1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.mosip.biosdk.client.config.LoggerConfig;
import io.mosip.biosdk.client.constant.ResponseStatus;
import io.mosip.biosdk.client.dto.*;
import io.mosip.biosdk.client.exception.BioSdkClientException;
import io.mosip.biosdk.client.utils.Util;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApiV2;
import io.mosip.kernel.core.logger.spi.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_IDTYPE;
import static io.mosip.biosdk.client.constant.AppConstants.LOGGER_SESSIONID;

/**
 * The Class BioApiImpl.
 *
 * @author Sanjay Murali
 * @author Manoj SP
 * @author Ankit
 * @author Loganathan Sekar
 */
public class Client_V_1_0 implements IBioApiV2 {
    private static Logger logger = LoggerConfig.logConfig(Client_V_1_0.class);

    private static final String FORMAT_SUFFIX = ".format";

    private static final String DEFAULT = "default";

    private static final String FORMAT_URL_PREFIX = "format.url.";

    private static final String PARAMETER_PREFIX = "config.parameter.";

    private static final String MOSIP_BIOSDK_SERVICE = "mosip_biosdk_service";

    private static final String VERSION = "1.0";

    private TypeReference<List<ErrorDto>> errorDtoListTypeRef = new TypeReference<List<ErrorDto>>() {
    };

    private static final ObjectMapper M = Util.getObjectMapper();
    private static final ObjectReader ERR_LIST_READER =
            M.readerFor(new TypeReference<List<ErrorDto>>() {
            });
    private static final ObjectReader SDKINFO_READER =
            M.readerFor(SDKInfo.class);
    private static final ObjectReader MATCH_DECISIONS_READER =
            M.readerFor(new TypeReference<MatchDecision[]>() {
            });
    private static final ObjectReader BIOREC_READER =
            M.readerFor(BiometricRecord.class);
    private static final ObjectReader QUALITY_READER =
            M.readerFor(QualityCheck.class);

    private Map<String, String> sdkUrlsMap;

    private static final String TAG_HTTP_URL = "HTTP url: ";
    private static final String TAG_HTTP_STATUS = "HTTP status: ";
    private static final String TAG_ERRORS = "errors";
    private static final String TAG_RESPONSE = "response";
    private static final String TAG_STATUS_CODE = "statusCode";
    private static final String TAG_STATUS_MESSAGE = "statusMessage";
    private static final String TAG_RESPONSE_NULL = "Response body is null";


    /**
     * Initializes the BioSDK client using the provided initialization parameters.
     *
     * @param initParams A map of initialization parameters including SDK URLs and configuration values.
     * @return An aggregated {@link SDKInfo} object containing combined SDK information.
     */
    @Override
    public SDKInfo init(Map<String, String> initParams) {
        sdkUrlsMap = getSdkUrls(initParams);
        setConfigParameters(initParams);
        List<SDKInfo> sdkInfos = sdkUrlsMap.values()
                .stream()
                .map(sdkUrl -> initForSdkUrl(initParams, sdkUrl))
                .collect(Collectors.toList());
        return getAggregatedSdkInfo(sdkInfos);
    }

    /**
     * Sets configuration parameters as system properties from the initialization parameters.
     *
     * @param initParams A map of key-value pairs where keys containing the defined prefix will be set as system properties.
     */
    private void setConfigParameters(Map<String, String> initParams) {
        Map<String, String> parametersMap = new HashMap<>(initParams.entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains(PARAMETER_PREFIX))
                .collect(Collectors.toMap(entry -> entry.getKey()
                        .substring(PARAMETER_PREFIX.length()), Entry::getValue)));

        for (Map.Entry<String, String> map : parametersMap.entrySet()) {
            System.setProperty(map.getKey(), map.getValue());
        }
    }

    /**
     * Aggregates SDK information from multiple SDK instances.
     *
     * @param sdkInfos A list of {@link SDKInfo} objects retrieved from different SDK URLs.
     * @return A single aggregated {@link SDKInfo} object combining relevant details.
     */
    private SDKInfo getAggregatedSdkInfo(List<SDKInfo> sdkInfos) {
        SDKInfo sdkInfo;
        if (!sdkInfos.isEmpty()) {
            sdkInfo = sdkInfos.get(0);
            if (sdkInfos.size() == 1) {
                return sdkInfo;
            } else {
                return getAggregatedSdkInfo(sdkInfos, sdkInfo);
            }
        } else {
            sdkInfo = null;
        }
        return sdkInfo;
    }

    /**
     * Combines details from multiple {@link SDKInfo} objects into a single aggregated SDKInfo.
     *
     * @param sdkInfos A list of SDKInfo objects retrieved from multiple SDK URLs.
     * @param sdkInfo  The base SDKInfo object used as the primary reference for aggregation.
     * @return An aggregated SDKInfo object containing combined API version, SDK version, organization, and type.
     */
    private SDKInfo getAggregatedSdkInfo(List<SDKInfo> sdkInfos, SDKInfo sdkInfo) {
        String organization = sdkInfo.getProductOwner() == null ? null : sdkInfo.getProductOwner().getOrganization();
        String type = sdkInfo.getProductOwner() == null ? null : sdkInfo.getProductOwner().getType();
        SDKInfo aggregatedSdkInfo = new SDKInfo(sdkInfo.getApiVersion(), sdkInfo.getSdkVersion(), organization, type);
        sdkInfos.forEach(info -> addOtherSdkInfoDetails(info, aggregatedSdkInfo));
        return aggregatedSdkInfo;
    }

    /**
     * Adds other SDK details such as additional info, supported methods, and supported modalities
     * to the aggregated SDKInfo object while avoiding duplicates.
     *
     * @param sdkInfo           The source {@link SDKInfo} object from which additional information is collected.
     * @param aggregatedSdkInfo The aggregated {@link SDKInfo} object being updated with additional details.
     */
    private void addOtherSdkInfoDetails(SDKInfo sdkInfo, SDKInfo aggregatedSdkInfo) {
        if (sdkInfo.getOtherInfo() != null) {
            aggregatedSdkInfo.getOtherInfo().putAll(sdkInfo.getOtherInfo());
        }
        if (sdkInfo.getSupportedMethods() != null) {
            aggregatedSdkInfo.getSupportedMethods().putAll(sdkInfo.getSupportedMethods());
        }
        if (sdkInfo.getSupportedModalities() != null) {
            List<BiometricType> supportedModalities = aggregatedSdkInfo.getSupportedModalities();
            supportedModalities.addAll(sdkInfo.getSupportedModalities()
                    .stream()
                    .filter(s -> !supportedModalities.contains(s))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Initializes the SDK for a specific service URL by sending an init request and parsing the response.
     * Handles any errors encountered during initialization.
     *
     * @param initParams    A map of initialization parameters to be included in the init request.
     * @param sdkServiceUrl The URL of the SDK service to be initialized.
     * @return A {@link SDKInfo} object containing details about the initialized SDK.
     * @throws BioSdkClientException if there is an error during HTTP communication or response parsing.
     */
    private SDKInfo initForSdkUrl(Map<String, String> initParams, String sdkServiceUrl) {
        try {
            InitRequestDto initRequestDto = new InitRequestDto();
            initRequestDto.setInitParams(initParams);

            RequestDto requestDto = generateNewRequestDto(initRequestDto);
            ResponseEntity<?> responseEntity = Util.restRequest(sdkServiceUrl + "/init", HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }
            String responseBody = (String) Objects.requireNonNull(responseEntity.getBody(), TAG_RESPONSE_NULL);
            if (responseBody.equalsIgnoreCase(TAG_RESPONSE_NULL)) {
                throw new NullPointerException(TAG_RESPONSE_NULL);
            }
            com.fasterxml.jackson.databind.JsonNode root = M.readTree(responseBody);
            handleErrors(root.path(TAG_ERRORS));

            com.fasterxml.jackson.databind.JsonNode respNode = root.path(TAG_RESPONSE);
            return SDKINFO_READER.readValue(respNode.traverse());
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR + "", e.getLocalizedMessage(), e);
        }
    }

    /**
     * Extracts SDK URLs from the initialization parameters. If no default URL is provided, it attempts to fetch one from the environment variables.
     * Ensures that at least one default URL is available.
     *
     * @param initParams A map containing initialization parameters including SDK URLs with a specific prefix.
     * @return A map where keys represent format names and values represent SDK service URLs.
     * @throws IllegalStateException if no valid SDK service URL is configured.
     */
    private Map<String, String> getSdkUrls(Map<String, String> initParams) {
        Map<String, String> sdkUrls = new HashMap<>(initParams.entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains(FORMAT_URL_PREFIX))
                .collect(Collectors.toMap(entry -> entry.getKey()
                        .substring(FORMAT_URL_PREFIX.length()), Entry::getValue)));
        if (!sdkUrls.containsKey(DEFAULT)) {
            //If default is not specified in configuration, try getting it from env.
            String defaultSdkServiceUrl = getDefaultSdkServiceUrlFromEnv();
            if (defaultSdkServiceUrl != null) {
                sdkUrls.put(DEFAULT, defaultSdkServiceUrl);
            }
        }

        //There needs a default URL to be used when no format is specified.
        if (!sdkUrls.containsKey(DEFAULT) && !sdkUrls.isEmpty()) {
            //Take any first url and set it to default
            sdkUrls.put(DEFAULT, sdkUrls.values().iterator().next());
        }

        if (sdkUrls.isEmpty()) {
            throw new IllegalStateException("No valid sdk service url configured");
        }
        return sdkUrls;
    }

    /**
     * Retrieves the SDK service URL based on the biometric modality and provided flags.
     * If no specific format URL is found for the modality, the default SDK service URL is returned.
     *
     * @param modality The {@link BiometricType} representing the biometric modality.
     * @param flags    A map of flags where keys may indicate the format for the specified modality.
     * @return The corresponding SDK service URL for the modality or the default URL if none found.
     */
    private String getSdkServiceUrl(BiometricType modality, Map<String, String> flags) {
        if (modality != null) {
            String key = modality.name() + FORMAT_SUFFIX;
            if (flags != null) {
                Optional<String> formatFromFlag = flags.entrySet()
                        .stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(key))
                        .findAny()
                        .map(Entry::getValue);
                if (formatFromFlag.isPresent()) {
                    String format = formatFromFlag.get();
                    Optional<String> urlForFormat = sdkUrlsMap.entrySet()
                            .stream()
                            .filter(e -> e.getKey().equalsIgnoreCase(format))
                            .findAny()
                            .map(Entry::getValue);
                    if (urlForFormat.isPresent()) {
                        return urlForFormat.get();
                    }
                }
            }
        }
        return getDefaultSdkServiceUrl();
    }

    /**
     * Retrieves the default SDK service URL from the current SDK URLs map.
     *
     * @return The default SDK service URL, or null if not present.
     */
    private String getDefaultSdkServiceUrl() {
        return sdkUrlsMap.get(DEFAULT);
    }

    /**
     * Retrieves the default SDK service URL from environment variables.
     *
     * @return The default SDK service URL from environment variables, or null if not defined.
     */
    private String getDefaultSdkServiceUrlFromEnv() {
        if (System.getProperty(MOSIP_BIOSDK_SERVICE) != null)
            return System.getProperty(MOSIP_BIOSDK_SERVICE);

        return System.getenv(MOSIP_BIOSDK_SERVICE);
    }

    /**
     * Checks the quality of a biometric sample for the given modalities.
     *
     * @param sample            The biometric record to check.
     * @param modalitiesToCheck List of biometric modalities to check quality for.
     * @param flags             Additional configuration flags.
     * @return A response containing the quality check result.
     * @throws BioSdkClientException if parsing or processing of response fails.
     */
    @Override
    public Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck, Map<String, String> flags) {
        Response<QualityCheck> response = new Response<>();
        response.setStatusCode(200);
        try {
            CheckQualityRequestDto checkQualityRequestDto = new CheckQualityRequestDto();
            checkQualityRequestDto.setSample(sample);
            checkQualityRequestDto.setModalitiesToCheck(modalitiesToCheck);
            checkQualityRequestDto.setFlags(flags);

            RequestDto requestDto = generateNewRequestDto(checkQualityRequestDto);
            String url = getSdkServiceUrl(modalitiesToCheck.get(0), flags) + "/check-quality";
            ResponseEntity<?> responseEntity = Util.restRequest(url, HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }

            String responseBody = (String) Objects.requireNonNull(responseEntity.getBody(), TAG_RESPONSE_NULL);
            if (responseBody.equalsIgnoreCase(TAG_RESPONSE_NULL)) {
                throw new NullPointerException(TAG_RESPONSE_NULL);
            }

            fillResponse(response, responseBody, QUALITY_READER);
            return response;
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "", e.getLocalizedMessage(),
                    e);
        }
    }

    /**
     * Matches a biometric sample against a gallery of biometric records for specified modalities.
     *
     * @param sample            The biometric sample to match.
     * @param gallery           Array of biometric records to match against.
     * @param modalitiesToMatch List of biometric modalities to consider for matching.
     * @param flags             Additional configuration flags.
     * @return A response containing an array of match decisions.
     * @throws BioSdkClientException if parsing or processing of response fails.
     */
    @Override
    public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery,
                                           List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
        Response<MatchDecision[]> response = new Response<>();
        try {
            MatchRequestDto matchRequestDto = new MatchRequestDto();
            matchRequestDto.setSample(sample);
            matchRequestDto.setGallery(gallery);
            matchRequestDto.setModalitiesToMatch(modalitiesToMatch);
            matchRequestDto.setFlags(flags);

            RequestDto requestDto = generateNewRequestDto(matchRequestDto);
            String url = getSdkServiceUrl(modalitiesToMatch.get(0), flags) + "/match";
            ResponseEntity<?> responseEntity = Util.restRequest(url, HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }

            String responseBody = (String) Objects.requireNonNull(responseEntity.getBody(), TAG_RESPONSE_NULL);
            if (responseBody.equalsIgnoreCase(TAG_RESPONSE_NULL)) {
                throw new NullPointerException(TAG_RESPONSE_NULL);
            }

            fillResponse(response, responseBody, MATCH_DECISIONS_READER);
            return response;
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "", e.getLocalizedMessage(),
                    e);
        }
    }

    /**
     * Extracts a biometric template from the provided biometric record for the given modalities.
     *
     * @param sample              The biometric record from which the template needs to be extracted.
     * @param modalitiesToExtract List of biometric modalities to extract templates for.
     * @param flags               Additional configuration flags that may influence the extraction process.
     * @return A response containing the extracted biometric template.
     * @throws BioSdkClientException if parsing or processing of response fails.
     */
    @Override
    public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract, Map<String, String> flags) {
        Response<BiometricRecord> response = new Response<>();
        try {
            ExtractTemplateRequestDto extractTemplateRequestDto = new ExtractTemplateRequestDto();
            extractTemplateRequestDto.setSample(sample);
            extractTemplateRequestDto.setModalitiesToExtract(modalitiesToExtract);
            extractTemplateRequestDto.setFlags(flags);

            RequestDto requestDto = generateNewRequestDto(extractTemplateRequestDto);
            String url = getSdkServiceUrl(modalitiesToExtract, flags) + "/extract-template";
            ResponseEntity<?> responseEntity = Util.restRequest(url, HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }

            convertAndSetResponseObject(response, responseEntity);
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "", e.getLocalizedMessage(),
                    e);
        }
        return response;
    }

    /**
     * Retrieves the appropriate SDK service URL based on the modalities to extract and additional flags.
     * Falls back to a default URL if no specific match is found.
     *
     * @param modalitiesToExtract List of biometric modalities for which template extraction is requested.
     * @param flags               A map of additional parameters to help determine the correct service URL.
     * @return The resolved SDK service URL to use for template extraction.
     */
    private String getSdkServiceUrl(List<BiometricType> modalitiesToExtract, Map<String, String> flags) {
        if (modalitiesToExtract != null && !modalitiesToExtract.isEmpty()) {
            return getSdkServiceUrl(modalitiesToExtract.get(0), flags);
        } else {
            Set<String> keySet = flags.keySet();
            for (String key : keySet) {
                if (key.toLowerCase().contains(BiometricType.FINGER.name().toLowerCase())) {
                    return getSdkServiceUrl(BiometricType.FINGER, flags);
                } else if (key.toLowerCase().contains(BiometricType.IRIS.name().toLowerCase())) {
                    return getSdkServiceUrl(BiometricType.IRIS, flags);
                } else if (key.toLowerCase().contains(BiometricType.FACE.name().toLowerCase())) {
                    return getSdkServiceUrl(BiometricType.FACE, flags);
                }
            }
        }
        return getDefaultSdkServiceUrl();
    }

    /**
     * Segments the given biometric record based on the specified modalities and configuration flags.
     *
     * @param biometricRecord     The biometric record to be segmented.
     * @param modalitiesToSegment List of biometric modalities to perform segmentation on.
     * @param flags               Additional configuration flags that may influence the segmentation process.
     * @return A response containing the segmented biometric record.
     * @throws BioSdkClientException if parsing or processing of the response fails.
     */
    @Override
    public Response<BiometricRecord> segment(BiometricRecord biometricRecord, List<BiometricType> modalitiesToSegment, Map<String, String> flags) {
        Response<BiometricRecord> response = new Response<>();
        try {
            SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
            segmentRequestDto.setSample(biometricRecord);
            segmentRequestDto.setModalitiesToSegment(modalitiesToSegment);
            segmentRequestDto.setFlags(flags);

            RequestDto requestDto = generateNewRequestDto(segmentRequestDto);
            String url = getSdkServiceUrl(modalitiesToSegment.get(0), flags) + "/segment";
            ResponseEntity<?> responseEntity = Util.restRequest(url, HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }
            convertAndSetResponseObject(response, responseEntity);
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "", e.getLocalizedMessage(),
                    e);
        }
        return response;
    }

    /**
     * Converts the HTTP response entity into a {@link Response} object containing a {@link BiometricRecord}.
     * Extracts status codes, messages, and parsed biometric record information from the response.
     *
     * @param response       The response object to populate with parsed data.
     * @param responseEntity The raw HTTP response entity received from the service call.
     * @throws ParseException        If there is an error while parsing the JSON response body.
     * @throws BioSdkClientException If the JSON mapping fails during response conversion.
     */
    private void convertAndSetResponseObject(Response<BiometricRecord> response, ResponseEntity<?> responseEntity) throws Exception {
        String responseBody = (String) Objects.requireNonNull(responseEntity.getBody(), TAG_RESPONSE_NULL);
        if (responseBody.equalsIgnoreCase(TAG_RESPONSE_NULL)) {
            throw new NullPointerException(TAG_RESPONSE_NULL);
        }

        fillResponse(response, responseBody, BIOREC_READER);
    }

    /**
     * Converts biometric data format from sourceFormat to targetFormat using provided parameters.
     * This method is deprecated and replaced by {@link #convertFormatV2}.
     *
     * @param sample              The biometric record to be converted.
     * @param sourceFormat        Source format of the biometric data.
     * @param targetFormat        Target format of the biometric data.
     * @param sourceParams        Additional source format parameters.
     * @param targetParams        Additional target format parameters.
     * @param modalitiesToConvert List of biometric modalities to be converted.
     * @return Converted {@link BiometricRecord}.
     * @throws BioSdkClientException if parsing or processing of the response fails.
     */
    @Override
    @Deprecated
    public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
                                         Map<String, String> sourceParams, Map<String, String> targetParams, List<BiometricType> modalitiesToConvert) {
        BiometricRecord resBiometricRecord = null;
        try {
            ConvertFormatRequestDto convertFormatRequestDto = new ConvertFormatRequestDto();
            convertFormatRequestDto.setSample(sample);
            convertFormatRequestDto.setSourceFormat(sourceFormat);
            convertFormatRequestDto.setTargetFormat(targetFormat);
            convertFormatRequestDto.setSourceParams(sourceParams);
            convertFormatRequestDto.setTargetParams(targetParams);
            convertFormatRequestDto.setModalitiesToConvert(modalitiesToConvert);

            RequestDto requestDto = generateNewRequestDto(convertFormatRequestDto);
            String url = getDefaultSdkServiceUrl() + "/convert-format";
            ResponseEntity<?> responseEntity = Util.restRequest(url, HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }

            String responseBody = (String) Objects.requireNonNull(responseEntity.getBody(), TAG_RESPONSE_NULL);
            if (responseBody.equalsIgnoreCase(TAG_RESPONSE_NULL)) {
                throw new NullPointerException(TAG_RESPONSE_NULL);
            }

            JSONParser parser = new JSONParser();
            JSONObject js = (JSONObject) parser.parse(responseBody);

            /* Error handler */
            errorHandler(js.get("errors") != null ? Util.getObjectMapper().readValue(js.get("errors").toString(), errorDtoListTypeRef) : null);

            resBiometricRecord = Util.getObjectMapper().readValue(js.get("response").toString(), new TypeReference<BiometricRecord>() {
            });
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "", e.getLocalizedMessage(),
                    e);
        }
        return resBiometricRecord;
    }

    /**
     * Converts biometric data format from sourceFormat to targetFormat using provided parameters.
     * Returns a Response object containing status, message, and converted record.
     *
     * @param sample              The biometric record to be converted.
     * @param sourceFormat        Source format of the biometric data.
     * @param targetFormat        Target format of the biometric data.
     * @param sourceParams        Additional source format parameters.
     * @param targetParams        Additional target format parameters.
     * @param modalitiesToConvert List of biometric modalities to be converted.
     * @return Response containing converted {@link BiometricRecord} and status information.
     * @throws BioSdkClientException if parsing or processing of the response fails.
     */
    @Override
    public Response<BiometricRecord> convertFormatV2(BiometricRecord sample, String sourceFormat, String targetFormat,
                                                     Map<String, String> sourceParams, Map<String, String> targetParams,
                                                     List<BiometricType> modalitiesToConvert) {
        Response<BiometricRecord> response = new Response<>();
        try {
            ConvertFormatRequestDto convertFormatRequestDto = new ConvertFormatRequestDto();
            convertFormatRequestDto.setSample(sample);
            convertFormatRequestDto.setSourceFormat(sourceFormat);
            convertFormatRequestDto.setTargetFormat(targetFormat);
            convertFormatRequestDto.setSourceParams(sourceParams);
            convertFormatRequestDto.setTargetParams(targetParams);
            convertFormatRequestDto.setModalitiesToConvert(modalitiesToConvert);

            RequestDto requestDto = generateNewRequestDto(convertFormatRequestDto);
            String url = getDefaultSdkServiceUrl() + "/convert-format";
            ResponseEntity<?> responseEntity = Util.restRequest(url, HttpMethod.POST, MediaType.APPLICATION_JSON, requestDto, null, String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, "HTTP status: ", responseEntity.getStatusCode().toString());
                throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "",
                        TAG_HTTP_STATUS + responseEntity.getStatusCode().toString());
            }
            Object body = responseEntity.getBody();
            if (body == null) {
                throw new NullPointerException("Response body is null");
            }
            String responseBody = body.toString();
            fillResponse(response, responseBody, BIOREC_READER);
        } catch (Exception e) {
            logger.error(LOGGER_SESSIONID, LOGGER_IDTYPE, "error", e);
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "", e.getLocalizedMessage(),
                    e);
        }
        return response;
    }

    /**
     * Generates a new {@link RequestDto} object by encoding the provided body as a Base64 string.
     * This ensures that the request payload is safely encoded before sending to the SDK service.
     *
     * @param body The request body object to encode.
     * @return A new {@link RequestDto} containing the encoded request.
     * @throws JsonProcessingException If the object cannot be converted to a JSON string.
     */
    private RequestDto generateNewRequestDto(Object body) throws JsonProcessingException {
        RequestDto requestDto = new RequestDto();
        requestDto.setVersion(VERSION);
        requestDto.setRequest(Util.base64Encode(Util.getObjectMapper().writeValueAsString(body)));
        return requestDto;
    }

    /**
     * Handles and processes a list of {@link ErrorDto} objects.
     * If any errors are present, this method throws a {@link RuntimeException}
     * containing the first error code and message for easier debugging and tracking.
     *
     * @param errors The list of {@link ErrorDto} objects returned from the SDK service.
     * @throws RuntimeException If one or more errors are present in the list.
     */
    private void errorHandler(List<ErrorDto> errors) {
        if (errors == null) {
            return;
        }

        StringBuilder errorMessages = new StringBuilder();
        for (ErrorDto errorDto : errors) {
            if (errorDto != null) {
                errorMessages.append("Code: ").append(errorDto.getCode()).append(", Message: ")
                        .append(errorDto.getMessage()).append(System.lineSeparator());
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new BioSdkClientException(ResponseStatus.UNKNOWN_ERROR.getStatusCode() + "",
                    errorMessages.toString());
        }
    }

    /**
     * Populates a {@link Response} object from a raw JSON response string.
     * <p>
     * This method performs the following steps:
     * <ol>
     *     <li>Parses the raw {@code responseBody} into a Jackson {@link JsonNode} tree.</li>
     *     <li>Invokes {@link #handleErrors(JsonNode)} to process any errors present in the {@code errors} node.</li>
     *     <li>Extracts the {@code statusCode} and {@code statusMessage} values from either the nested
     *         {@code response} object or the root node, supporting both endpoint response formats:
     *         <ul>
     *             <li>{@code { response: { statusCode, statusMessage, response: {...} } }}</li>
     *             <li>{@code { statusCode, statusMessage, response: {...} }}</li>
     *         </ul>
     *     </li>
     *     <li>Locates the {@code response} payload node, either nested inside {@code response.response}
     *         or directly under the root {@code response} field.</li>
     *     <li>Uses the provided {@link com.fasterxml.jackson.databind.ObjectReader} to deserialize
     *         the payload into the target type {@code <T>} and assigns it to the {@link Response#setResponse(Object)}.</li>
     * </ol>
     *
     * @param <T>          the type of the response payload.
     * @param out          the {@link Response} instance to populate.
     * @param responseBody the raw JSON string returned by the SDK service endpoint.
     * @param reader       a pre-configured Jackson {@link com.fasterxml.jackson.databind.ObjectReader}
     *                     for deserializing the payload into the desired type {@code <T>}.
     * @throws Exception if the JSON parsing or payload mapping fails, or if {@link #handleErrors(JsonNode)} throws due to service errors.
     * @see #handleErrors(JsonNode)
     */
    private <T> void fillResponse(Response<T> out, String responseBody, ObjectReader reader)
            throws Exception {
        JsonNode root = M.readTree(responseBody);
        handleErrors(root.path(TAG_ERRORS));

        JsonNode jsonResponse = root.path(TAG_RESPONSE);
        // If some endpoints return nested { response: { statusCode, statusMessage, response } }, support both:
        Integer statusCode = jsonResponse.hasNonNull(TAG_STATUS_CODE)
                ? Integer.valueOf(jsonResponse.get(TAG_STATUS_CODE).asInt())
                : root.path(TAG_STATUS_CODE).isNumber() ? root.get(TAG_STATUS_CODE).asInt() : null;

        String statusMessage = jsonResponse.hasNonNull(TAG_STATUS_MESSAGE)
                ? jsonResponse.get(TAG_STATUS_MESSAGE).asText("")
                : root.path(TAG_STATUS_MESSAGE).asText("");

        out.setStatusCode(statusCode);
        out.setStatusMessage(statusMessage);

        JsonNode payload =
                jsonResponse.has("response") ? jsonResponse.get("response") : root.get("response");

        out.setResponse((payload == null || payload.isNull()) ? null : reader.readValue(payload));
    }

    /**
     * Processes an {@code errors} JSON array node returned by the SDK service.
     * <p>
     * This method validates that the {@code errorsNode} is a non-null, non-empty JSON array.
     * If valid, it deserializes the array into a {@link List} of {@link ErrorDto} objects
     * using a pre-configured Jackson {@link com.fasterxml.jackson.databind.ObjectReader}.
     * The resulting list is then passed to the existing {@link #errorHandler(List)} method,
     * which aggregates the error codes and messages and throws a {@link BioSdkClientException}
     * if one or more errors are present.
     *
     * @param errorsNode the {@link JsonNode} representing the {@code errors} array in the SDK service response.
     * @throws IOException           if deserialization of the {@code errorsNode} into {@link ErrorDto} objects fails.
     * @throws BioSdkClientException if {@link #errorHandler(List)} detects any service errors.
     */
    private void handleErrors(com.fasterxml.jackson.databind.JsonNode errorsNode) throws IOException {
        if (errorsNode == null || errorsNode.isNull() || errorsNode.isMissingNode() || !errorsNode.isArray() || errorsNode.isEmpty()) {
            return;
        }
        List<ErrorDto> errors = ERR_LIST_READER.readValue(errorsNode);
        errorHandler(errors); // your existing method aggregates and throws BioSdkClientException
    }
}