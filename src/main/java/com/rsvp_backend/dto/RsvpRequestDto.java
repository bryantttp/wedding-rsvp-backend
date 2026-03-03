package com.rsvp_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;


public class RsvpRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    // ✅ NEW: additional guests (excluding the main person)
    @Min(value = 0, message = "Additional guests cannot be less than 0")
    @Max(value = 10, message = "Additional guests cannot be more than 10")
    private Integer totalGuests; // total guests including the main person

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getTotalGuests() { return totalGuests; }
    public void setTotalGuests(Integer totalGuests) { this.totalGuests = totalGuests; }
}