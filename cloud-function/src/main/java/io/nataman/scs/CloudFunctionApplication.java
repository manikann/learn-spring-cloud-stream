package io.nataman.scs;

import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Log4j2
public class CloudFunctionApplication {

  public static void main(String[] args) {
    SpringApplication.run(CloudFunctionApplication.class, args);
  }

  @Bean
  public Function<PageViewEvent, PageViewEvent> toUpperCaseUserid() {
    return pageViewEvent -> pageViewEvent.withUserid(pageViewEvent.getUserid().toUpperCase());
  }

  public Supplier<PageViewEvent> eventSupplier() {
    return () -> {
      var ret = PageViewEvent.builder().userid("supplier").page("p").duration(100).build();
      log.info("eventSupplier: {}", ret);
      return ret;
    };
  }
}
