package device.google.atv.audio_proxy;

import device.google.atv.audio_proxy.TimeSpec;

/**
 * Info on pipeline latency:
 * frames is the amount of data in pipeline not rendered yet.
 * timestamp is the time of system clock (must be CLOCK_MONOTONIC_RAW) at which
 * latency measurement was taken.
 */
@VintfStability
@FixedSize
parcelable RenderingLatency {
    long frames;
    TimeSpec timestamp;
}