package com.github.toy.constructor.allure;

import com.github.toy.constructor.core.api.captors.CapturedFileInjector;
import io.qameta.allure.AllureResultsWriteException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.google.common.io.Files.getFileExtension;
import static io.qameta.allure.Allure.addAttachment;

public class AllureFileInjector implements CapturedFileInjector {

    @Override
    public void inject(File toBeInjected, String message) {
        try {
            addAttachment(message, null, new FileInputStream(toBeInjected),
                    getFileExtension(toBeInjected.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            throw new AllureResultsWriteException(e.getMessage(), e);
        }
    }
}
