package io.harness.demo.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Input accepted by the greeting endpoint. */
public record GreetingRequest(
    @NotBlank(message = "name must not be blank")
        @Size(max = 60, message = "name must be at most 60 characters")
        String name) {}
