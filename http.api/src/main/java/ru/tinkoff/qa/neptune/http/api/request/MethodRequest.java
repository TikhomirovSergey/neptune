package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

public final class MethodRequest extends RequestBuilder {

    private MethodRequest(String uri, String method, HttpRequest.BodyPublisher bodyPublisher) {
        super(uri);
        checkArgument(isNotBlank(method), "Method name should not be null or empty string");
        checkArgument(nonNull(bodyPublisher), "Body publisher should not be a null value");
        builder.method(method, bodyPublisher);
    }

    public static MethodRequest METHOD(String uri, String method, HttpRequest.BodyPublisher bodyPublisher) {
        return new MethodRequest(uri, method, bodyPublisher);
    }
}
