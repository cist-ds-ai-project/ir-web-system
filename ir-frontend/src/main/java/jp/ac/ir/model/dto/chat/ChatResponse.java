package jp.ac.ir.model.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatResponse(@JsonProperty("answer") String answer) {}
