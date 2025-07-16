package io.mosip.biosdk.client.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.model.SDKInfo;

import java.io.IOException;
import java.util.*;

public class SDKInfoDeserializer extends JsonDeserializer<SDKInfo> {

    @Override
    public SDKInfo deserialize(JsonParser jp,
                               DeserializationContext ctxt) throws IOException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root     = mapper.readTree(jp);

        /* ----------- required fields ----------- */
        String apiVersion = root.path("apiVersion").asText(null);
        String sdkVersion = root.path("sdkVersion").asText(null);

        // productOwner is an object: { "organization": "...", "type": "..." }
        JsonNode ownerNode = root.path("productOwner");
        String organization = ownerNode.path("organization").asText(null);
        String type         = ownerNode.path("type").asText(null);

        // Use the only available constructor
        SDKInfo sdkInfo = new SDKInfo(apiVersion, sdkVersion, organization, type);

        /* ----------- supportedModalities ----------- */
        List<BiometricType> modalities = new ArrayList<>();
        for (JsonNode n : root.withArray("supportedModalities")) {
            modalities.add(BiometricType.valueOf(n.asText()));
        }
        sdkInfo.setSupportedModalities(modalities);

        /* ----------- supportedMethods ----------- */
        Map<BiometricFunction, List<BiometricType>> methods = new HashMap<>();

        JsonNode methodsNode = root.path("supportedMethods");
        if (methodsNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = methodsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                BiometricFunction function = BiometricFunction.valueOf(e.getKey());
                List<BiometricType> types   = new ArrayList<>();
                for (JsonNode t : e.getValue()) {
                    types.add(BiometricType.valueOf(t.asText()));
                }
                methods.put(function, types);
            }
        }
        sdkInfo.setSupportedMethods(methods);

        /* ----------- otherInfo (key/value map) ----------- */
        Map<String, String> other = new HashMap<>();
        JsonNode otherInfoNode = root.path("otherInfo");
        if (otherInfoNode.isObject()) {
            otherInfoNode.fields().forEachRemaining(entry ->
                    other.put(entry.getKey(), entry.getValue().asText())
            );
        }
        sdkInfo.setOtherInfo(other);

        return sdkInfo;
    }
}

