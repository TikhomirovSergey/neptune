package ru.tinkoff.qa.neptune.data.base.api.query;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import ru.tinkoff.qa.neptune.core.api.StoryWriter;
import ru.tinkoff.qa.neptune.data.base.api.DataBaseSteps;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;

import javax.jdo.JDOQLTypedQuery;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ru.tinkoff.qa.neptune.data.base.api.query.ChangePersistenceManagerByNameFunction.changeConnectionByName;
import static ru.tinkoff.qa.neptune.data.base.api.query.ChangePersistenceManagerByPersistenceManagerFactory.changeConnectionByersistenceManagerFactory;
import static ru.tinkoff.qa.neptune.data.base.api.query.ChangePersistenceManagerToDefault.changeConnectionToDefault;

@SuppressWarnings("unchecked")
public abstract class ByQuerySequentialGetStepSupplier<T extends PersistableObject, S, Q extends ByQuerySequentialGetStepSupplier<T, S, Q>>
        extends SelectSequentialGetStepSupplier<S, JDOQLTypedQuery<T>, Q> {

    private final QueryBuilderFunction<T> queryBuilder;
    Predicate<T> condition;

    ByQuerySequentialGetStepSupplier(QueryBuilderFunction<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    /**
     * Sometimes the performing of a query can take o lot of time. The better solution is to create lighter query
     * and filter result by some condition. It is necessary to describe given condition by {@link StoryWriter#condition(String, Predicate)}.
     *
     * @param condition is a predicate to filter the selection result.
     * @return self-reference
     */
    public Q withCondition(Predicate<T> condition) {
        checkArgument(condition != null, "Condition should be defined");
        this.condition = condition;
        return (Q) this;
    }

    @Override
    public Function<DataBaseSteps, S> get() {
        if (toUseDefaultConnection) {
            super.from(queryBuilder);
            return super.get().compose(changeConnectionToDefault());
        }

        return ofNullable(connectionDescription).map(o -> {
            Class<?> objectClass = o.getClass();
            if (String.class.equals(objectClass)) {
                super.from(queryBuilder.compose(changeConnectionByName(String.valueOf(o))));
                return super.get();
            }

            if (JDOPersistenceManagerFactory.class.isAssignableFrom(objectClass)) {
                super.from(queryBuilder.compose(changeConnectionByersistenceManagerFactory((JDOPersistenceManagerFactory) o)));
                return super.get();
            }

            throw new IllegalArgumentException(format("Unknown description of a connection of type %s",
                    objectClass.getName()));
        }).orElseGet(() -> {
            super.from(queryBuilder);
            return super.get();
        });
    }
}
