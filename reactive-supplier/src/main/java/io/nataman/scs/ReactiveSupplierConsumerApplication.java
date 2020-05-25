package io.nataman.scs;

import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY;
import static org.springframework.kafka.support.converter.KafkaMessageHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
public class ReactiveSupplierConsumerApplication {

  private final Validator validator;

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
    return flux -> flux.log("eventSink").filter(this::validateMessage).subscribe(consumer);
  }

  private boolean validateMessage(Message<PageViewEvent> m) {
    log.debug("validateMessage: {}", m);
    var errorMessage =
        validator.validate(m.getPayload()).stream()
            .map(
                v -> {
                  log.debug("getConstraintDescriptor: {}", v.getConstraintDescriptor());
                  log.debug("getExecutableParameters: {}", v.getExecutableParameters());
                  log.debug("getExecutableReturnValue: {}", v.getExecutableReturnValue());
                  log.debug("getInvalidValue: {}", v.getInvalidValue());
                  log.debug("getPropertyPath: {}", v.getPropertyPath());
                  log.debug("getMessage: {}", v.getMessage());
                  return String.format(
                      "{ path: '%s', invalidValue: '%s', message: '%s'}",
                      v.getPropertyPath(), v.getInvalidValue(), v.getMessage());
                })
            .collect(Collectors.joining(";"));
    Optional.of(errorMessage)
        .filter(e -> !e.isBlank())
        .ifPresent(
            s -> {
              var t = new RuntimeException(s);
              log.error("validateMessage failed: [{}]", s, t);
              throw t;
            });
    return true;
  }
}
