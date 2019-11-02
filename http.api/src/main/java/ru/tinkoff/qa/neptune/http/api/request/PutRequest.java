package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

public final class PutRequest extends RequestBuilder {

    private PutRequest(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        super(uri);
        checkArgument(nonNull(bodyPublisher), "Body publisher should not be a null value");
        builder.PUT(bodyPublisher);
    }

    public static PutRequest PUT(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        return new PutRequest(uri, bodyPublisher);
    }
}
