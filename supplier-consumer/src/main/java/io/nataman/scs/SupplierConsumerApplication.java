package io.nataman.scs;

import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY;
import static org.springframework.messaging.MessageHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
public class SupplierConsumerApplication {

  private final Validator validator;
  private final AtomicInteger msgIndex = new AtomicInteger(1);

  public static void main(String[] args) {
    SpringApplication.run(SupplierConsumerApplication.class, args);
  }

  @Bean
  public Supplier<Message<PageViewEvent>> eventSupplier() {
    return () -> {
      var payload =
          PageViewEvent.builder()
              .userid("user" + msgIndex.get())
              .page("page")
              .duration(msgIndex.incrementAndGet() % 10)
              .build();
      var msg =
          MessageBuilder.withPayload(payload)
              .setHeader(MESSAGE_KEY, "message-key")
              .setHeader(CONTENT_TYPE, APPLICATION_JSON)
              .build();
      log.info("eventSupplier: {}", msg);
      return msg;
    };
  }

  @Bean
  public Consumer<Message<PageViewEvent>> eventSink() {
    return validateMessage().andThen(processMessage());
  }

  @Bean
  public Consumer<Message<PageViewEvent>> processMessage() {
    return msg -> log.info("eventSink: {}", msg);
  }

  public Consumer<Message<PageViewEvent>> validateMessage() {
    return m -> {
      var errorMessage =
          validator.validate(m.getPayload()).stream()
              .map(
                  v ->
                      String.format(
                          "{ path: '%s', invalidValue: '%s', message: '%s'}",
                          v.getPropertyPath(), v.getInvalidValue(), v.getMessage()))
              .collect(Collectors.joining(";"));
      Optional.of(errorMessage)
          .filter(e -> !e.isBlank())
          .ifPresent(
              s -> {
                log.warn("validationFailed: {} {}", s, m);
                throw new ValidationFailedException(s);
              });
      log.debug("validationPassed: {}", m);
    };
  }
}

