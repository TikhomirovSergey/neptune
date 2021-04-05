package ru.tinkoff.qa.neptune.selenium.functions.elements;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import ru.tinkoff.qa.neptune.core.api.steps.Description;
import ru.tinkoff.qa.neptune.core.api.steps.DescriptionFragment;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.selenium.SeleniumStepContext;
import ru.tinkoff.qa.neptune.selenium.api.widget.HasSize;
import ru.tinkoff.qa.neptune.selenium.functions.searching.SearchSupplier;

import static ru.tinkoff.qa.neptune.selenium.SeleniumStepContext.CurrentContentFunction.currentContent;

public final class GetElementSizeSupplier extends SequentialGetStepSupplier
        .GetObjectChainedStepSupplier<SeleniumStepContext, Dimension, SearchContext, GetElementSizeSupplier> {

    private GetElementSizeSupplier() {
        super(s -> {
            var cls = s.getClass();

            if (WebElement.class.isAssignableFrom(cls)) {
                return ((WebElement) s).getSize();
            }

            if (HasSize.class.isAssignableFrom(cls)) {
                return ((HasSize) s).getSize();
            }

            throw new UnsupportedOperationException("It is not possible to get size of " + s.toString());
        });
    }

    /**
     * Builds a function that retrieves size of a web element|widget.
     *
     * @param supplier is how to get the web element/widget to get size from
     * @return Supplier of a function which gets size.
     */
    @Description("Size of the {element}")
    public static <T extends SearchContext> GetElementSizeSupplier elementSize(@DescriptionFragment("element") SearchSupplier<T> supplier) {
        return new GetElementSizeSupplier().from(supplier.get().compose(currentContent()));
    }

    /**
     * Builds a function that retrieves size of a web element/widget.
     *
     * @param context is the web element/widget to get size from
     * @return Supplier of a function which gets size.
     */
    @Description("Size of the {element}")
    public static GetElementSizeSupplier elementSize(@DescriptionFragment("element") SearchContext context) {
        return new GetElementSizeSupplier().from(context);
    }
}
