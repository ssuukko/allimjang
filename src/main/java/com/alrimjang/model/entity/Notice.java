  package com.alrimjang.model.entity;

  import lombok.*;

  import java.time.LocalDateTime;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public class Notice {
      private String id;  // UUID
      private String title;
      private String content;
      private String authorId;
      private String authorName;
      private LocalDateTime createdAt;
      private LocalDateTime updatedAt;
      private Boolean isImportant;
      private Integer viewCount;
  }