package com.grace.recon.naas.queue;

import com.grace.recon.common.monitoring.MetricService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class NaasDataQueue {

    public final Queue<Object> validationToNormalizationQueue = new ConcurrentLinkedQueue<>();

    private final MetricService metricService;

    public NaasDataQueue(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostConstruct
    public void registerMetrics() {
        metricService.registerGauge(
            "naas.queue.depth",
            this,
            queue -> (double) queue.validationToNormalizationQueue.size(),
            "queueName", "validationToNormalization"
        );
    }
}