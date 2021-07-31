package device.google.atv.audio_proxy;

import device.google.atv.audio_proxy.RenderingLatency;

/**
 * Status for one audio data write. It will be returned by the status FMQ as a
 * response to the data FMQ write.
 * written is the number of bytes been written into the output stream.
 * latency is the latency info measured by the output stream.
 */
@VintfStability
@FixedSize
parcelable WriteStatus {
    long written;
    RenderingLatency latency;
}