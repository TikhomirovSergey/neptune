package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 *  Designed to create an instance that retrieves headers of http response.

 *
 * @param <T> is a type of an input object that used to get headers of http response.
 */
public final class GetHttpHeaders<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, Map<String, List<String>>, HttpResponse<?>, GetHttpHeaders<T>> {

    private GetHttpHeaders(String description) {
        super(description, response -> response.headers().map());
    }

    /**
     * Creates an instance that sends http request and returns http headers of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link HttpRequest}
     * @return a new {@link GetHttpHeaders}
     */
    public static GetHttpHeaders<HttpStepContext> httpHeadersOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpHeaders<HttpStepContext>(format("Http headers of a received response. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves http headers of the http response.
     *
     * @param response than already received
     * @return Http headers
     */
    public static Map<String, List<String>> httpHeadersOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpHeaders<HttpResponse<?>>(format("Http headers. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
