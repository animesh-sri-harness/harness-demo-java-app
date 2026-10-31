package io.harness.demo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.harness.demo.release.ReleaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DemoController.class)
@Import(ReleaseInfoService.class)
@TestPropertySource(
    properties = {
      "spring.application.name=demo",
      "app.version=1.2.3",
      "app.release-color=green",
      "app.environment=test",
      "HOSTNAME=test-pod"
    })
class DemoControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void returnsReleaseInfo() throws Exception {
    mockMvc
        .perform(get("/api/v1/info"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value("1.2.3"))
        .andExpect(jsonPath("$.color").value("green"))
        .andExpect(jsonPath("$.hostname").value("test-pod"));
  }

  @Test
  void returnsDefaultGreeting() throws Exception {
    mockMvc
        .perform(get("/api/v1/greetings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Hello, Harness!"))
        .andExpect(jsonPath("$.version").value("1.2.3"));
  }

  @Test
  void createsValidatedGreeting() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/greetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Developer\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Hello, Developer!"))
        .andExpect(jsonPath("$.color").value("green"));
  }

  @Test
  void rejectsBlankGreetingName() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/greetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
