package com.rsvp_backend.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class RsvpRequestDto {

  @NotBlank
  private String name;

  @NotBlank
  @Email(message = "Invalid email format")
  private String email;

  @Size(max = 20, message = "Max 20 attendees")
  private List<String> listOfAttendees;

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

  public List<String> getListOfAttendees() {
      return listOfAttendees;
  }

  public void setListOfAttendees(List<String> listOfAttendees) {
      this.listOfAttendees = listOfAttendees;
  }
}
