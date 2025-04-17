package com.cartagenacorp.lm_projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatedByDto {
    UUID id;
    String firstName;
    String lastName;
    String picture;
}
