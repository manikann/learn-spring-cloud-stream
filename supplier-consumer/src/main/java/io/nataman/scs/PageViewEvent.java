package io.nataman.scs;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
public class PageViewEvent {

  @NotBlank(message = "userid is mandatory")
  String userid;

  @NotBlank(message = "page is mandatory")
  String page;

  @Min(value = 1, message = "duration cannot be negative")
  int duration;
}
