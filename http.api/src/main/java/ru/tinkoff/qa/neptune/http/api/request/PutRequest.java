package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

/**
 * Designed to create a PUT-request
 */
public final class PutRequest extends RequestBuilder {

    private PutRequest(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        super(uri);
        checkArgument(nonNull(bodyPublisher), "Body publisher should not be a null value");
        builder.PUT(bodyPublisher);
    }

    /**
     * Creates an instance that builds a PUT request.
     *
     * @param uri is a request end point
     * @param bodyPublisher the body publisher
     * @return new {@link PutRequest}
     */
    public static PutRequest PUT(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        return new PutRequest(uri, bodyPublisher);
    }
}
