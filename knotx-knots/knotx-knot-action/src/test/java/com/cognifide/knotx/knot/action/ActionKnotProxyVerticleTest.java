/*
 * Knot.x - Reactive microservice assembler - Action Knot Verticle
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
package com.cognifide.knotx.knot.action;

import com.cognifide.knotx.dataobjects.AdapterRequest;
import com.cognifide.knotx.dataobjects.AdapterResponse;
import com.cognifide.knotx.dataobjects.ClientResponse;
import com.cognifide.knotx.dataobjects.Fragment;
import com.cognifide.knotx.dataobjects.KnotContext;
import com.cognifide.knotx.http.MultiMapCollector;
import com.cognifide.knotx.junit.KnotContextFactory;
import com.cognifide.knotx.junit.Logback;
import com.cognifide.knotx.launcher.junit.FileReader;
import com.cognifide.knotx.launcher.junit.KnotxConfiguration;
import com.cognifide.knotx.launcher.junit.TestVertxDeployer;
import com.cognifide.knotx.proxy.AdapterProxy;
import com.cognifide.knotx.rxjava.proxy.KnotProxy;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import rx.functions.Action1;
import rx.functions.Func1;

@RunWith(VertxUnitRunner.class)
public class ActionKnotProxyVerticleTest {

  public static final String EXPECTED_KNOT_TRANSITION = "next";
  private final static String ADDRESS = "knotx.knot.action";
  private final static String HIDDEN_INPUT_TAG_NAME = "snippet-identifier";
  private static final String FRAGMENT_KNOTS = "data-knotx-knots";
  private final static String FRAGMENT_REDIRECT_IDENTIFIER = "someId123";
  private final static Fragment FIRST_FRAGMENT = Fragment.raw("<html><head></head><body>");
  private final static Fragment LAST_FRAGMENT = Fragment.raw("</body></html>");
  private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings()
      .escapeMode(Entities.EscapeMode.xhtml)
      .indentAmount(0)
      .prettyPrint(false);
  //Test Runner Rule of Verts
  private RunTestOnContext vertx = new RunTestOnContext();

  //Test Runner Rule of Knotx
  private TestVertxDeployer knotx = new TestVertxDeployer(vertx);

  //Junit Rule, sets up logger, prepares verts, starts verticles according to the config (supplied in annotation of test method)
  @Rule
  public RuleChain chain = RuleChain.outerRule(new Logback()).around(vertx).around(knotx);

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithNoActionFragments_expectResponseOkNoFragmentChanges(TestContext context) throws Exception {
    String expectedTemplatingFragment = FileReader.readText("fragment_templating_out.txt");
    KnotContext knotContext = createKnotContext(FIRST_FRAGMENT, LAST_FRAGMENT, "fragment_templating_in.txt");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.OK.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertTrue(clientResponse.getTransition() != null);
          context.assertEquals("next", clientResponse.getTransition());
          context.assertTrue(clientResponse.getFragments() != null);

          List<Fragment> fragments = clientResponse.getFragments();
          context.assertEquals(FIRST_FRAGMENT.content(), fragments.get(0).content());
          context.assertEquals(expectedTemplatingFragment, fragments.get(1).content());
          context.assertEquals(LAST_FRAGMENT.content(), fragments.get(2).content());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithTwoActionFragments_expectResponseOkTwoFragmentChanges(TestContext context) throws Exception {
    String expectedRedirectFormFragment = FileReader.readText("fragment_form_redirect_out.txt");
    String expectedSelfFormFragment = FileReader.readText("fragment_form_self_out.txt");
    KnotContext knotContext = createKnotContext(FIRST_FRAGMENT, LAST_FRAGMENT, "fragment_form_redirect_in.txt", "fragment_form_self_in.txt");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.OK.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertTrue(clientResponse.getTransition() != null);
          context.assertEquals(EXPECTED_KNOT_TRANSITION, clientResponse.getTransition());
          context.assertTrue(clientResponse.getFragments() != null);

          List<Fragment> fragments = clientResponse.getFragments();
          context.assertEquals(FIRST_FRAGMENT.content(), fragments.get(0).content());
          context.assertEquals(clean(expectedRedirectFormFragment), clean(fragments.get(1).content()));
          context.assertEquals(clean(expectedSelfFormFragment), clean(fragments.get(2).content()));
          context.assertEquals(LAST_FRAGMENT.content(), fragments.get(3).content());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithActionFragmentWithoutIdentifier_expectResponseOkWithOneFragmentChanges(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_no_identifier_in.txt");
    String expectedFragmentHtml = FileReader.readText("fragment_form_no_identifier_out.txt");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.OK.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertTrue(clientResponse.getTransition() != null);
          context.assertEquals(EXPECTED_KNOT_TRANSITION, clientResponse.getTransition());
          context.assertTrue(clientResponse.getFragments() != null);

          List<Fragment> fragments = clientResponse.getFragments();
          context.assertEquals(fragments.size(), 1);
          context.assertEquals(clean(expectedFragmentHtml), clean(fragments.get(0).content()));
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithActionFragmentActionHandlerNotExists_expectStatusCode500(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_actionhandler_not_exists_in.txt");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertFalse(clientResponse.getTransition() != null);
          context.assertFalse(clientResponse.getFragments() != null);
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callPostWithTwoActionFragments_expectResponseOkWithTransitionStep2(TestContext context) throws Exception {
    createMockAdapter("address-redirect", "", "step2");
    KnotContext knotContext = createKnotContext("fragment_form_redirect_in.txt", "fragment_form_self_in.txt");
    knotContext.getClientRequest()
        .setMethod(HttpMethod.POST)
        .setFormAttributes(MultiMap.caseInsensitiveMultiMap().add(HIDDEN_INPUT_TAG_NAME, FRAGMENT_REDIRECT_IDENTIFIER));

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.MOVED_PERMANENTLY.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertEquals("/content/form/step2.html", clientResponse.getClientResponse().getHeaders().get("Location"));
          context.assertFalse(clientResponse.getTransition() != null);
          context.assertFalse(clientResponse.getFragments() != null);
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callPostWithActionFragmentWithoutRequestedFragmentIdentifier_expectStatusCode500(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_incorrect_identifier_in.txt");
    knotContext.getClientRequest().setMethod(HttpMethod.POST);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.NOT_FOUND.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertFalse(clientResponse.getFragments() != null);
          context.assertFalse(clientResponse.getTransition() != null);
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callPostWithActionFragmentWithIncorrectSnippetId_expectStatusCode500(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_redirect_in.txt");
    knotContext.getClientRequest().setMethod(HttpMethod.POST)
        .setFormAttributes(MultiMap.caseInsensitiveMultiMap().add(HIDDEN_INPUT_TAG_NAME, "snippet_id_not_exists"));

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.NOT_FOUND.code(), clientResponse.getClientResponse().getStatusCode());
          context.assertFalse(clientResponse.getFragments() != null);
          context.assertFalse(clientResponse.getTransition() != null);
        },
        error -> context.fail(error.getMessage()));
  }

  private void callActionKnotWithAssertions(TestContext context, KnotContext knotContext, Action1<KnotContext> onSuccess,
      Action1<Throwable> onError) {
    Async async = context.async();

    KnotProxy actionKnot = KnotProxy.createProxy(new Vertx(vertx.vertx()), ADDRESS);

    actionKnot.processObservable(knotContext)
        .subscribe(
            onSuccess,
            onError,
            async::complete
        );
  }

  private KnotContext createKnotContext(String... snippetFilenames) throws Exception {
    return createKnotContext(null, null, snippetFilenames);
  }

  private KnotContext createKnotContext(Fragment firstFragment, Fragment lastFragment, String... snippetFilenames) throws Exception {
    List<Fragment> fragments = Lists.newArrayList();
    Optional.ofNullable(firstFragment).ifPresent(fragments::add);
    for (String file : snippetFilenames) {
      String fileContent = FileReader.readText(file);
      String fragmentIdentifiers = Jsoup.parse(fileContent).getElementsByAttribute(FRAGMENT_KNOTS).attr(
          FRAGMENT_KNOTS);
      fragments.add(Fragment.snippet(Arrays.asList(fragmentIdentifiers.split(",")), fileContent));
    }
    Optional.ofNullable(lastFragment).ifPresent(fragments::add);

    return KnotContextFactory.empty(fragments);
  }

  private String clean(String text) {
    String cleanText = text.replace("\n", "").replaceAll(">(\\s)+<", "><").replaceAll(">(\\s)+\\{", ">{").replaceAll("\\}(\\s)+<", "}<");
    return Jsoup.parse(cleanText, "UTF-8", Parser.xmlParser())
        .outputSettings(OUTPUT_SETTINGS)
        .html()
        .trim();
  }

  private void createMockAdapter(String adddress, String addToBody, String signal) {
    createMockAdapter(adddress, addToBody, signal, Collections.emptyMap());
  }

  private void createMockAdapter(String adddress, String addToBody, String signal, Map<String, List<String>> headers) {
    Func1<AdapterRequest, AdapterResponse> adapter = adapterRequest -> {
      ClientResponse response = new ClientResponse();
      response.setStatusCode(HttpResponseStatus.OK.code());
      response.setBody(Buffer.buffer().appendString(addToBody));
      response.setHeaders(headers.keySet().stream().collect(MultiMapCollector.toMultimap(o -> o, headers::get)));
      return new AdapterResponse().setResponse(response).setSignal(signal);
    };

    ProxyHelper
        .registerService(
            com.cognifide.knotx.proxy.AdapterProxy.class, vertx.vertx(), new MockAdapterImpl(adapter),
            adddress);
  }

  private class MockAdapterImpl implements AdapterProxy {

    private final Func1<AdapterRequest, AdapterResponse> adapter;

    private MockAdapterImpl(Func1<AdapterRequest, AdapterResponse> adapter) {
      this.adapter = adapter;
    }

    @Override
    public void process(AdapterRequest request, Handler<AsyncResult<AdapterResponse>> result) {
      result.handle(Future.succeededFuture(adapter.call(request)));
    }
  }

}
