package com.alrimjang.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatTaskCreateRequest {

    @NotBlank(message = "할 일 제목은 필수입니다.")
    @Size(max = 120, message = "할 일 제목은 120자 이하여야 합니다.")
    private String title;

    @Size(max = 2000, message = "상세 내용은 2000자 이하여야 합니다.")
    private String description;

    @NotNull(message = "마감일시는 필수입니다.")
    private LocalDateTime deadlineAt;

    @NotBlank(message = "담당자 아이디는 필수입니다.")
    @Size(max = 50, message = "담당자 아이디는 50자 이하여야 합니다.")
    private String assigneeUsername;
}
