package io.nataman.scs;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Log4j2
public class SupplierConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SupplierConsumerApplication.class, args);
  }

  @Bean
  public Supplier<PageViewEvent> eventSupplier() {
    return () -> {
      var e = PageViewEvent.builder().userid("source").page("page").duration(10).build();
      log.info("Generated event: {}", e);
      return e;
    };
  }

  @Bean
  public Consumer<PageViewEvent> eventSink() {
    return pageViewEvent -> log.info("Received event: {}", pageViewEvent);
  }
}
