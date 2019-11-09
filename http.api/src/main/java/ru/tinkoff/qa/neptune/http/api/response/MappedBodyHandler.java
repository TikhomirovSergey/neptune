package ru.tinkoff.qa.neptune.http.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;

import javax.xml.parsers.DocumentBuilder;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.net.http.HttpResponse.BodySubscribers.mapping;
import static ru.tinkoff.qa.neptune.http.api.response.body.data.FromJson.getFromJson;
import static ru.tinkoff.qa.neptune.http.api.response.body.data.GetDocument.getDocument;
import static ru.tinkoff.qa.neptune.http.api.response.body.data.GetMapped.getMapped;

/**
 * Designed to get http responses with body of desired type.
 *
 * @param <S> is an original type of a response body
 * @param <T> is a type of a final value of the response body
 */
public class MappedBodyHandler<S, T> implements HttpResponse.BodyHandler<T> {

    private final HttpResponse.BodyHandler<S> upstreamBodyHandler;
    private final Function<S, T> mapper;

    private MappedBodyHandler(HttpResponse.BodyHandler<S> upstreamBodyHandler, Function<S, T> mapper) {
        checkNotNull(upstreamBodyHandler, "Upstream body handler should be defined");
        checkNotNull(mapper, "Mapping function should be defined");

        this.upstreamBodyHandler = upstreamBodyHandler;
        this.mapper = mapper;
    }

    /**
     * Creates a new body handler to convert a response body to desired value
     *
     * @param upstreamBodyHandler is handler of a response body
     * @param mapper is a function that converts response body to desired value
     * @param <S> is an original type of a response body
     * @param <T> is a type of a final value of the response body
     * @return a new {@link MappedBodyHandler}
     */
    public static <S, T> MappedBodyHandler<S, T> mapped(HttpResponse.BodyHandler<S> upstreamBodyHandler,
                                                 Function<S, T> mapper) {
        return new MappedBodyHandler<>(upstreamBodyHandler, mapper);
    }

    public static <T> MappedBodyHandler<String, T> json(Class<T> toReturn, GsonBuilder builder) {
        return mapped(ofString(), getFromJson(toReturn, builder));
    }

    public static <T> MappedBodyHandler<String, T> json(Class<T> toReturn) {
        return mapped(ofString(), getFromJson(toReturn));
    }

    public static MappedBodyHandler<String, org.w3c.dom.Document> document(DocumentBuilder documentBuilder) {
        return mapped(ofString(), getDocument(documentBuilder));
    }

    public static MappedBodyHandler<String, org.jsoup.nodes.Document> document() {
        return mapped(ofString(), getDocument());
    }

    public static <T> MappedBodyHandler<String, T> mappedByJackson(Class<T> toReturn, ObjectMapper mapper) {
        return mapped(ofString(), getMapped(toReturn, mapper));
    }

    public static <T> MappedBodyHandler<String, T> mappedByJackson(Class<T> toReturn) {
        return mapped(ofString(), getMapped(toReturn));
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return mapping(upstreamBodyHandler.apply(responseInfo), mapper);
    }
}
