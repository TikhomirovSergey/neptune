package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

public final class PostRequest extends RequestBuilder {

    private PostRequest(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        super(uri);
        checkArgument(nonNull(bodyPublisher), "Body publisher should not be a null value");
        builder.POST(bodyPublisher);
    }

    public static PostRequest POST(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        return new PostRequest(uri, bodyPublisher);
    }
}
