package lcc.internetradio.server;

/** Deterministic transport test source; replaceable by the future HLS source. */
final class TonePcmSource implements PcmSource {
    private final double frequencyHz;
    private final double amplitude;
    private double phase;

    TonePcmSource(double frequencyHz, double amplitude) {
        this.frequencyHz = frequencyHz;
        this.amplitude = amplitude;
    }

    @Override
    public synchronized void fill(byte[] frame, int sampleRate) {
        double phaseStep = 2.0 * Math.PI * frequencyHz / sampleRate;
        int samples = frame.length / 2;
        for (int index = 0; index < samples; index++) {
            short sample = (short) Math.round(
                    Math.sin(phase) * Short.MAX_VALUE * amplitude);
            int byteIndex = index * 2;
            frame[byteIndex] = (byte) (sample & 0xff);
            frame[byteIndex + 1] = (byte) ((sample >>> 8) & 0xff);
            phase += phaseStep;
            if (phase >= 2.0 * Math.PI) phase -= 2.0 * Math.PI;
        }
    }

    @Override
    public String description() {
        return "tone:" + frequencyHz + "Hz";
    }
}
