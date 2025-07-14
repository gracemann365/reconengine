package com.grace.recon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quant {
  private String transactionId;
  private double amount;
  private String currency;
  private long transactionDate; // Changed to long for timestamp-millis
  private String description;
  private String sourceSystem;
  private String transactionType;
  private String authorizationCode;
  private String sourceReferenceId;
  private String accountId;
  private java.util.Map<String, String>
      additionalMetadata; // Changed to Map<String, String> for map type
}
