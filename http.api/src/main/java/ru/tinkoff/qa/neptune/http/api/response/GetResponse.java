package ru.tinkoff.qa.neptune.http.api.response;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.http.HttpResponse.BodyHandlers.discarding;

/**
 * Designed to get http response
 * @param <T> is a type of a response body
 */
public final class GetResponse<T> implements Function<HttpClient, HttpResponse<T>> {

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

    /**
     * Creates an instance that retrieves an http response. Body of the response has no matter.
     *
     * @param request a http request to get response of
     * @return a new {@link GetResponse}
     */
    public static GetResponse<Void> getResponse(HttpRequest request) {
        return getResponse(request, discarding());
    }

    private GetResponse(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        checkNotNull(request, "Http request should be defined");
        checkNotNull(handler, "Http body handler should be defined");
        this.request = request;
        this.handler = handler;
    }

    @Override
    public HttpResponse<T> apply(HttpClient httpClient) {
        try {
            return httpClient.send(request, handler);
        } catch (IOException|InterruptedException e) {
           throw new RuntimeException(e);
        }
    }
}