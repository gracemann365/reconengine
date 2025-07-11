package com.grace.recon.matcher.service;

import com.grace.recon.common.dto.QuantPair;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QuantBufferService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QuantBufferService.class);
  // Task requires capacity for 50,000 quants; since they come in pairs, capacity is 25,000.
  private static final int BUFFER_CAPACITY = 25000;

  private final BlockingQueue<QuantPair> buffer = new LinkedBlockingQueue<>(BUFFER_CAPACITY);

  public QuantBufferService(MeterRegistry meterRegistry) {
    Gauge.builder("matcher.engine.buffer.size", buffer::size)
        .description("The current number of QuantPairs in the input buffer.")
        .register(meterRegistry);
  }

  /**
   * Adds a QuantPair to the buffer, blocking if the buffer is full. This inherently applies
   * backpressure to the Kafka consumer.
   *
   * @param quantPair the QuantPair to add
   */
  public void addToBuffer(QuantPair quantPair) {
    try {
      // put() will wait if the queue is full, pausing the Kafka listener thread.
      buffer.put(quantPair);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Buffer addition was interrupted", e);
    }
  }

  /**
   * Retrieves and removes the head of the buffer, waiting if necessary.
   *
   * @return the head of the buffer
   */
  public QuantPair takeFromBuffer() throws InterruptedException {
    return buffer.take();
  }

  public int getBufferSize() {
    return buffer.size();
  }
}
