package ru.tinkoff.qa.neptune.data.base.api.queries;

import org.datanucleus.api.jdo.JDOPersistenceManager;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeFileCapturesOnFinishing;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeStringCapturesOnFinishing;
import ru.tinkoff.qa.neptune.core.api.steps.ConditionConcatenation;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.data.base.api.DataBaseStepContext;
import ru.tinkoff.qa.neptune.data.base.api.NothingIsSelectedException;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;
import ru.tinkoff.qa.neptune.data.base.api.connection.data.DBConnectionSupplier;
import ru.tinkoff.qa.neptune.data.base.api.queries.jdoql.JDOQLQueryParameters;
import ru.tinkoff.qa.neptune.data.base.api.queries.jdoql.ReadableJDOQuery;

import javax.jdo.query.PersistableExpression;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.tinkoff.qa.neptune.core.api.steps.StoryWriter.toGet;
import static ru.tinkoff.qa.neptune.data.base.api.properties.WaitingForQueryResultDuration.SLEEPING_TIME;
import static ru.tinkoff.qa.neptune.data.base.api.properties.WaitingForQueryResultDuration.WAITING_FOR_SELECTION_RESULT_TIME;
import static ru.tinkoff.qa.neptune.data.base.api.queries.JDOPersistenceManagerByConnectionSupplierClass.getConnectionBySupplierClass;
import static ru.tinkoff.qa.neptune.data.base.api.queries.JDOPersistenceManagerByPersistableClass.getConnectionByClass;
import static ru.tinkoff.qa.neptune.data.base.api.queries.ids.IdQuery.byIds;
import static ru.tinkoff.qa.neptune.data.base.api.queries.jdoql.JDOQLQuery.byJDOQLQuery;
import static ru.tinkoff.qa.neptune.data.base.api.queries.sql.SqlQuery.bySql;

@MakeFileCapturesOnFinishing
@MakeStringCapturesOnFinishing
public class SelectList<T, M> extends SequentialGetStepSupplier
        .GetIterableChainedStepSupplier<DataBaseStepContext, List<T>, M, T, SelectList<T, M>> {

    private SelectList(String description,
                       Function<M, List<T>> originalFunction) {
        super(description, originalFunction);
        timeOut(WAITING_FOR_SELECTION_RESULT_TIME.get());
        pollingInterval(SLEEPING_TIME.get());
    }

    public static <R extends PersistableObject, Q extends PersistableExpression<R>> SelectList<R, ReadableJDOQuery<R>> listOf(Class<R> toSelect,
                                                                                                                              JDOQLQueryParameters<R, Q> params) {
        return new SelectList<R, ReadableJDOQuery<R>>(format("List of %s by JDO typed query", toSelect.getName()),
                byJDOQLQuery()) {
            protected Function<ReadableJDOQuery<R>, List<R>> getEndFunction() {
                //such implementation is for advanced reporting
                return rReadableJDOQuery -> toGet(format("Result using JDO query %s",
                        rReadableJDOQuery.getInternalQuery()),
                        super.getEndFunction()).apply(rReadableJDOQuery);
            }
        }
                .from(getConnectionByClass(toSelect).andThen(manager ->
                        ofNullable(params)
                                .map(parameters -> parameters.buildQuery(new ReadableJDOQuery<>(manager, toSelect)))
                                .orElseGet(() -> new ReadableJDOQuery<>(manager, toSelect))));
    }

    public static <R extends PersistableObject> SelectList<R, ReadableJDOQuery<R>> listOf(Class<R> toSelect) {
        return listOf(toSelect, (JDOQLQueryParameters<R, PersistableExpression<R>>) null);
    }

    public static <R extends PersistableObject> SelectList<R, JDOPersistenceManager> listOf(Class<R> toSelect,
                                                                                            Object... ids) {
        return new SelectList<>(format("List of %s by ids [%s]",
                toSelect.getName(),
                Arrays.toString(ids)),
                byIds(toSelect, ids))
                .from(getConnectionByClass(toSelect));
    }

    public static <R extends PersistableObject> SelectList<R, JDOPersistenceManager> listOf(Class<R> toSelect,
                                                                                            String sql,
                                                                                            Object... parameters) {
        return new SelectList<>(format("List of %s by query '%s'. " +
                        "Parameters: %s",
                toSelect.getName(),
                sql,
                Arrays.toString(parameters)),
                bySql(toSelect, sql, parameters))
                .from(getConnectionByClass(toSelect));
    }

    public static <R extends DBConnectionSupplier> SelectList<List<Object>, JDOPersistenceManager> listOf(String sql,
                                                                                                          Class<R> connection,
                                                                                                          Object... parameters) {
        return new SelectList<>(format("List of rows by query %s. " +
                "The connection is described by %s. " +
                "Parameters: %s",
                sql,
                connection.getName(),
                Arrays.toString(parameters)),
                bySql(sql, parameters))
                .from(getConnectionBySupplierClass(connection));
    }

    @Override
    public SelectList<T, M> timeOut(Duration timeOut) {
        return super.timeOut(timeOut);
    }

    @Override
    public SelectList<T, M> pollingInterval(Duration pollingTime) {
        return super.pollingInterval(pollingTime);
    }

    @Override
    public SelectList<T, M> criteria(ConditionConcatenation concat, Predicate<? super T> condition) {
        return super.criteria(concat, condition);
    }

    @Override
    public SelectList<T, M> criteria(ConditionConcatenation concat, String conditionDescription, Predicate<? super T> condition) {
        return super.criteria(concat, conditionDescription, condition);
    }

    @Override
    public SelectList<T, M> criteria(Predicate<? super T> condition) {
        return super.criteria(condition);
    }

    @Override
    public SelectList<T, M> criteria(String conditionDescription, Predicate<? super T> condition) {
        return super.criteria(conditionDescription, condition);
    }

    public SelectList<T, M> throwWhenResultEmpty(String errorText) {
        checkArgument(isNotBlank(errorText), "Please define not blank exception text");
        return super.throwOnEmptyResult(() -> new NothingIsSelectedException(errorText));
    }
}