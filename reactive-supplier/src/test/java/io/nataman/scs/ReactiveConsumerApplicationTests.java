package io.nataman.scs;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY;
import static org.springframework.messaging.MessageHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

import java.util.function.Consumer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(args = "--spring.cloud.stream.function.definition=eventSink")
@Import({TestChannelBinderConfiguration.class})
@Log4j2
@DirtiesContext
class ReactiveConsumerApplicationTests {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private InputDestination input;

  @MockBean
  private Consumer<Message<PageViewEvent>> sinkConsumer;

  @Test
  void contextLoads() {
    var sendEvent = PageViewEvent.builder().userid("test").page("page").duration(1).build();
    var sendMessage =
        MessageBuilder.withPayload(sendEvent)
            .setHeader(CONTENT_TYPE, APPLICATION_JSON)
            .setHeader(MESSAGE_KEY, "test")
            .build();
    input.send(sendMessage);
    verify(sinkConsumer, times(1)).accept(sendMessage);
  }
}
