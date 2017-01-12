/*
 * Knot.x - Reactive microservice assembler - Http Service Adapter
 *
 * Copyright (C) 2016 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.knotx.adapter.action.http.impl;

import com.cognifide.knotx.adapter.api.AbstractAdapterProxy;
import com.cognifide.knotx.adapter.common.http.HttpAdapterConfiguration;
import com.cognifide.knotx.adapter.common.http.HttpClientFacade;
import com.cognifide.knotx.dataobjects.AdapterRequest;
import com.cognifide.knotx.dataobjects.AdapterResponse;
import com.cognifide.knotx.dataobjects.ClientResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import rx.Observable;

public class HttpActionAdapterProxyImpl extends AbstractAdapterProxy {

  private HttpClientFacade httpClientFacade;

  public HttpActionAdapterProxyImpl(Vertx vertx, HttpAdapterConfiguration configuration) {
    super(configuration);
    this.httpClientFacade = new HttpClientFacade(getHttpClient(vertx, configuration), configuration.getServices());
  }

  @Override
  protected Observable<AdapterResponse> processRequest(AdapterRequest request) {
    return httpClientFacade.process(request, HttpMethod.POST).map(this::prepareResponse);
  }

  private HttpClient getHttpClient(Vertx vertx, HttpAdapterConfiguration configuration) {
    JsonObject clientOptions = configuration.getClientOptions();
    return clientOptions.isEmpty() ?
        vertx.createHttpClient() : vertx.createHttpClient(new HttpClientOptions(clientOptions));
  }

  private AdapterResponse prepareResponse(ClientResponse response) {
    AdapterResponse result = new AdapterResponse();

    if (response.getStatusCode() == HttpResponseStatus.OK.code()) {
      if (isJsonBody(response.getBody()) && response.getBody().toJsonObject().containsKey("validationErrors")) {
        result.setSignal("error");
      } else {
        result.setSignal("success");
      }
    }
    result.setResponse(response);

    return result;
  }

  private boolean isJsonBody(Buffer bodyBuffer) {
    String body = bodyBuffer.toString().trim();
    if (body.charAt(0) == '{' && body.charAt(body.length() - 1) == '}') {
      return true;
    } else {
      return false;
    }
  }
}
