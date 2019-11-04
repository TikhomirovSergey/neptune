package ru.tinkoff.qa.neptune.http.api.request;

import java.net.http.HttpRequest;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

/**
 * Designed to create a request with defined common or customized http method name
 * and with body publisher
 */
public final class MethodRequest extends RequestBuilder {

    private MethodRequest(String uri, String method, HttpRequest.BodyPublisher bodyPublisher) {
        super(uri);
        checkArgument(isNotBlank(method), "Method name should not be null or empty string");
        checkArgument(nonNull(bodyPublisher), "Body publisher should not be a null value");
        builder.method(method, bodyPublisher);
    }

    /**
     * Creates an instance that builds a request with defined common or customized http method name
     * and with body publisher
     *
     * @param uri is a request end point
     * @param method  method the method to use
     * @param bodyPublisher the body publisher
     * @return new {@link MethodRequest}
     */
    public static MethodRequest METHOD(String uri, String method, HttpRequest.BodyPublisher bodyPublisher) {
        return new MethodRequest(uri, method, bodyPublisher);
    }
}
