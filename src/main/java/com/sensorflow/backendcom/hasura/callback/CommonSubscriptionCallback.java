package com.sensorflow.backendcom.hasura.callback;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.jetbrains.annotations.NotNull;

import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.backendcom.newrelic.ErrorTypes;
import com.sensorflow.backendcom.newrelic.EventReporter;
import com.sensorflow.util.Pair;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class CommonSubscriptionCallback<T> implements ApolloSubscriptionCall.Callback<Optional<T>> {
    private final Callable<Object> retries;
    private final String subscriptionName;
    private SubscriptionStatus status;

    CommonSubscriptionCallback(Callable<Object> retries, String subscriptionName) {
        this.retries = retries;
        this.subscriptionName = subscriptionName;
        status = SubscriptionStatus.DISCONNECTED;
    }

    @Override
    public void onResponse(Response<Optional<T>> response) {
        Objects.requireNonNull(response.getData()).ifPresent(this::handleData);
    }

    @Override
    public void onFailure(@NotNull ApolloException e) {
        EventReporter.recordEvent(EventReporter.SUBSCRIPTION_EVENT, new Pair<>("subscription", subscriptionName),
                new Pair<>("event", "FAILED"));
        try {
            retries.call();
        } catch (Exception ex) {
            ErrorReporter.reportError("resubscription_failed: ", ErrorTypes.HASURA_ERROR, new Pair<>("subscription", subscriptionName));
        }
        status = SubscriptionStatus.ERROR;
    }

    @Override
    public void onCompleted() {
        EventReporter.recordEvent(EventReporter.SUBSCRIPTION_EVENT, new Pair<>("subscription", subscriptionName),
                new Pair<>("event", "COMPLETED"));
        log.info("completed connecting to Hasura Service! {}", subscriptionName);
        status = SubscriptionStatus.COMPLETE;
    }

    @Override
    public void onTerminated() {
        EventReporter.recordEvent(EventReporter.SUBSCRIPTION_EVENT, new Pair<>("subscription", subscriptionName),
                new Pair<>("event", "TERMINATED"));
        try {
            retries.call();
        } catch (Exception e) {
            ErrorReporter.reportError("resubscription_failed: ", ErrorTypes.HASURA_ERROR, new Pair<>("subscription", subscriptionName));
        }
        status = SubscriptionStatus.DISCONNECTED;
    }

    @Override
    public void onConnected() {
        EventReporter.recordEvent(EventReporter.SUBSCRIPTION_EVENT, new Pair<>("subscription", subscriptionName),
                new Pair<>("event", "CONNECTED"));
        log.info("Connected to Hasura Service! {}", subscriptionName);
        status = SubscriptionStatus.CONNECTED;
    }



    abstract void handleData(T data);
}
