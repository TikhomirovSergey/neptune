package ru.tinkoff.qa.neptune.core.api.hamcrest.iterables.descriptions;

import org.hamcrest.Description;
import ru.tinkoff.qa.neptune.core.api.hamcrest.MismatchDescriber;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.DescriptionFragment;
import ru.tinkoff.qa.neptune.core.api.steps.parameters.ParameterValueGetter;

@ru.tinkoff.qa.neptune.core.api.steps.annotations.Description("Index: {index}. Item: {object}. {mismatch}")
public final class ItemMismatch extends MismatchDescriber {

    @DescriptionFragment("index")
    final int index;
    @DescriptionFragment("object")
    final Object object;
    @DescriptionFragment(value = "mismatch", makeReadableBy = ParameterValueGetter.TranslatedDescriptionParameterValueGetter.class)
    final Description mismatchDescription;

    public ItemMismatch(int index, Object object, Description mismatchDescription) {
        this.index = index;
        this.object = object;
        this.mismatchDescription = mismatchDescription;
    }
}