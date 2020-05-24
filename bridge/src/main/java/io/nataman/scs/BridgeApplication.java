package io.nataman.scs;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@SpringBootApplication
@Log4j2
@Controller
@RequiredArgsConstructor
public class BridgeApplication {

  private final StreamBridge streamBridge;

  public static void main(String[] args) {
    SpringApplication.run(BridgeApplication.class, args);
  }

  @PostMapping(consumes = {MimeTypeUtils.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void bridge(@RequestBody PageViewEvent pageViewEvent) {
    log.info("Received request: {}", pageViewEvent);
    streamBridge.send("bridge-out-0", pageViewEvent);
  }

  @Bean
  public Consumer<PageViewEvent> bridge() {
    return pageViewEvent -> log.info("Received event: {}", pageViewEvent);
  }
}
