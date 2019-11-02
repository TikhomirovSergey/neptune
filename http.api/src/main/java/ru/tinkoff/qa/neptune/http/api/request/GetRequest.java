package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

public final class GetRequest extends RequestBuilder {

    private GetRequest(String uri) {
        super(uri);
        builder.GET();
    }

    public static GetRequest GET(String uri) {
        return new GetRequest(uri);
    }
}
