package io.nataman.scs.consumer;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
@Log4j2
public class ConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerApplication.class, args);
  }

  @Bean
  public Function<PageViewEvent, PageViewEvent> upperCaseName() {
    return pageViewEvent -> pageViewEvent.withUserid(pageViewEvent.getUserid().toUpperCase());
  }

}
