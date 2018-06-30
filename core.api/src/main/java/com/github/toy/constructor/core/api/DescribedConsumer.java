package com.github.toy.constructor.core.api;

import java.util.function.Consumer;

import static com.github.toy.constructor.core.api.StaticEventFiring.*;
import static com.github.toy.constructor.core.api.properties.DoCapturesOf.catchFailureEvent;
import static com.github.toy.constructor.core.api.properties.DoCapturesOf.catchSuccessEvent;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressWarnings("unchecked")
class DescribedConsumer<T> implements Consumer<T> {

    private final String description;
    private final Consumer<T> consumer;
    private boolean isComplex;

    DescribedConsumer(String description, Consumer<T> consumer) {
        checkArgument(consumer != null, "Consumer should be defined");
        checkArgument(!isBlank(description), "Description should not be empty");
        this.description = description;
        this.consumer = consumer;
    }

    private static <T> DescribedConsumer<T> getSequentialDescribedConsumer(Consumer<? super T> before,
                                                                  Consumer<? super T> after) {
        checkNotNull(before);
        checkNotNull(after);
        checkArgument(DescribedConsumer.class.isAssignableFrom(before.getClass()),
                "It seems given consumer doesn't describe any before-action. Use method " +
                        "StoryWriter.action to describe the after-action.");
        checkArgument(DescribedConsumer.class.isAssignableFrom(after.getClass()),
                "It seems given consumer doesn't describe any after-action. Use method " +
                        "StoryWriter.action to describe the after-action.");

        return new DescribedConsumer<T>(after.toString(), t -> {
            before.accept(t); after.accept(t);
        }).setComplex();
    }

    @Override
    public void accept(T t) {
        try {
            fireEventStarting(format("Perform %s on %s", description, t));
            consumer.accept(t);
            if (catchSuccessEvent() && !isComplex) {
                catchResult(t, format("Performing of '%s' succeed", description));
            }
        }
        catch (Throwable thrown) {
            fireThrownException(thrown);
            if (catchFailureEvent() && !isComplex) {
                catchResult(t, format("Performing of '%s' failed", description));
            }
            throw thrown;
        }
        finally {
            fireEventFinishing();
        }
    }

    @Override
    public String toString() {
        return description;
    }

    public Consumer<T> andThen(Consumer<? super T> afterAction)  {
        return getSequentialDescribedConsumer(this, afterAction);
    }

    private DescribedConsumer<T> setComplex() {
        isComplex = true;
        return this;
    }
}
