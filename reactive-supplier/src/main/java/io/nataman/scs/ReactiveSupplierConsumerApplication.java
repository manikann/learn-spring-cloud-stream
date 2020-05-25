package io.nataman.scs;

import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY;
import static org.springframework.kafka.support.converter.KafkaMessageHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Log4j2
public class ReactiveSupplierConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveSupplierConsumerApplication.class, args);
  }

  static Message<PageViewEvent> newMessage(long l) {
    var payload =
        PageViewEvent.builder().userid("source").page("page").duration(Math.toIntExact(l)).build();
    return MessageBuilder.withPayload(payload)
        .setHeader(MESSAGE_KEY, "message-key")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON)
        .build();
  }

  @Bean
  public Consumer<Message<PageViewEvent>> sinkConsumer() {
    return m -> log.info("Received message: {}", m);
  }

  @Bean
  public Supplier<Flux<Message<PageViewEvent>>> eventSupplier() {
    return () ->
        Flux.interval(Duration.ofSeconds(1))
            .map(ReactiveSupplierConsumerApplication::newMessage)
            .log("eventSupplier");
  }

  @Bean
  public Consumer<Flux<Message<PageViewEvent>>> eventSink(
      Consumer<Message<PageViewEvent>> consumer) {
    return flux -> flux.log("eventSink").subscribe(consumer);
  }
}
