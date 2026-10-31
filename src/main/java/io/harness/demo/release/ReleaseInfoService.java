package io.harness.demo.release;

import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Supplies immutable metadata for the currently running release. */
@Service
public class ReleaseInfoService {

  private final String applicationName;
  private final String version;
  private final String color;
  private final String environment;
  private final String hostname;
  private final Clock clock;

  /** Creates the service from externally supplied deployment metadata. */
  @Autowired
  public ReleaseInfoService(
      @Value("${spring.application.name}") String applicationName,
      @Value("${app.version:dev}") String version,
      @Value("${app.release-color:local}") String color,
      @Value("${app.environment:local}") String environment,
      @Value("${HOSTNAME:unknown}") String hostname) {
    this(applicationName, version, color, environment, hostname, Clock.systemUTC());
  }

  ReleaseInfoService(
      String applicationName,
      String version,
      String color,
      String environment,
      String hostname,
      Clock clock) {
    this.applicationName = applicationName;
    this.version = version;
    this.color = color;
    this.environment = environment;
    this.hostname = hostname;
    this.clock = clock;
  }

  /** Returns metadata for use in responses and deployment verification. */
  public ReleaseInfo current() {
    return new ReleaseInfo(
        applicationName, version, color, environment, hostname, Instant.now(clock));
  }
}
