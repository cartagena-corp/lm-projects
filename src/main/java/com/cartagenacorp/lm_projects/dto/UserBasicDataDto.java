package com.cartagenacorp.lm_projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicDataDto {
    UUID id;
    String firstName;
    String lastName;
    String picture;
    String email;
    String role;
    LocalDateTime createdAt;
}
