package ru.tinkoff.qa.neptune.http.api.captors.request;

import ru.tinkoff.qa.neptune.core.api.event.firing.annotations.UseInjectors;
import ru.tinkoff.qa.neptune.core.api.event.firing.captors.CapturedFileInjector;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;
import ru.tinkoff.qa.neptune.http.api.request.body.FileBody;
import ru.tinkoff.qa.neptune.http.api.request.body.RequestBody;

import java.io.File;

import static java.util.List.of;

@UseInjectors(CapturedFileInjector.class)
@Description("Request body. File")
public final class FileRequestBodyCaptor extends AbstractRequestBodyCaptor<FileBody, File> {

    public FileRequestBodyCaptor() {
        super(of(FileBody.class));
    }

    @Override
    public File getData(FileBody caught) {
        return caught.body();
    }

    @Override
    FileBody convertTo(RequestBody<?> requestBody) {
        return (FileBody) requestBody;
    }
}
