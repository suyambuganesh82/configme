package com.javaworm.configme.schedulers.http;

import com.javaworm.configme.ConfigSource;
import com.javaworm.configme.sources.HttpSourceConfig;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestFactory {

  private Map<String, AuthRequestFilter> requestFilterMappings = new HashMap<>();

  public HttpRequestFactory(KubernetesClient k8sClient) {
    requestFilterMappings.put("none", new NoneRequestFilter());
    requestFilterMappings.put("bearer", new BearerRequestFilter(k8sClient));
  }

  public HttpRequest create(ConfigSource<HttpSourceConfig> configSource) {
    final var httpSourceConfig = configSource.getSourceConfig();
    final var url = httpSourceConfig.getUrl();
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();

    final var requestFilter =
        requestFilterMappings.get(httpSourceConfig.getHttpAuthenticationMethod());
    if (requestFilter != null) {
      requestFilter.filter(requestBuilder, configSource);
    }

    return requestBuilder.build();
  }
}
