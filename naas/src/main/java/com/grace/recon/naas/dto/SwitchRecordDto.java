package com.grace.recon.naas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Represents a single record parsed from the Switch SQL Dump. The fields are based on the schema
 * defined in etl.json.
 */
@Data
public class SwitchRecordDto {

  private String transaction_id;
  private String merchant_name;
  private String merchant_id;
  private String stan;
  private String card_number;
  private String transaction_type;
  private String authorization_code;
  private String rrn;
  private String terminal_id;
  private BigDecimal settlement_amount;
  private LocalDateTime created_at;
}
