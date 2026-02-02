package com.rsvp_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class RsvpRequestDto {

  @NotBlank
  private String name;

  @NotBlank
  @Email
  private String email;

  @NotNull
  @Min(1)
  @Max(999)
  private Integer groupNumber;

  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

  public String getEmail() {
      return email;
  }
  public void setEmail(String email) {
      this.email = email;
  }

  public Integer getGroupNumber() {
      return groupNumber;
  }

  public void setGroupNumber(Integer groupNumber) {
      this.groupNumber = groupNumber;
  }
}
