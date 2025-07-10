package com.grace.recon.orchestrator.model;

import com.grace.recon.common.dto.Quant;

public class QuantPair {
    private Quant quant1;
    private Quant quant2;

    public QuantPair(Quant quant1, Quant quant2) {
        this.quant1 = quant1;
        this.quant2 = quant2;
    }

    public Quant getQuant1() {
        return quant1;
    }

    public Quant getQuant2() {
        return quant2;
    }

    public String getTransactionId() {
        return quant1.getTransactionId();
    }

    @Override
    public String toString() {
        return "QuantPair{" +
               "quant1=" + (quant1 != null ? quant1.getTransactionId() : "null") +
               ", quant2=" + (quant2 != null ? quant2.getTransactionId() : "null") +
               '}';
    }
}