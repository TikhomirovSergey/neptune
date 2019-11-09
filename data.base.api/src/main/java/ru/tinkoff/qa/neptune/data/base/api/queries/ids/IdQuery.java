package ru.tinkoff.qa.neptune.data.base.api.queries.ids;

import org.apache.commons.lang3.StringUtils;
import org.datanucleus.api.jdo.JDOPersistenceManager;
import ru.tinkoff.qa.neptune.data.base.api.IdSetter;
import ru.tinkoff.qa.neptune.data.base.api.ListOfDataBaseObjects;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * This class constructs a query to select stored objects by known ids.
 * @param <T> is a type of {@link PersistableObject} to be selected
 */
public final class IdQuery<T extends PersistableObject> implements Function<JDOPersistenceManager, List<T>>, IdSetter {

    private final Class<T> classOfRequestedValue;
    private final Object[] ids;

    private IdQuery(Class<T> classOfRequestedValue, Object[] ids) {
        checkNotNull(classOfRequestedValue, "A class of selected objects should be defined");
        checkNotNull(ids, "Ids should be defined as a value that differs from null");
        checkArgument(ids.length > 0, "At least one object Id should be defined");
        this.classOfRequestedValue = classOfRequestedValue;
        this.ids = ids;
    }

    /**
     * Creates an instance that performs a query to select stored objects by known ids
     *
     * @param classOfRequestedValue is a class of {@link PersistableObject} to be selected
     * @param ids is an array of ids used to select desired objects
     * @param <T> is a type of {@link PersistableObject} to be selected
     * @return new {@link IdQuery}
     */
    public static <T extends PersistableObject> IdQuery<T> byIds(Class<T> classOfRequestedValue, Object... ids) {
        return new IdQuery<>(classOfRequestedValue, ids);
    }

    @Override
    public List<T> apply(JDOPersistenceManager jdoPersistenceManager) {
        var found = new ListOfDataBaseObjects<T>() {
            public String toString() {
                var resultStr =  format("%s objects/object selected by ids %s", size(), Arrays.toString(ids));

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

        var list = new ArrayList<T>();
        stream(ids).forEach(id -> {
            try {
                var p = jdoPersistenceManager.getObjectById(classOfRequestedValue, id);
                list.add(p);
            }
            catch (RuntimeException ignored) {
            }
        });

        found.addAll(jdoPersistenceManager.detachCopyAll(list));
        setRealIds(list, found);

        return found;
    }
}
