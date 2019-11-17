package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 *  Designed to create an instance that retrieves the previous response of http response.
 *
 * <p> Returns a previous intermediate response if it is received.
 * An intermediate response is one that is received
 * as a result of redirection or authentication. If no previous response
 * was received then null is returned.
 *
 * @param <T> is a type of an input object that used to get the previous response of http response.
 */
public final class GetHttpPreviousResponse<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, HttpResponse<?>, HttpResponse<?>, GetHttpPreviousResponse<T>> {

    private GetHttpPreviousResponse(String description) {
        super(description,
                httpResponse -> httpResponse.previousResponse().orElse(null));
    }

    /**
     * Creates an instance that sends http request and returns the previous response of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link HttpRequest}
     * @return a new {@link GetHttpPreviousResponse}
     */
    public static GetHttpPreviousResponse<HttpStepContext> previousHttpResponseOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpPreviousResponse<HttpStepContext>(format("Previous response a received response. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves the previous response of the http response.
     *
     * @param response than already received
     * @return the corresponding request
     */
    public static HttpResponse<?> previousHttpResponseOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpPreviousResponse<HttpResponse<?>>(format("Previous response. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
