package ru.tinkoff.qa.neptune.data.base.api;

import org.datanucleus.api.jdo.JDOPersistenceManager;
import ru.tinkoff.qa.neptune.core.api.cleaning.Stoppable;
import ru.tinkoff.qa.neptune.core.api.steps.context.Context;
import ru.tinkoff.qa.neptune.data.base.api.connection.data.DBConnection;
import ru.tinkoff.qa.neptune.data.base.api.connection.data.InnerJDOPersistenceManagerFactory;
import ru.tinkoff.qa.neptune.data.base.api.data.operations.UpdateExpression;
import ru.tinkoff.qa.neptune.data.base.api.queries.SelectASingle;
import ru.tinkoff.qa.neptune.data.base.api.queries.SelectList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.synchronizedSet;
import static java.util.List.of;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static ru.tinkoff.qa.neptune.data.base.api.data.operations.DataOperation.*;

public class DataBaseStepContext extends Context<DataBaseStepContext> implements Stoppable {

    private static final DataBaseStepContext context = getInstance(DataBaseStepContext.class);
    private final Set<JDOPersistenceManager> jdoPersistenceManagerSet = synchronizedSet(new HashSet<>());

    private static  <T> T returnSingleWhenNecessary(List<T> ts) {
        if (ts.size() == 0) {
            return null;
        }
        return ts.get(0);
    }

    public static DataBaseStepContext inDataBase() {
        return context;
    }

    public JDOPersistenceManager getManager(DBConnection connection) {
        checkArgument(nonNull(connection), "DB connection should not be null-value");
        var manager = jdoPersistenceManagerSet
                .stream()
                .filter(jdoPersistenceManager -> !jdoPersistenceManager.isClosed()
                        && ((InnerJDOPersistenceManagerFactory) jdoPersistenceManager.getPersistenceManagerFactory())
                        .getConnection() == connection)
                .findFirst()
                .orElse(null);

        return  ofNullable(manager).orElseGet(() -> {
            var newManager = (JDOPersistenceManager) connection.getPersistenceManager();
            jdoPersistenceManagerSet.add(newManager);
            return newManager;
        });
    }

    @Override
    public void stop() {
        jdoPersistenceManagerSet.forEach(JDOPersistenceManager::close);
    }

    public final <T, R extends List<T>> R select(SelectList<?, R, ?> selectList) {
        return selectList.get().apply(this);
    }

    public final <T> T select(SelectASingle<T, ?, ?> selectOne) {
        return selectOne.get().apply(this);
    }

    @SafeVarargs
    public final <T extends PersistableObject> T update(SelectASingle<T, ?, ?> howToSelect, UpdateExpression<T>... set) {
        return returnSingleWhenNecessary(updated(howToSelect, set).get().apply(this));
    }

    @SafeVarargs
    public final <T extends PersistableObject> List<T> update(SelectList<?, List<T>, ?> howToSelect, UpdateExpression<T>... set) {
        return updated(howToSelect, set).get().apply(this);
    }

    @SafeVarargs
    public final <T extends PersistableObject> List<T> update(Collection<T> toUpdate, UpdateExpression<T>... set) {
        return updated(toUpdate, set).get().apply(this);
    }

    @SafeVarargs
    public final <T extends PersistableObject> T update(T t, UpdateExpression<T>... set) {
        return returnSingleWhenNecessary(update(ofNullable(t).map(List::of).orElse(null), set));
    }

    public final <T extends PersistableObject> T delete(SelectASingle<T, ?, ?> howToSelect) {
        return returnSingleWhenNecessary(deleted(howToSelect).get().apply(this));
    }

    public final <T extends PersistableObject> List<T> delete(SelectList<T, List<T>, ?> howToSelect) {
        return deleted(howToSelect).get().apply(this);
    }

    public final <T extends PersistableObject> List<T> delete(Collection<T> toDelete) {
        return deleted(toDelete).get().apply(this);
    }

    @SafeVarargs
    public final <T extends PersistableObject> List<T> delete(T... toDelete) {
        return delete(ofNullable(toDelete).map(List::of).orElse(null));
    }

    public final <T extends PersistableObject> T delete(T toDelete) {
        checkArgument(nonNull(toDelete), "Object to be deleted should be defined as a value that differs from null");
        return returnSingleWhenNecessary(delete(of(toDelete)));
    }

    public final <T extends PersistableObject> List<T> insert(Collection<T> toInsert) {
        return inserted(toInsert).get().apply(this);
    }

    @SafeVarargs
    public final <T extends PersistableObject> List<T> insert(T... toInsert) {
        return insert(ofNullable(toInsert).map(List::of).orElse(null));
    }

    public final <T extends PersistableObject> T insert(T toInsert) {
        checkArgument(nonNull(toInsert), "Object to be inserted should be defined as a value that differs from null");
        return returnSingleWhenNecessary(insert(of(toInsert)));
    }
}
