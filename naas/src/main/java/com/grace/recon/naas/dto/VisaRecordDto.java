package com.grace.recon.naas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

/**
 * Represents a single record parsed from the VISA CSV file. The fields are based on the schema
 * defined in etl.json.
 */
@Data
public class VisaRecordDto {

  private String transactionType;
  private String transactionId;
  private String cardNumber;
  private BigDecimal amount;
  private String Stan;
  private String currencyCode;
  private LocalDate transactionDate;
  private LocalTime transactionTime;
  private String authorizationCode;
  private String merchantId;
  private String rrn;
  private BigDecimal settlementAmount;
}
