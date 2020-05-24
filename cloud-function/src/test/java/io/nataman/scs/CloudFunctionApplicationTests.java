package io.nataman.scs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY;
import static org.springframework.messaging.MessageHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class})
@Log4j2
class CloudFunctionApplicationTests {

  private static ObjectMapper objectMapper;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private InputDestination input;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private OutputDestination output;

  @BeforeAll
  static void setup() {
    objectMapper = new ObjectMapper();
  }

  private static void testHeaders(Message<byte[]> message) {
    assertThat(message.getHeaders())
        .containsEntry(MESSAGE_KEY, "test")
        .containsEntry(CONTENT_TYPE, APPLICATION_JSON);
  }

  @Test
  @SneakyThrows
  void contextLoads() {
    var sendEvent = PageViewEvent.builder().userid("test").page("page").duration(1).build();
    var sendMessage =
        MessageBuilder.withPayload(sendEvent)
            .setHeader(CONTENT_TYPE, APPLICATION_JSON)
            .setHeader(MESSAGE_KEY, "test")
            .build();

    input.send(sendMessage);

    var receivedMessage = output.receive();
    var receivedEvent = objectMapper.readValue(receivedMessage.getPayload(), PageViewEvent.class);

    assertThat(receivedMessage).satisfies(CloudFunctionApplicationTests::testHeaders);
    assertThat(receivedEvent)
        .is(new Condition<>(e -> e.getUserid().equals("TEST"), "uppercase name"))
        .isEqualTo(sendEvent.withUserid("TEST"));
  }

  @Test
  void upperCaseNameTest() {
    var upperCaseFn = new CloudFunctionApplication().toUpperCaseUserid();
    var inEvent = PageViewEvent.builder().userid("test").page("page").duration(1).build();

    assertThat(upperCaseFn.apply(inEvent)).extracting(PageViewEvent::getUserid).isEqualTo("TEST");
  }
}
