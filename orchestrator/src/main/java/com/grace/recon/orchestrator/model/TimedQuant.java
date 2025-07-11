package com.grace.recon.orchestrator.model;

import com.grace.recon.common.dto.Quant;
import lombok.Data;

public class TimedQuant {
    private final Quant quant;
    private final long entryTime;

    public TimedQuant(Quant quant) {
        this.quant = quant;
        this.entryTime = System.currentTimeMillis();
    }

    public Quant getQuant() {
        return quant;
    }

    public long getEntryTime() {
        return entryTime;
    }
}