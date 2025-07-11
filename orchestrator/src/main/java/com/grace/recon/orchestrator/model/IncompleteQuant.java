package com.grace.recon.orchestrator.model;

import com.grace.recon.common.dto.Quant;

public class IncompleteQuant {
  private Quant quant;
  private long timestamp;
  private String reason;

  public IncompleteQuant(Quant quant, long timestamp, String reason) {
    this.quant = quant;
    this.timestamp = timestamp;
    this.reason = reason;
  }

  public Quant getQuant() {
    return quant;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString() {
    return "IncompleteQuant{"
        + "quant="
        + (quant != null ? quant.getTransactionId() : "null")
        + ", timestamp="
        + timestamp
        + ", reason='"
        + reason
        + "'"
        + '}';
  }
}
