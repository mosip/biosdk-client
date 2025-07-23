package io.mosip.biosdk.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MatchDecisionMixin {
    @JsonCreator
    public MatchDecisionMixin(@JsonProperty("galleryIndex") int galleryIndex) {}
}
