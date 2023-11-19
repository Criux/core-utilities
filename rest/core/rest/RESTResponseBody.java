package com.marinos.xyz.budgetmanagement.core.rest;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
@Builder
public class RESTResponseBody<T> {

  LocalDateTime timestamp;
  String path;
  @JsonIgnore
  HttpStatusCode status;
  Long processedInMs;
  Long total;
  T data;

  @JsonGetter
  public int getStatus() {
    return status.value();
  }
}
