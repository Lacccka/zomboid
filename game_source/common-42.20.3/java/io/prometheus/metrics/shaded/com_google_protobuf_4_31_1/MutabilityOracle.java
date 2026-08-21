/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

interface MutabilityOracle {
    public static final MutabilityOracle IMMUTABLE = new MutabilityOracle(){

        @Override
        public void ensureMutable() {
            throw new UnsupportedOperationException();
        }
    };

    public void ensureMutable();
}

