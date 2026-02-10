  package com.alrimjang.model.entity;

  import lombok.*;

  import java.time.LocalDateTime;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public class Notice {
      private Long id;
      private String title;
      private String content;
      private LocalDateTime createdAt;
  }