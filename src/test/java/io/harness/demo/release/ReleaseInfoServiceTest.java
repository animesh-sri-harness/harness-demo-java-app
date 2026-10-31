package io.harness.demo.release;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ReleaseInfoServiceTest {

  @Test
  void returnsConfiguredReleaseMetadata() {
    Instant now = Instant.parse("2026-01-15T10:00:00Z");
    ReleaseInfoService service =
        new ReleaseInfoService(
            "demo", "2.1.0", "green", "production", "pod-123", Clock.fixed(now, ZoneOffset.UTC));

    ReleaseInfo result = service.current();

    assertThat(result)
        .isEqualTo(new ReleaseInfo("demo", "2.1.0", "green", "production", "pod-123", now));
  }
}
