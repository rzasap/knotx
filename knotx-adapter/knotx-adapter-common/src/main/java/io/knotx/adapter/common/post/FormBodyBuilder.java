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
package io.knotx.adapter.common.post;

import io.vertx.rxjava.core.MultiMap;
import org.apache.commons.lang3.StringUtils;

public class FormBodyBuilder {

  public static String createBody(final MultiMap formAttributes) {
    String result = StringUtils.EMPTY;

    if (formAttributes != null && !formAttributes.isEmpty()) {
      result = formAttributes.names().stream()
          .map(field ->
              new StringBuilder(field).append("=").append(formAttributes.get(field))
          )
          .reduce((a, b) -> a.append("&").append(b))
          .orElse(new StringBuilder()).toString();
    }

    return result;
  }
}
