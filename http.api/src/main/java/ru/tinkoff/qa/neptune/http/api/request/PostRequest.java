package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

/**
 * Designed to create a POST-request
 */
public final class PostRequest extends RequestBuilder {

    private PostRequest(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        super(uri);
        checkArgument(nonNull(bodyPublisher), "Body publisher should not be a null value");
        builder.POST(bodyPublisher);
    }

    /**
     * Creates an instance that builds a POST request.
     *
     * @param uri is a request end point
     * @param bodyPublisher the body publisher
     * @return new {@link PutRequest}
     */
    public static PostRequest POST(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        return new PostRequest(uri, bodyPublisher);
    }
}
