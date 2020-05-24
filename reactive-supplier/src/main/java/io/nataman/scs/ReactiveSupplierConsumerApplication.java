package io.nataman.scs;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Log4j2
public class ReactiveSupplierConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveSupplierConsumerApplication.class, args);
  }

  @Bean
  public Consumer<PageViewEvent> sinkConsumer() {
    return pageViewEvent -> log.info("Received event: {}", pageViewEvent);
  }

  @Bean
  public Supplier<Flux<PageViewEvent>> eventSupplier() {
    return () ->
        Flux.interval(Duration.ofSeconds(1))
            .map(
                aLong ->
                    PageViewEvent.builder()
                        .userid("source")
                        .page("page")
                        .duration(Math.toIntExact(aLong))
                        .build())
            .log("eventSupplier");
  }

  @Bean
  public Consumer<Flux<PageViewEvent>> eventSink(Consumer<PageViewEvent> consumer) {
    return pageViewEventFlux -> pageViewEventFlux.log("eventSink").subscribe(consumer);
  }
}
