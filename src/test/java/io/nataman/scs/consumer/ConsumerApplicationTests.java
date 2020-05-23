package io.nataman.scs.consumer;

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
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.KafkaMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class})
@Log4j2
class ConsumerApplicationTests {

  static ObjectMapper objectMapper;
  @Autowired
  private InputDestination input;
  @Autowired
  private OutputDestination output;

  @BeforeAll
  static void setup() {
    objectMapper = new ObjectMapper();
  }

  private static void testHeaders(Message<byte[]> message) {
    assertThat(message.getHeaders())
            .containsEntry(KafkaHeaders.MESSAGE_KEY, "test")
            .containsEntry(KafkaMessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
  }

  @Test
  @SneakyThrows
  void contextLoads() {
    var sendEvent = PageViewEvent.builder().userid("test").page("page").duration(1).build();
    var sendMessage =
            MessageBuilder.withPayload(sendEvent)
                    .setHeader(KafkaMessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "test")
                    .build();

    input.send(sendMessage);

    var receivedMessage = output.receive();
    var receivedEvent = objectMapper.readValue(receivedMessage.getPayload(), PageViewEvent.class);

    assertThat(receivedMessage).satisfies(ConsumerApplicationTests::testHeaders);
    assertThat(receivedEvent)
            .is(new Condition<>(e -> e.getUserid().equals("TEST"), "uppercase name"))
            .isEqualTo(sendEvent.withUserid("TEST"));
  }

  @Test
  void upperCaseNameTest() {
    var upperCaseFn = new ConsumerApplication().upperCaseName();
    var inEvent = PageViewEvent.builder().userid("test").page("page").duration(1).build();

    assertThat(upperCaseFn.apply(inEvent)).extracting(PageViewEvent::getUserid).isEqualTo("TEST");
  }
}
