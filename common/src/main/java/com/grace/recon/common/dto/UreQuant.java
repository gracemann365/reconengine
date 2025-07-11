package com.grace.recon.common.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UreQuant {
  private QuantPair quantPair;
  private String matchStatus; // e.g., "UNRECONCILED"
  private String reasonCode; // e.g., "NO_EXACT_MATCH", "FUZZY_TOLERANCE_EXCEEDED"
  private Map<String, String> errorDetails; // Detailed metadata explaining the failure
}
