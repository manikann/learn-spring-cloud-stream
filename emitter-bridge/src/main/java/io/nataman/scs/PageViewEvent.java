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

  @NotBlank(message = "Userid can't be blank")
  String userid;

  @NotBlank(message = "Page can't be blank")
  String page;

  @Min(value = 0, message = "Duration can't be less than 0")
  int duration;
}
