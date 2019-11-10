package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.http.api.HttpStepContext;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Designed to get http response
 * @param <T> is a type of a response body
 */
public final class GetResponse<T> implements Function<HttpStepContext, HttpResponse<T>> {

    private final HttpRequest request;
    private final HttpResponse.BodyHandler<T> handler;

    /**
     * Creates an instance that retrieves an http response with desired body
     *
     * @param request a http request to get response of
     * @param handler a http response body handler
     * @param <T> is a type of a response body
     * @return a new {@link GetResponse}
     */
    public static <T> GetResponse<T> getResponse(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        return new GetResponse<>(request, handler);
    }

    private GetResponse(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        checkNotNull(request, "Http request should be defined");
        checkNotNull(handler, "Http body handler should be defined");
        this.request = request;
        this.handler = handler;
    }

    @Override
    public HttpResponse<T> apply(HttpStepContext httpStepContext) {
        try {
            return httpStepContext.getCurrentClient().send(request, handler);
        } catch (IOException|InterruptedException e) {
           throw new RuntimeException(e);
        }
    }
}
