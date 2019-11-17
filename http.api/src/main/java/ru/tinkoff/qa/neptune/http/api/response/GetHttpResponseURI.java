package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Designed to create an instance that retrieves URI that a response was received from.
 *
 * Returns the {@code URI} that the response was received from. This may be
 * different from the request {@code URI} if redirection occurred.
 *
 * @param <T> is a type of an input object that used to get URI of http response.
 */
public final class GetHttpResponseURI<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, URI, HttpResponse<?>, GetHttpResponseURI<T>> {

    private GetHttpResponseURI(String description) {
        super(description, HttpResponse::uri);
    }

    /**
     * Creates an instance that sends http request and returns URI of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link HttpRequest}
     * @return a new {@link GetHttpResponseURI}
     */
    public static GetHttpResponseURI<HttpStepContext> httpResponseURIOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpResponseURI<HttpStepContext>(format("URI of a received response. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves URI of the http response.
     *
     * @param response than already received
     * @return URI
     */
    public static URI httpResponseURIOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpResponseURI<HttpResponse<?>>(format("Response URI. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
