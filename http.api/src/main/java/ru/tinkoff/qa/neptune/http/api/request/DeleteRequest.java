package ru.tinkoff.qa.neptune.http.api.request;

/**
 * Designed to create a DELETE-request
 */
public final class DeleteRequest extends RequestBuilder {

    private DeleteRequest(String uri) {
        super(uri);
        builder.DELETE();
    }

    /**
     Creates an instance that builds a DELETE request.
     *
     * @param uri is a request end point
     * @return new {@link DeleteRequest}
     */
    public static DeleteRequest DELETE(String uri) {
        return new DeleteRequest(uri);
    }
}
