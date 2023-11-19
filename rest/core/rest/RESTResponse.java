package com.marinos.xyz.budgetmanagement.core.rest;

import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

public class RESTResponse<T> extends ResponseEntity<RESTResponseBody<T>> {

  public RESTResponse(HttpStatusCode status) {
    super(status);
  }

  public RESTResponse(T body, HttpStatusCode status) {
    super(withEnvelope(body, status), status);
  }
  public RESTResponse(T body, HttpStatusCode status,long totalResults) {
    super(withEnvelope(body, status,totalResults), status);
  }

  public RESTResponse(MultiValueMap<String, String> headers, HttpStatusCode status) {
    super(headers, status);
  }

  public RESTResponse(T body, MultiValueMap<String, String> headers, HttpStatusCode status) {
    super(withEnvelope(body, status), headers, status);
  }

  public RESTResponse(T body, MultiValueMap<String, String> headers, int rawStatus) {
    super(withEnvelope(body, HttpStatusCode.valueOf(rawStatus)), headers, rawStatus);
  }

  public static <T> ResponseEntity<T> ok(@Nullable T body) {
    var status = HttpStatusCode.valueOf(200);
    return (ResponseEntity<T>) new RESTResponse<T>(body, status);
  }
  public static <T> ResponseEntity<T> ok(@Nullable T body,long totalResults) {
    var status = HttpStatusCode.valueOf(200);
    return (ResponseEntity<T>) new RESTResponse<T>(body, status,totalResults);
  }

  private static <T> RESTResponseBody<T> withEnvelope(T body, HttpStatusCode status) {
    long totalResults = 1L;
    if (body instanceof Collection<?>) {
      totalResults=((Collection) body).size();
    }
    return withEnvelope(body, status,totalResults);
  }
  private static <T> RESTResponseBody<T> withEnvelope(T body, HttpStatusCode status,long totalResults) {
    var response = RESTResponseBody.<T>builder().data(body).status(status).total(totalResults).timestamp(LocalDateTime.now()).build();
    return response;
  }
}
