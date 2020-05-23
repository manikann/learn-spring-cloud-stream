package io.nataman.scs.consumer;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
public class PageViewEvent {
  @NonNull String userid;
  @NonNull String page;
  int duration;
}
