package com.grace.recon.common.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grace.recon.common.dto.QuantPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UreQuant {
    private QuantPair quantPair;
    private String reasonCode;
    private String errorDetails;
    private long timestamp;
}