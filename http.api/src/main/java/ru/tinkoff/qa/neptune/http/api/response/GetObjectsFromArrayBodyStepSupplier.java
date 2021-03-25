package ru.tinkoff.qa.neptune.http.api.response;

import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.CaptorFilterByProducedType;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeCaptureOnFinishing;
import ru.tinkoff.qa.neptune.core.api.steps.Criteria;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.core.api.steps.parameters.StepParameter;
import ru.tinkoff.qa.neptune.http.api.HttpStepContext;
import ru.tinkoff.qa.neptune.http.api.request.RequestBuilder;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Set.of;
import static ru.tinkoff.qa.neptune.core.api.event.firing.StaticEventFiring.catchValue;
import static ru.tinkoff.qa.neptune.core.api.properties.general.events.DoCapturesOf.catchFailureEvent;
import static ru.tinkoff.qa.neptune.core.api.properties.general.events.DoCapturesOf.catchSuccessEvent;
import static ru.tinkoff.qa.neptune.core.api.steps.localization.StepLocalization.translate;
import static ru.tinkoff.qa.neptune.http.api.response.ResponseSequentialGetSupplier.response;

/**
 * Builds a step-function that retrieves an array from http response body.
 *
 * @param <T> is a type of a response body
 * @param <R> is a type of an item of resulted array
 */
@SequentialGetStepSupplier.DefaultParameterNames(
        criteria = "Criteria for an item of resulted array",
        timeOut = "Time to receive expected http response and get not empty array"
)
@MakeCaptureOnFinishing(typeOfCapture = Object.class)
public abstract class GetObjectsFromArrayBodyStepSupplier<T, R, S extends GetObjectsFromArrayBodyStepSupplier<T, R, S>>
        extends SequentialGetStepSupplier.GetArrayStepSupplier<HttpStepContext, R, S> {

    private GetObjectsFromArrayBodyStepSupplier(Function<HttpStepContext, R[]> f) {
        super(f);
    }

    /**
     * Creates an instance of {@link GetObjectsFromArrayWhenResponseReceived}. It builds a step-function that retrieves
     * an array from http response body.
     *
     * @param description is a description of resulted array
     * @param received    is a received http response
     * @param f           is a function that describes how to get an array from a body of http response
     * @param <T>         is a type of a response body
     * @param <R>         is a type of an item of resulted array
     * @return an instance of {@link GetObjectsFromArrayWhenResponseReceived}
     */
    public static <T, R> GetObjectsFromArrayWhenResponseReceived<T, R> asArray(String description,
                                                                               HttpResponse<T> received,
                                                                               Function<T, R[]> f) {
        return new GetObjectsFromArrayWhenResponseReceived<>(received, f).setDescription(translate(description));
    }

    /**
     * Creates an instance of {@link GetObjectsFromArrayWhenResponseReceiving}. It builds a step-function that retrieves
     * an array from http response body.
     *
     * @param description    is a description of resulted array
     * @param requestBuilder describes a request to be sent
     * @param handler        is a response body handler
     * @param f              is a function that describes how to get an array from a body of http response
     * @param <T>            is a type of a response body
     * @param <R>            is a type of an item of resulted array
     * @return an instance of {@link GetObjectsFromArrayWhenResponseReceiving}
     */
    public static <T, R> GetObjectsFromArrayWhenResponseReceiving<T, R> asArray(String description,
                                                                                RequestBuilder requestBuilder,
                                                                                HttpResponse.BodyHandler<T> handler,
                                                                                Function<T, R[]> f) {
        return new GetObjectsFromArrayWhenResponseReceiving<>(response(requestBuilder, handler), f).setDescription(translate(description));
    }


    /**
     * Creates an instance of {@link GetObjectsFromArrayWhenResponseReceived}. It builds a step-function that retrieves
     * an array from http response body.
     *
     * @param description is a description of resulted array
     * @param received    is a received http response
     * @param <R>         is a type of an item of array of response body
     * @return an instance of {@link GetObjectsFromArrayWhenResponseReceived}
     */
    public static <R> GetObjectsFromArrayWhenResponseReceived<R[], R> asArray(String description,
                                                                              HttpResponse<R[]> received) {
        return new GetObjectsFromArrayWhenResponseReceived<>(received, rs -> rs).setDescription(translate(description));
    }

    /**
     * Creates an instance of {@link GetObjectsFromArrayWhenResponseReceiving}. It builds a step-function that retrieves
     * an array from http response body.
     *
     * @param description    is a description of resulted array
     * @param requestBuilder describes a request to be sent
     * @param handler        is a response body handler
     * @param <R>            is a type of an item of array of response body
     * @return an instance of {@link GetObjectsFromArrayWhenResponseReceiving}
     */
    public static <R> GetObjectsFromArrayWhenResponseReceiving<R[], R> asArray(String description,
                                                                               RequestBuilder requestBuilder,
                                                                               HttpResponse.BodyHandler<R[]> handler) {
        return new GetObjectsFromArrayWhenResponseReceiving<>(response(requestBuilder, handler), rs -> rs).setDescription(translate(description));
    }


    @Override
    public S criteria(Criteria<? super R> criteria) {
        return super.criteria(criteria);
    }

    @Override
    public S criteria(String description, Predicate<? super R> predicate) {
        return super.criteria(description, predicate);
    }

    /**
     * Make throw an exception if no data received
     *
     * @param exceptionMessage is a message of {@link DesiredDataHasNotBeenReceivedException} exception to be thrown
     * @return self-reference
     * @see SequentialGetStepSupplier#throwOnEmptyResult(Supplier)
     */
    public S throwIfNoDesiredDataReceived(String exceptionMessage) {
        return super.throwOnEmptyResult(() -> new DesiredDataHasNotBeenReceivedException(exceptionMessage));
    }

    /**
     * Returns an array from a body of http response which is received already.
     *
     * @param <T> is a type of a response body
     * @param <R> is a type of an item of resulted array
     */
    @SuppressWarnings("unused")
    public static final class GetObjectsFromArrayWhenResponseReceived<T, R>
            extends GetObjectsFromArrayBodyStepSupplier<T, R, GetObjectsFromArrayWhenResponseReceived<T, R>> {

        @StepParameter("From body of received http response")
        final HttpResponse<T> response;

        private GetObjectsFromArrayWhenResponseReceived(HttpResponse<T> response,
                                                        Function<T, R[]> f) {
            super(f.compose(ignored -> response.body()));
            checkNotNull(response);
            this.response = response;
        }

        @Override
        protected GetObjectsFromArrayWhenResponseReceived<T, R> setDescription(String description) {
            return super.setDescription(description);
        }
    }

    /**
     * Returns an array from a body of http response. Response is supposed to be received during the step execution
     *
     * @param <T> is a type of a response body
     * @param <R> is a type of an item of resulted array
     */
    public static final class GetObjectsFromArrayWhenResponseReceiving<T, R>
            extends GetObjectsFromArrayBodyStepSupplier<T, R, GetObjectsFromArrayWhenResponseReceiving<T, R>> {

        private final ResponseExecutionInfo info;
        private final ResponseSequentialGetSupplier<T> getResponse;

        private GetObjectsFromArrayWhenResponseReceiving(ReceiveResponseAndGetResultFunction<T, R[]> f) {
            super(f);
            var s = f.getGetResponseSupplier();
            info = s.getInfo();
            getResponse = s;
        }

        private GetObjectsFromArrayWhenResponseReceiving(ResponseSequentialGetSupplier<T> getResponse,
                                                         Function<T, R[]> f) {
            this(new ReceiveResponseAndGetResultFunction<>(f, getResponse));
        }

        @Override
        protected GetObjectsFromArrayWhenResponseReceiving<T, R> setDescription(String description) {
            return super.setDescription(description);
        }

        /**
         * Defines time to receive a response and get desired data.
         *
         * @param timeOut is a time duration to receive a response and get desired data
         * @return self-reference
         * @see SequentialGetStepSupplier#timeOut(Duration)
         */
        public GetObjectsFromArrayWhenResponseReceiving<T, R> retryTimeOut(Duration timeOut) {
            return super.timeOut(timeOut);
        }

        @Override
        public GetObjectsFromArrayWhenResponseReceiving<T, R> pollingInterval(Duration pollingTime) {
            return super.pollingInterval(pollingTime);
        }

        /**
         * Defines criteria for expected http response.
         *
         * @param description criteria description
         * @param predicate   is how to match http response
         * @return self-reference
         * @see SequentialGetStepSupplier#criteria(String, Predicate)
         */
        public GetObjectsFromArrayWhenResponseReceiving<T, R> responseCriteria(String description, Predicate<HttpResponse<T>> predicate) {
            getResponse.criteria(description, predicate);
            return this;
        }

        /**
         * Defines criteria for expected http response.
         *
         * @param criteria describes how to match http response
         * @return self-reference
         * @see SequentialGetStepSupplier#criteria(Criteria)
         */
        public GetObjectsFromArrayWhenResponseReceiving<T, R> responseCriteria(Criteria<HttpResponse<T>> criteria) {
            getResponse.criteria(criteria);
            return this;
        }

        @Override
        protected Function<HttpStepContext, R[]> getEndFunction() {
            return httpStepContext -> {
                boolean success = false;
                try {
                    catchValue(getResponse.getRequest().body(), of(new CaptorFilterByProducedType(Object.class)));
                    var result = super.getEndFunction().apply(httpStepContext);
                    success = true;
                    return result;
                } finally {
                    if ((success && catchSuccessEvent()) || (!success && catchFailureEvent())) {
                        catchValue(info, of(new CaptorFilterByProducedType(Object.class)));
                        catchValue(info.getLastReceived(), of(new CaptorFilterByProducedType(Object.class)));
                    }
                }
            };
        }
    }
}
