package lcc.internetradio.server;

/** Supplies signed little-endian 16-bit mono PCM frames. */
interface PcmSource {
    void fill(byte[] frame, int sampleRate);
    String description();
}
