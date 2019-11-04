package ru.tinkoff.qa.neptune.http.api.request;

/**
 * Designed to create a GET-request
 */
public final class GetRequest extends RequestBuilder {

    private GetRequest(String uri) {
        super(uri);
        builder.GET();
    }

    /**
     Creates an instance that builds a GET request.
     *
     * @param uri is a request end point
     * @return new {@link GetRequest}
     */
    public static GetRequest GET(String uri) {
        return new GetRequest(uri);
    }
}
