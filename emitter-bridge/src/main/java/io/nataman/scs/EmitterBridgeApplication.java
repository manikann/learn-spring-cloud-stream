package io.nataman.scs;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Log4j2
@Controller
@RequiredArgsConstructor
public class EmitterBridgeApplication {

  EmitterProcessor<PageViewEvent> emitterProcessor = EmitterProcessor.create();

  public static void main(String[] args) {
    SpringApplication.run(EmitterBridgeApplication.class, args);
  }

  @PostMapping(consumes = {MimeTypeUtils.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void bridge(@RequestBody PageViewEvent pageViewEvent) {
    log.info("Received request: {}", pageViewEvent);
    emitterProcessor.onNext(pageViewEvent);
  }

  @Bean
  public Supplier<Flux<PageViewEvent>> fluxSupplier() {
    return () -> emitterProcessor.log("fluxSupplier");
  }
}
