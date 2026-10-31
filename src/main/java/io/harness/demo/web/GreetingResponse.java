package io.harness.demo.web;

/** Greeting response carrying release markers for progressive-delivery tests. */
public record GreetingResponse(String message, String version, String color, String hostname) {}
