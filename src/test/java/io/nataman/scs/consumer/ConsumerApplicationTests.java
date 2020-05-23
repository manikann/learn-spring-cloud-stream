package io.nataman.scs.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.KafkaMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class})
@Log4j2
class ConsumerApplicationTests {

    @Autowired
    private InputDestination input;

    @Autowired
    private OutputDestination output;

    static ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @SneakyThrows
    void contextLoads() {
        var sendEvent = ConsumerApplication.PageViewEvent.builder()
                .userid("test")
                .page("page")
                .duration(1)
                .build();
        var sendMessage = MessageBuilder
                .withPayload(sendEvent)
                .setHeader(KafkaMessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .setHeader(KafkaHeaders.MESSAGE_KEY, "test")
                .build();

        input.send(sendMessage);
        log.info("Sent: {}", sendMessage);

        var receivedMessage = output.receive();
        log.info("Received: {}", receivedMessage);

        var receivedEvent = objectMapper.readValue(receivedMessage.getPayload(), ConsumerApplication.PageViewEvent.class);
        log.info("Received Object: {}", receivedEvent);
        assertThat(receivedEvent)
                .is(new Condition<>(e -> e.getUserid().equals("TEST"), "uppercase name"))
                .isEqualTo(sendEvent.withUserid("TEST"));
    }

    @Test
    void upperCaseNameTest() {
        var upperCaseFn = new ConsumerApplication().upperCaseName();
        log.info( "upperCaseFn: {}", upperCaseFn);
        var inEvent = ConsumerApplication.PageViewEvent.builder()
                .userid("test")
                .page("page")
                .duration(1)
                .build();

        assertThat(upperCaseFn.apply(inEvent))
                .isEqualTo(inEvent.withUserid("TEST"));
    }
}
