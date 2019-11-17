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
 *  Designed to create an instance that retrieves corresponding request of http response.
 *
 * <p> The returned {@code HttpRequest} may not be the initiating request
 * provided when {@linkplain HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
 * sending}. For example, if the initiating request was redirected, then the
 * request returned by this method will have the redirected URI, which will
 * be different from the initiating request URI.
 *
 * @param <T> is a type of an input object that used to get the corresponding request of http response.
 */
public final class GetHttpCorrespondingRequest<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, HttpRequest, HttpResponse<?>, GetHttpCorrespondingRequest<T>> {

    private GetHttpCorrespondingRequest(String description) {
        super(description, HttpResponse::request);
    }

    /**
     * Creates an instance that sends http request and returns the corresponding request of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link java.net.http.HttpRequest}
     * @return a new {@link GetHttpCorrespondingRequest}
     */
    public static GetHttpCorrespondingRequest<HttpStepContext> correspondingHttpRequestOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpCorrespondingRequest<HttpStepContext>(format("Corresponding request of a received response. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves the corresponding request of the http response.
     *
     * @param response than already received
     * @return the corresponding request
     */
    public static HttpRequest correspondingHttpRequestOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpCorrespondingRequest<HttpResponse<?>>(format("Corresponding request. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
