package com.github.toy.constructor.core.api.proxy;

import com.github.toy.constructor.core.api.GetStep;
import com.github.toy.constructor.core.api.PerformStep;
import com.github.toy.constructor.core.api.ToBeReported;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import org.objenesis.ObjenesisStd;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.*;

public final class Substitution {

    private static final Map<Class<?>, Class<?>> FOR_USED_SIMPLE_TYPES =
            Map.ofEntries(entry(Integer.class, int.class),
                    entry(Long.class, long.class),
                    entry(Boolean.class, boolean.class),
                    entry(Byte.class, byte.class),
                    entry(Short.class, short.class),
                    entry(Float.class, float.class),
                    entry(Double.class, double.class),
                    entry(Character.class, char.class));

    private Substitution() {
        super();
    }

    static <T> Constructor<T> findSuitableConstructor(Class<T> clazz, Object...params) throws Exception {
        List<Constructor<?>> constructorList = asList(clazz.getDeclaredConstructors());
        final List<Class<?>> paramTypes = Arrays.stream(params).map(o -> ofNullable(o)
                .map(Object::getClass)
                .orElse(null))
                .collect(toList());

        Constructor<?> foundConstructor = constructorList.stream().filter(constructor -> {
            List<Class<?>> constructorTypes = asList(constructor.getParameterTypes());
            return constructorTypes.size() == paramTypes.size() && matches(constructorTypes, paramTypes);
        })
                .findFirst().orElseThrow(() -> new NoSuchMethodException(
                        format("There is no constructor that convenient to parameter list %s", paramTypes)));
        foundConstructor.setAccessible(true);
        return (Constructor<T>) foundConstructor;
    }

    private static boolean matches(List<Class<?>> constructorTypes,
                                   List<Class<?>> paramTypes) {
        int i = -1;
        for (Class<?> parameter : constructorTypes) {
            i++;
            Class<?> currentType = paramTypes.get(i);
            if (currentType == null && FOR_USED_SIMPLE_TYPES.get(parameter) != null) {
                return false;
            }
            else if (currentType == null){
                continue;
            }

            if (parameter.isAssignableFrom(currentType)) {
                continue;
            }

            Class<?> simple;
            if ((simple = FOR_USED_SIMPLE_TYPES.get(currentType)) != null &&
                    parameter.isAssignableFrom(simple)) {
                continue;
            }

            Class<?> declaredArrayType = parameter.getComponentType();
            Class<?> currentArrayType = currentType.getComponentType();
            if (declaredArrayType != null && currentArrayType != null &&
                    declaredArrayType.isAssignableFrom(currentArrayType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static List<Logger> loadSPI(List<Logger> additional) {
        List<Logger> loggers = new ArrayList<>(ServiceLoader.load(Logger.class)
                .stream()
                .map(ServiceLoader.Provider::get).collect(toList()));

        List<? extends Class<? extends Logger>> loggerClasses =
                loggers.stream().map(Logger::getClass).collect(toList());

        loggers.addAll(additional
                .stream()
                .filter(logger -> !loggerClasses.contains(ofNullable(logger).map(Logger::getClass).orElse(null)))
                .collect(toList()));
        return loggers;
    }

    /**
     * This is the service method which generates a subclass
     * of the given implementor of {@link com.github.toy.constructor.core.api.GetStep} and/or
     * {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param clazz to substitute. It should be the implementor of {@link com.github.toy.constructor.core.api.GetStep}
     *                    and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param constructorParameters is a POJO with wrapped parameters of required constructor.
     * @param loggers list of custom loggers. see {@link Logger}
     * @param annotations to set to methods that marked by {@link com.github.toy.constructor.core.api.ToBeReported}.
     *                    These annotations should describe steps. Their description should be like {@value {0}} or
     *                    some string convenient to the formatting with a single parameter.
     * @return generated sub-class.
     */
    private static <T> Class<? extends T> substitute(Class<T> clazz,
                                                    ConstructorParameters constructorParameters,
                                                    List<Logger> loggers,
                                                    Annotation...annotations) throws Exception {
        checkArgument(PerformStep.class.isAssignableFrom(clazz) ||
                GetStep.class.isAssignableFrom(clazz), "Class to substitute should be " +
                "assignable from com.github.toy.constructor.core.api.GetStep and/or " +
                "com.github.toy.constructor.core.api.PerformStep.");
        checkArgument(findSuitableConstructor(clazz, constructorParameters.getParameterValues()) != null);

        DynamicType.Builder<? extends T> builder = new ByteBuddy().subclass(clazz);

        InnerInterceptor<T> interceptor = new InnerInterceptor<>(clazz, constructorParameters, loadSPI(loggers));
        return builder.method(isAnnotatedWith(ToBeReported.class))
                .intercept(to(interceptor))
                .annotateMethod(annotations)
                .method(not(isAnnotatedWith(ToBeReported.class)))
                .intercept(to(interceptor))
                .make()
                .load(Substitution.class.getClassLoader())
                .getLoaded();
    }

    /**
     * This is the service method which creates an instance of the given implementor of
     * {@link com.github.toy.constructor.core.api.GetStep} and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param clazz to substitute. It should be the implementor of {@link com.github.toy.constructor.core.api.GetStep}
     *                    and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param constructorParameters is a POJO with wrapped parameters of required constructor.
     * @param manipulationWithClassToInstantiate is a function which transforms class to be instantiated, e.g bytecode
     *                                            operations by CGLIB or Byte Buddy etc.
     * @param manipulationWithObjectToReturn is a function which transforms created object, e.g creating proxy,
     *                                        changing some attributes etc.
     * @param loggers list of custom loggers. see {@link Logger}
     * @param annotations to set to methods that marked by {@link com.github.toy.constructor.core.api.ToBeReported}.
     *                    These annotations should describe steps. Their description should be like {@value {0}} or
     *                    some string convenient to the formatting with a single parameter.
     * @param <T> type of the implementor of {@link com.github.toy.constructor.core.api.GetStep} and/or
     * {@link com.github.toy.constructor.core.api.PerformStep}.
     * @return an instance.
     */
    public static <T> T getSubstituted(Class<T> clazz,
                                       ConstructorParameters constructorParameters,
                                       Function<Class<? extends T>, Class<? extends T>> manipulationWithClassToInstantiate,
                                       Function<T, T> manipulationWithObjectToReturn,
                                       List<Logger> loggers,
                                       Annotation...annotations) throws Exception {
        Class<? extends T> toInstantiate =
                manipulationWithClassToInstantiate.apply(substitute(clazz, constructorParameters, loggers, annotations));
        return manipulationWithObjectToReturn.apply(new ObjenesisStd().newInstance(toInstantiate));
    }

    /**
     * This is the service method which creates an instance of the given implementor of
     * {@link com.github.toy.constructor.core.api.GetStep} and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param clazz to substitute. It should be the implementor of {@link com.github.toy.constructor.core.api.GetStep}
     *                    and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param constructorParameters is a POJO with wrapped parameters of required constructor.
     * @param loggers list of custom loggers. see {@link Logger}
     * @param annotations to set to methods that marked by {@link com.github.toy.constructor.core.api.ToBeReported}.
     *                    These annotations should describe steps. Their description should be like {@value {0}} or
     *                    some string convenient to the formatting with a single parameter.
     * @param <T> type of the implementor of {@link com.github.toy.constructor.core.api.GetStep} and/or
     * {@link com.github.toy.constructor.core.api.PerformStep}.
     * @return an instance.
     */
    public static <T> T getSubstituted(Class<T> clazz,
                                       ConstructorParameters constructorParameters,
                                       List<Logger> loggers,
                                       Annotation...annotations) throws Exception {
        return getSubstituted(clazz, constructorParameters, aClass -> aClass, t -> t, loggers, annotations);
    }


    /**
     * This is the service method which creates an instance of the given implementor of
     * {@link com.github.toy.constructor.core.api.GetStep} and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param clazz to substitute. It should be the implementor of {@link com.github.toy.constructor.core.api.GetStep}
     *                    and/or {@link com.github.toy.constructor.core.api.PerformStep}.
     *
     * @param constructorParameters is a POJO with wrapped parameters of required constructor.
     * @param annotations to set to methods that marked by {@link com.github.toy.constructor.core.api.ToBeReported}.
     *                    These annotations should describe steps. Their description should be like {@value {0}} or
     *                    some string convenient to the formatting with a single parameter.
     * @param <T> type of the implementor of {@link com.github.toy.constructor.core.api.GetStep} and/or
     * {@link com.github.toy.constructor.core.api.PerformStep}.
     * @return an instance.
     */
    public static <T> T getSubstituted(Class<T> clazz,
                                       ConstructorParameters constructorParameters,
                                       Annotation...annotations) throws Exception {
        return getSubstituted(clazz, constructorParameters, List.of(), annotations);
    }
}