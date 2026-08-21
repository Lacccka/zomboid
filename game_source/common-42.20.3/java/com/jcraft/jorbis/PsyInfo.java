/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

class PsyInfo {
    float athAtt;
    int athp;
    float attackCoeff;
    float decayCoeff;
    int decayp;
    float maxCurveDb;
    float[] noiseatt1000Hz = new float[5];
    float[] noiseatt125Hz = new float[5];
    float[] noiseatt2000Hz = new float[5];
    float[] noiseatt250Hz = new float[5];
    float[] noiseatt4000Hz = new float[5];
    float[] noiseatt500Hz = new float[5];
    float[] noiseatt8000Hz = new float[5];
    int noisefitSubblock;
    float noisefitThreshDb;
    int noisefitp;
    int noisemaskp;
    float[] peakatt1000Hz = new float[5];
    float[] peakatt125Hz = new float[5];
    float[] peakatt2000Hz = new float[5];
    float[] peakatt250Hz = new float[5];
    float[] peakatt4000Hz = new float[5];
    float[] peakatt500Hz = new float[5];
    float[] peakatt8000Hz = new float[5];
    int peakattp;
    int smoothp;
    float[] toneatt1000Hz = new float[5];
    float[] toneatt125Hz = new float[5];
    float[] toneatt2000Hz = new float[5];
    float[] toneatt250Hz = new float[5];
    float[] toneatt4000Hz = new float[5];
    float[] toneatt500Hz = new float[5];
    float[] toneatt8000Hz = new float[5];
    int tonemaskp;

    PsyInfo() {
    }

    void free() {
    }
}

