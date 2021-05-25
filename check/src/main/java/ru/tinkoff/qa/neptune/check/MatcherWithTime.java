package ru.tinkoff.qa.neptune.check;

import ru.tinkoff.qa.neptune.core.api.hamcrest.MismatchDescriber;
import ru.tinkoff.qa.neptune.core.api.hamcrest.NeptuneFeatureMatcher;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;

abstract class MatcherWithTime<T> extends NeptuneFeatureMatcher<T> {

    private Duration waitForMatch;

    protected boolean prerequisiteChecking(Object actual) {
        return true;
    }

    protected boolean checkFeature(Object actual) {
        long millis = ofNullable(waitForMatch)
                .map(Duration::toMillis)
                .orElse(0L);

        var startTime = currentTimeMillis();
        do {
            if (super.checkFeature(actual)) {
                return true;
            }

        } while (currentTimeMillis() < startTime + millis);

        return false;
    }

    MatcherWithTime<T> waitForMatch(Duration waitForMatch) {
        checkNotNull(waitForMatch);
        checkArgument(!waitForMatch.isNegative(), "Time value should not be negative");
        this.waitForMatch = waitForMatch;
        return this;
    }

    @Override
    protected final void appendMismatchDescription(MismatchDescriber describer) {
        super.appendMismatchDescription(ofNullable(waitForMatch)
                .map(d -> (MismatchDescriber) new MismatchDescriberWithTime(d, describer))
                .orElse(describer));
    }
}
