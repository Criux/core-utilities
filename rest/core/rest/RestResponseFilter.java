package com.marinos.xyz.budgetmanagement.core.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class RestResponseFilter implements Filter {

  private final ObjectMapper objectMapper;
  private final List<String>EXCLUDE_PATHS=List.of("/actuator");

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    var path =((HttpServletRequest) servletRequest).getRequestURI();
    if(EXCLUDE_PATHS.stream().anyMatch(filter->path.startsWith(path))){
      filterChain.doFilter(servletRequest,servletResponse);
    }else{
      overrideResponse(servletRequest,servletResponse,filterChain);
    }

  }
  private void overrideResponse(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws ServletException, IOException {
    var start = System.currentTimeMillis();
    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    ByteArrayPrinter pw = new ByteArrayPrinter();
    HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
      @Override
      public PrintWriter getWriter() {
        return pw.getWriter();
      }

      @Override
      public ServletOutputStream getOutputStream() {
        return pw.getStream();
      }
    };
    filterChain.doFilter(httpRequest, wrappedResp);
    byte[] bytes = pw.toByteArray();
    String respBody = new String(bytes);
    String url = httpRequest.getRequestURI();
    String queryString = (httpRequest).getQueryString();
    try{
      var responseBody = objectMapper.readValue(respBody, RESTResponseBody.class);
      responseBody.setPath(url + "?" + queryString);
      responseBody.setStatus(HttpStatusCode.valueOf(response.getStatus()));
      responseBody.setProcessedInMs((System.currentTimeMillis() - start));
      response.getOutputStream().write(objectMapper.writeValueAsBytes(responseBody));
    }catch (JsonMappingException e){
      log.warn("Response is not of type RESTResponseEnvelope for path {}",url);
      response.getOutputStream().write(bytes);
    }
  }
}
