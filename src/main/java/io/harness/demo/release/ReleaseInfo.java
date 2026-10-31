package io.harness.demo.release;

import java.time.Instant;

/** Public release metadata used to identify which deployment handled a request. */
public record ReleaseInfo(
    String application,
    String version,
    String color,
    String environment,
    String hostname,
    Instant timestamp) {}
