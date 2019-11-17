package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Designed to create an instance that retrieves the HTTP protocol version that is used for htp response
 *
 *
 * @param <T> is a type of an input object that used to get HTTP protocol version of a response.
 */
public final class GetHttpVersion<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, HttpClient.Version, HttpResponse<?>, GetHttpVersion<T>> {

    private GetHttpVersion(String description) {
        super(description, HttpResponse::version);
    }

    /**
     * Creates an instance that sends http request and returns HTTP protocol version of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link HttpRequest}
     * @return a new {@link GetHttpVersion}
     */
    public static GetHttpVersion<HttpStepContext> httProtocolVersionOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpVersion<HttpStepContext>(format("HTTP protocol version of a received response. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves HTTP protocol version of the http response.
     *
     * @param response than already received
     * @return HTTP protocol version
     */
    public static HttpClient.Version httProtocolVersionOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpVersion<HttpResponse<?>>(format("HTTP protocol version. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
