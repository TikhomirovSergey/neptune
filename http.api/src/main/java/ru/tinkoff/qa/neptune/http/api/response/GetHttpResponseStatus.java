package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeCaptureOnFinishing;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import java.net.http.HttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 *  Designed to create an instance that retrieves status code of http response.
 *
 * @param <T> is a type of an input object that used to get value of http response status code.
 */
@MakeCaptureOnFinishing(typeOfCapture = Object.class)
public final class GetHttpResponseStatus<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, Integer, HttpResponse<?>, GetHttpResponseStatus<T>> {

    private GetHttpResponseStatus(String description) {
        super(description, HttpResponse::statusCode);
    }

    /**
     * Creates an instance that sends http request and returns status code of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link java.net.http.HttpRequest}
     * @return a new {@link GetHttpResponseStatus}
     */
    public static GetHttpResponseStatus<HttpStepContext> httpStatusCodeOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpResponseStatus<HttpStepContext>(format("Http status code. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves status code of the http response.
     *
     * @param response than already received
     * @return a value of status code
     */
    public static Integer httpStatusCodeOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpResponseStatus<HttpResponse<?>>(format("Http status code. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
