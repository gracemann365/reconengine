package com.grace.recon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchedQuant {
  private QuantPair quantPair;
  private String matchStatus; // e.g., "EXACT_MATCH"
}
