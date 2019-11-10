package ru.tinkoff.qa.neptune.http.api;

import ru.tinkoff.qa.neptune.core.api.cleaning.ContextRefreshable;
import ru.tinkoff.qa.neptune.core.api.steps.context.ActionStepContext;
import ru.tinkoff.qa.neptune.core.api.steps.context.CreateWith;
import ru.tinkoff.qa.neptune.core.api.steps.context.GetStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;
import ru.tinkoff.qa.neptune.http.api.response.GetHttpResponseStatus;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static ru.tinkoff.qa.neptune.core.api.steps.StoryWriter.toGet;
import static ru.tinkoff.qa.neptune.http.api.response.GetResponse.getResponse;

@CreateWith(provider = HttpStepsParameterProvider.class)
public class HttpStepContext implements GetStepContext<HttpStepContext>, ActionStepContext<HttpStepContext>, ContextRefreshable {

    private final HttpClient client;

    public HttpStepContext(HttpClient.Builder clientBuilder) {
        this.client = clientBuilder.build();
    }

    public HttpClient getCurrentClient() {
        return client;
    }

    @Override
    public void refreshContext() {
        client.cookieHandler().ifPresent(cookieHandler ->
                ((CookieManager) cookieHandler).getCookieStore().removeAll());
    }

    /**
     * Sends http request built with {@link RequestBuilder} and receives http response that has body of desired type.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link java.net.http.HttpRequest}
     * @param handler a http response body handler
     * @param <T> is a type of a response body
     * @return a received  {@link HttpResponse}
     */
    public <T> HttpResponse<T> httpResponseOf(RequestBuilder requestBuilder,
                                              HttpResponse.BodyHandler<T> handler) {
        checkNotNull(requestBuilder, "Request builder should be defined");
        var request = requestBuilder.build();

        //todo provide more details as step parameters
        //todo comment for further releases
        return get(toGet(format("Response. Request: %s", request), getResponse(request, handler))
                .makeStringCaptureOnFinish()
                .makeFileCaptureOnFinish());
    }

    /**
     * Sends http request built with {@link RequestBuilder} and receives http response. Body of the resulted
     * response has no matter.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link java.net.http.HttpRequest}
     * @return a received  {@link HttpResponse}
     */
    public HttpResponse<Void> httpResponseOf(RequestBuilder requestBuilder) {
        return httpResponseOf(requestBuilder, discarding());
    }

    /**
     * Gets status code of resulted http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link java.net.http.HttpRequest}
     * @return a value of status code
     */
    public Integer httpStatusCodeOf(RequestBuilder requestBuilder) {
        return get(GetHttpResponseStatus.httpStatusCodeOf(requestBuilder));
    }
}
