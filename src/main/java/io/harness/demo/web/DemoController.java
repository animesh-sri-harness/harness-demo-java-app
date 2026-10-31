package io.harness.demo.web;

import io.harness.demo.release.ReleaseInfo;
import io.harness.demo.release.ReleaseInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HTTP API used by smoke tests and deployment verification. */
@Validated
@RestController
@RequestMapping("/api/v1")
public class DemoController {

  private final ReleaseInfoService releaseInfoService;

  public DemoController(ReleaseInfoService releaseInfoService) {
    this.releaseInfoService = releaseInfoService;
  }

  /** Returns release metadata for blue-green and canary traffic checks. */
  @GetMapping("/info")
  public ReleaseInfo info() {
    return releaseInfoService.current();
  }

  /** Returns a greeting through an idempotent endpoint. */
  @GetMapping("/greetings")
  public GreetingResponse greet(
      @RequestParam(defaultValue = "Harness")
          @Size(max = 60, message = "name must be at most 60 characters")
          String name) {
    return responseFor(name);
  }

  /** Returns a greeting through a validated write endpoint. */
  @PostMapping("/greetings")
  @ResponseStatus(HttpStatus.CREATED)
  public GreetingResponse createGreeting(@Valid @RequestBody GreetingRequest request) {
    return responseFor(request.name());
  }

  private GreetingResponse responseFor(String name) {
    ReleaseInfo release = releaseInfoService.current();
    return new GreetingResponse(
        "Hello, " + name + "!", release.version(), release.color(), release.hostname());
  }
}
