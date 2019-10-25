package ru.tinkoff.qa.neptune.data.base.api.data.operations;

import org.apache.commons.lang3.StringUtils;
import org.datanucleus.api.jdo.JDOPersistenceManager;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeFileCapturesOnFinishing;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeStringCapturesOnFinishing;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.data.base.api.DataBaseStepContext;
import ru.tinkoff.qa.neptune.data.base.api.ListOfDataBaseObjects;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;
import ru.tinkoff.qa.neptune.data.base.api.queries.SelectASingle;
import ru.tinkoff.qa.neptune.data.base.api.queries.SelectList;

import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javax.jdo.JDOHelper.isDeleted;
import static javax.jdo.JDOHelper.isPersistent;
import static ru.tinkoff.qa.neptune.data.base.api.ConnectionToUse.ConnectionDataReader.getConnection;

@SuppressWarnings("unchecked")
@MakeFileCapturesOnFinishing
@MakeStringCapturesOnFinishing
public final class DataOperation<T extends PersistableObject>  extends SequentialGetStepSupplier
        .GetIterableChainedStepSupplier<DataBaseStepContext, List<T>, Map<JDOPersistenceManager, List<T>>, T, DataOperation<T>> {

    private DataOperation(String description, Function<Map<JDOPersistenceManager, List<T>>, List<T>> originalFunction) {
        super(description, originalFunction);
    }

    public static <T extends PersistableObject> DataOperation<T> updated(SelectASingle<T, ?> howToSelect, UpdateExpression<T> set) {
        checkArgument(nonNull(howToSelect), "Please define how to select an object to be updated");
        checkArgument(nonNull(set), "Please define update-actions");
        return new DataOperation<T>(format("Updated %s", howToSelect),
                jdoPersistenceManagerListMap -> update(jdoPersistenceManagerListMap, set))
                .from(context -> {
                    var result = context.select(howToSelect);
                    var list = ofNullable(result).map(List::of).orElseGet(List::of);
                    return getMap(context, list);
                });
    }

    public static <T extends PersistableObject> DataOperation<T> updated(SelectList<T, ?> howToSelect, UpdateExpression<T> set) {
        checkArgument(nonNull(howToSelect), "Please define how to select objects to be updated");
        checkArgument(nonNull(set), "Please define update-actions");
        return new DataOperation<T>(format("Updated %s", howToSelect),
                jdoPersistenceManagerListMap -> update(jdoPersistenceManagerListMap, set))
                .from(context -> getMap(context, context.select(howToSelect)));
    }

    public static <T extends PersistableObject> DataOperation<T> updated(Collection<T> toBeUpdated, UpdateExpression<T> set) {
        checkArgument(nonNull(toBeUpdated),
                "Collection of objects to be updated should be defined as a value that differs from null");
        checkArgument(toBeUpdated.size() > 0,
                "Should be defined at least one object to update it");
        checkArgument(nonNull(set),
                "Please define update-actions");

        checkArgument(toBeUpdated
                .stream()
                .noneMatch(Objects::isNull),
                format("There are null-values are being passed for the updating. The collection: %s", toBeUpdated));

        checkArgument(toBeUpdated
                .stream()
                .noneMatch(t -> !isPersistent(t) || isDeleted(t)),
                "There are objects that not stored in DB yet");

        return new DataOperation<T>(format("Updated %s object/objects from table/tables %s",
                toBeUpdated.size(),
                toBeUpdated.stream()
                        .map(PersistableObject::fromTable)
                        .distinct()
                        .collect(toList())),
                jdoPersistenceManagerListMap -> update(jdoPersistenceManagerListMap, set))
                .from(context -> getMap(context, toBeUpdated));
    }

    public static <T extends PersistableObject> DataOperation<T> updated(T toBeUpdated, UpdateExpression<T> set) {
        var updateCollection = ofNullable(toBeUpdated).map(List::of).orElse(null);
        return updated(updateCollection, set);
    }

    public static <T extends PersistableObject> DataOperation<T> deleted(SelectASingle<T, ?> howToSelect) {
        checkArgument(nonNull(howToSelect), "Please define how to select an object to be deleted");
        return new DataOperation<T>(format("Deleted %s", howToSelect),
                DataOperation::delete)
                .from(context -> {
                    var result = context.select(howToSelect);
                    var list = ofNullable(result).map(List::of).orElseGet(List::of);
                    return getMap(context, list);
                });
    }

    public static <T extends PersistableObject> DataOperation<T> deleted(SelectList<T, ?> howToSelect) {
        checkArgument(nonNull(howToSelect), "Please define how to select objects to be deleted");
        return new DataOperation<T>(format("Deleted %s", howToSelect),
                DataOperation::delete)
                .from(context -> getMap(context, context.select(howToSelect)));
    }

    public static <T extends PersistableObject> DataOperation<T> deleted(Collection<T> toBeDeleted) {
        checkArgument(nonNull(toBeDeleted),
                "Collection of objects to be deleted should be defined as a value that differs from null");

        var toDelete = toBeDeleted
                .stream()
                .filter(t -> nonNull(t) && isPersistent(t) && !isDeleted(t))
                .collect(toList());

        return new DataOperation<T>(format("Deleted %s object/objects from table/tables %s",
                toDelete.size(),
                toDelete.stream()
                        .map(PersistableObject::fromTable)
                        .distinct()
                        .collect(toList())),
                DataOperation::delete)
                .from(context -> getMap(context, toDelete));
    }

    public static <T extends PersistableObject> DataOperation<T> deleted(T... toBeDeleted) {
        var deleteCollection = ofNullable(toBeDeleted).map(List::of).orElse(null);
        return deleted(deleteCollection);
    }

    public static <T extends PersistableObject> DataOperation<T> inserted(Collection<T> toBeInserted) {
        checkArgument(nonNull(toBeInserted),
                "Collection of objects to be inserted should be defined as a value that differs from null");

        checkArgument(toBeInserted.size() > 0,
                "Should be defined at least one object to insert it");

        checkArgument(toBeInserted
                        .stream()
                        .noneMatch(Objects::isNull),
                format("There are null-values are being passed for the inserting. The collection: %s", toBeInserted));

        checkArgument(toBeInserted
                        .stream()
                        .noneMatch(t -> isPersistent(t) && !isDeleted(t)),
                "There are objects that stored in DB already");

        var toInsert = toBeInserted
                .stream()
                .map(t -> {
                    if (!isPersistent(t)) {
                        return t;
                    }//persistent but deleted
                    return (T) t.clone();
                }).collect(toList());

        return new DataOperation<T>(format("Inserted %s object/objects",
                toInsert.size()),
                DataOperation::insert)
                .from(context -> getMap(context, toInsert));
    }

    public static <T extends PersistableObject> DataOperation<T> inserted(T... toBeInserted) {
        var insertCollection = ofNullable(toBeInserted).map(List::of).orElse(null);
        return inserted(insertCollection);
    }

    private static <T extends PersistableObject> List<T> update(Map<JDOPersistenceManager, List<T>> connectionMap, UpdateExpression<T> set) {
        var managerSet = connectionMap.keySet();
        openTransaction(managerSet);

        try {
            var result = new ListOfDataBaseObjects<T>() {
                public String toString() {
                    var resultStr = format("%s updated object/objects", size());
                    var tableList = stream().map(PersistableObject::fromTable)
                            .filter(StringUtils::isNotBlank)
                            .distinct()
                            .collect(toList());

                    if (tableList.size() > 0) {
                        resultStr = format("%s of table/tables %s", resultStr, join(",", tableList));
                    }
                    return resultStr;
                }
            };

            var toBeUpdated = new ArrayList<T>();
            connectionMap.forEach((ignored, value) -> toBeUpdated.addAll(value));
            set.getUpdateAction().forEach(setAction -> setAction.accept(toBeUpdated));
            result.addAll(toBeUpdated);
            commitTransaction(managerSet);

            return result;
        }
        catch (Throwable t) {
            rollbackTransaction(managerSet);
            throw t;
        }
    }

    private static <T extends PersistableObject> List<T> insert(Map<JDOPersistenceManager, List<T>> connectionMap) {
        var managerSet = connectionMap.keySet();
        openTransaction(managerSet);

        try {
            var result = new ListOfDataBaseObjects<T>() {
                public String toString() {
                    var resultStr = format("%s inserted object/objects", size());
                    var tableList = stream().map(PersistableObject::fromTable)
                            .filter(StringUtils::isNotBlank)
                            .distinct()
                            .collect(toList());

                    if (tableList.size() > 0) {
                        resultStr = format("%s of table/tables %s", resultStr, join(",", tableList));
                    }
                    return resultStr;
                }
            };

            connectionMap.forEach((manager, toBeInserted) ->
                    result.addAll(manager.makePersistentAll(toBeInserted)));
            commitTransaction(managerSet);
            return result;
        }
        catch (Throwable t) {
            rollbackTransaction(managerSet);
            throw t;
        }
    }

    private static <T extends PersistableObject> List<T> delete(Map<JDOPersistenceManager, List<T>> connectionMap) {
        var managerSet = connectionMap.keySet();
        openTransaction(managerSet);

        try {
            var result = new ListOfDataBaseObjects<T>() {
                public String toString() {
                    return format("%s deleted object/objects", size());
                }
            };

            connectionMap.forEach((manager, ts) -> {
                manager.deletePersistentAll(ts);
                ts.forEach(o -> result.add((T) o.clone()));
            });
            commitTransaction(managerSet);
            return result;
        }
        catch (Throwable t) {
            rollbackTransaction(managerSet);
            throw t;
        }
    }

    private static <T extends PersistableObject> Map<JDOPersistenceManager, List<T>> getMap(DataBaseStepContext context,
                                                                                            Collection<T> toBeOperated) {
        var result = new LinkedHashMap<JDOPersistenceManager, List<T>>();

        toBeOperated.forEach(t -> {
            var manager = context.getManager(getConnection(t.getClass()));
            ofNullable(result.get(manager))
                    .ifPresentOrElse(ts -> ts.add(t),
                            () -> result.put(manager, new ArrayList<>(List.of(t))));
        });
        return result;
    }

    private static void openTransaction(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager -> jdoPersistenceManager
                .currentTransaction()
                .begin());
    }

    private static void commitTransaction(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager ->
                jdoPersistenceManager
                        .currentTransaction()
                        .commit());
    }

    private static void rollbackTransaction(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager -> {
            var transaction = jdoPersistenceManager.currentTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
        });
    }
}