package de.numcodex.feasibility_gui_backend.query.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public record TimeRestriction(
    @JsonProperty("beforeDate") String beforeDate,
    @JsonProperty("afterDate") String afterDate
) {

}
