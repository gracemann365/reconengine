package com.grace.recon.matcher.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingRule {
  private String field;
  private String type;
  private String value;
  private String operator;
  private double tolerance;
}
