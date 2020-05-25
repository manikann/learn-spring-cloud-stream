package io.nataman.scs;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootTest(args = "--spring.cloud.stream.function.definition=eventSupplier")
@Import({TestChannelBinderConfiguration.class})
@Log4j2
class SupplierApplicationTests {

  private static ObjectMapper objectMapper;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private OutputDestination output;

  @BeforeAll
  static void setup() {
    objectMapper = new ObjectMapper();
  }

  private static boolean validatePageViewEvent(PageViewEvent e) {
    return e.getUserid().equals("user1");
  }

  @Test
  @SneakyThrows
  void contextLoads() {
    var receivedMessage = output.receive();
    log.info("receivedMessage: {}", receivedMessage);
    var receivedEvent = objectMapper.readValue(receivedMessage.getPayload(), PageViewEvent.class);
    assertThat(receivedEvent)
        .is(
            new Condition<>(
                SupplierApplicationTests::validatePageViewEvent, "check userid=source"));
  }
}
