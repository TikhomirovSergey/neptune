package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import javax.net.ssl.SSLSession;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 *  Designed to create an instance that retrieves ssl session of http response.
 *  <p>
 *      It returns the {@code SSLSession} associated with the response. Returns null if this is not a
 *      <i>HTTPS</i> response.
 *  </p>
 *
 * @param <T> is a type of an input object that used to get ssl session of http response.
 */
public final class GetHttpSslSession<T> extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<T, SSLSession, HttpResponse<?>, GetHttpSslSession<T>> {

    private GetHttpSslSession(String description) {
        super(description, response -> response
                .sslSession()
                .orElse(null));
    }

    /**
     * Creates an instance that sends http request and returns the ssl session of a received http response.
     *
     * @param requestBuilder an instance of {@link RequestBuilder} that creates a {@link HttpRequest}
     * @return a new {@link GetHttpSslSession}
     */
    public static GetHttpSslSession<HttpStepContext> httpSslSessionOf(RequestBuilder requestBuilder) {
        checkNotNull(requestBuilder, "Request builder should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpSslSession<HttpStepContext>(format("Ssl session of a received response. Request: %s", requestBuilder))
                .from(context -> context.httpResponseOf(requestBuilder));
    }

    /**
     * Retrieves the ssl session of the http response.
     *
     * @param response than already received
     * @return ssl session
     */
    public static SSLSession httpSslSessionOf(HttpResponse<?> response) {
        checkNotNull(response, "Response should be defined");

        //todo provide more details as step parameters
        //todo comment for further releases
        return new GetHttpSslSession<HttpResponse<?>>(format("Ssl session. Response: %s", response))
                .from(response1 -> response1)
                .get()
                .apply(response);
    }
}
