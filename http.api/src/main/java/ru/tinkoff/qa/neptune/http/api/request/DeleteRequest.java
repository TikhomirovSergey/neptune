package ru.tinkoff.qa.neptune.http.api.request;

public final class DeleteRequest extends RequestBuilder {

    private DeleteRequest(String uri) {
        super(uri);
        builder.DELETE();
    }

    public static DeleteRequest DELETE(String uri) {
        return new DeleteRequest(uri);
    }
}
