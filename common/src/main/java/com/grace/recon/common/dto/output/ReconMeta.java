package com.grace.recon.common.dto.output;

/**
 * Aggregates metadata about a reconciliation batch for reporting. Sent to the Recon_Metadata_Topic.
 */
public class ReconMeta {

  private String batchId;
  private long processingStartTime;
  private long processingEndTime;
  private long processingDurationMs;
  private int totalPairsProcessed;
  private int exactMatchCount;
  private int fuzzyMatchCount;
  private int ureCount;

  // No-arg constructor for Jackson deserialization
  public ReconMeta() {}

  // All-args constructor for programmatic creation
  public ReconMeta(
      String batchId,
      long processingStartTime,
      long processingEndTime,
      long processingDurationMs,
      int totalPairsProcessed,
      int exactMatchCount,
      int fuzzyMatchCount,
      int ureCount) {
    this.batchId = batchId;
    this.processingStartTime = processingStartTime;
    this.processingEndTime = processingEndTime;
    this.processingDurationMs = processingDurationMs;
    this.totalPairsProcessed = totalPairsProcessed;
    this.exactMatchCount = exactMatchCount;
    this.fuzzyMatchCount = fuzzyMatchCount;
    this.ureCount = ureCount;
  }

  // Standard Getters and Setters...
  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public long getProcessingStartTime() {
    return processingStartTime;
  }

  public void setProcessingStartTime(long processingStartTime) {
    this.processingStartTime = processingStartTime;
  }

  public long getProcessingEndTime() {
    return processingEndTime;
  }

  public void setProcessingEndTime(long processingEndTime) {
    this.processingEndTime = processingEndTime;
  }

  public long getProcessingDurationMs() {
    return processingDurationMs;
  }

  public void setProcessingDurationMs(long processingDurationMs) {
    this.processingDurationMs = processingDurationMs;
  }

  public int getTotalPairsProcessed() {
    return totalPairsProcessed;
  }

  public void setTotalPairsProcessed(int totalPairsProcessed) {
    this.totalPairsProcessed = totalPairsProcessed;
  }

  public int getExactMatchCount() {
    return exactMatchCount;
  }

  public void setExactMatchCount(int exactMatchCount) {
    this.exactMatchCount = exactMatchCount;
  }

  public int getFuzzyMatchCount() {
    return fuzzyMatchCount;
  }

  public void setFuzzyMatchCount(int fuzzyMatchCount) {
    this.fuzzyMatchCount = fuzzyMatchCount;
  }

  public int getUreCount() {
    return ureCount;
  }

  public void setUreCount(int ureCount) {
    this.ureCount = ureCount;
  }
}
