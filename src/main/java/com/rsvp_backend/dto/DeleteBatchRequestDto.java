package com.rsvp_backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class DeleteBatchRequestDto {

    @NotEmpty(message = "ids must not be empty")
    private List<String> ids;

    public List<String> getIds() { return ids; }
    public void setIds(List<String> ids) { this.ids = ids; }
}