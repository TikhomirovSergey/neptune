package ru.tinkoff.qa.neptune.check.test;

import ru.tinkoff.qa.neptune.core.api.event.firing.Captor;
import ru.tinkoff.qa.neptune.core.api.steps.Description;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Description("Value")
public class DefaultListLogger extends Captor<Object, String> {

    static final List<String> messages = new ArrayList<>();

    public DefaultListLogger() {
        super(List.of((toBeInjected, message) -> {
            var toBeLogged = format("%s %s", message, toBeInjected);
            System.out.println(toBeLogged);
            messages.add(toBeLogged);
        }));
    }

    @Override
    public String getData(Object caught) {
        return caught.toString();
    }

    @Override
    public Object getCaptured(Object toBeCaptured) {
        return toBeCaptured;
    }
}
