package com.sensorflow.backendcom.hasura;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Subscription;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.OnSubscriptionManagerStateChangeListener;
import com.apollographql.apollo.subscription.SubscriptionManagerState;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.sensorflow.backendcom.hasura.callback.CommonSubscriptionCallback;
import com.sensorflow.backendcom.hasura.graphql.type.CustomType;
import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.backendcom.newrelic.ErrorTypes;
import com.sensorflow.config.SensorflowConfig;
import com.sensorflow.util.Pair;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Service
@Slf4j
public class HasuraClient {
    private static final Logger logger = LoggerFactory.getLogger(HasuraClient.class);
    private final SensorflowConfig sensorflowConfig;
    private final ApolloClient client;
    private final OkHttpClient okHttpClient;
    private final HasuraAuthenticationProvider authenticationProvider;
    private static final int MAX_REQUESTS_QUEUED = 300;

    OnSubscriptionManagerStateChangeListener onStateChangeListener;

    public HasuraClient(SensorflowConfig sensorflowConfig, HasuraAuthenticationProvider authenticationProvider) {
        this.sensorflowConfig = sensorflowConfig;
        this.authenticationProvider = authenticationProvider;
        if (isHasuraAdmin()) {
            okHttpClient = getOkHttpClientAdmin();
        } else {
            okHttpClient = getOkHttpClient();
        }
        this.client = getApolloBuilder(okHttpClient).build();

        onStateChangeListener = (SubscriptionManagerState fromState, SubscriptionManagerState toState) ->{
            logger.info("subscriptionManager changed state From {} to {}", fromState, toState);
            switch (toState) {
                case STOPPED:
                    client.getSubscriptionManager().start();
                    break;
                case DISCONNECTED:
                case CONNECTED:
                case ACTIVE:
                case STOPPING:
                case CONNECTING:
                    break;
            }

        };

        this.client.addOnSubscriptionManagerStateChangeListener(onStateChangeListener);
    }

    private ApolloClient.Builder getApolloBuilder(OkHttpClient okHttpClient) {
        ApolloClient.Builder builder =  ApolloClient.builder()
                .serverUrl(sensorflowConfig.getHasuraEndpoint())
                .subscriptionTransportFactory(
                        new WebSocketSubscriptionTransport.Factory(sensorflowConfig.getHasuraEndpoint(),
                                okHttpClient))
                .okHttpClient(okHttpClient)
                .subscriptionHeartbeatTimeout(10,TimeUnit.SECONDS);

        return addTypeAdapters(builder);
    }

    private OkHttpClient getOkHttpClient() {
        if(authenticationProvider == null || authenticationProvider.getToken() == null){
//            for tests to avoid tons of null pointers
            return new OkHttpClient.Builder().build();
        }else {
            return new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                            .addHeader("authorization", authenticationProvider.getToken())
                            .addHeader("content-type", "application/json")
                            .build()))
                    .callTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
    }

    private OkHttpClient getOkHttpClientAdmin() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .addHeader("content-type", "application/json")
                        .addHeader("x-hasura-admin-secret", sensorflowConfig.getHasuraAdminSecret())
                        .addHeader("Hasura-Client-Name", sensorflowConfig.getBalenaDeviceNameAtInit())
                        .build()))
                .callTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private ApolloClient.Builder addTypeAdapters(ApolloClient.Builder builder){
        builder.addCustomTypeAdapter(CustomType.NUMERIC, new CustomTypeAdapter<Double>() {
            @NotNull
            @Override
            public CustomTypeValue<?> encode(Double aDouble) {
                return new CustomTypeValue.GraphQLString(aDouble.toString());
            }

            @Override
            public Double decode(@NotNull CustomTypeValue<?> customTypeValue) {
                return Double.parseDouble(customTypeValue.value.toString());
            }
        })
        .addCustomTypeAdapter(CustomType.TIMESTAMPTZ, new CustomTypeAdapter<LocalDateTime>() {
            @Override
            public LocalDateTime decode(@NotNull CustomTypeValue<?> customTypeValue) {
                return LocalDateTime.parse(customTypeValue.value.toString(), DateTimeFormatter.ISO_DATE_TIME);
            }

            @NotNull
            @Override
            public CustomTypeValue<?> encode(LocalDateTime dateTime) {
                return new CustomTypeValue.GraphQLString(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }).addCustomTypeAdapter(CustomType.UUID, new CustomTypeAdapter<String>() {
        @Override
        public String decode(@NotNull CustomTypeValue<?> customTypeValue) {
            return customTypeValue.value.toString();
        }

        @NotNull
        @Override
        public CustomTypeValue<?> encode(String s) {
            return new CustomTypeValue.GraphQLString(s);
        }
        });
        return builder;
    }

    private boolean isHasuraAdmin() {
        return sensorflowConfig.getHasuraAuthenticationType().equalsIgnoreCase("admin");
    }

    public <D extends Operation.Data, T, V extends Operation.Variables> void subscribe(Subscription<D, T, V> subscription, CommonSubscriptionCallback callback) {
            try{
                ApolloSubscriptionCall<T> apolloSubscriptionCall = client.subscribe(subscription);
                apolloSubscriptionCall.execute(callback);
            }catch(Exception e){
                ErrorReporter.reportError(e,new Pair<>("subscription",callback.getSubscriptionName()));
                ErrorReporter.reportError("unable to subscribe", ErrorTypes.HASURA_ERROR,
                        new Pair<>("subscription",callback.getSubscriptionName()));
            }
    }

    public <D extends Operation.Data, T, V extends Operation.Variables> void query(Query<D, T, V> query, ApolloCall.Callback<T> callback) {
        if(getQueuedCallsCount() > MAX_REQUESTS_QUEUED){
            log.warn("Hasura requests building up {}", getQueuedCallsCount());
            ErrorReporter.reportError("Hasura requests building up", ErrorTypes.GATEWAY_ERROR,
                    new Pair<>("queued requests",getQueuedCallsCount()));
            callback.onFailure(new ApolloException("RequestQueue is full"));
        }else{
            client.query(query).enqueue(callback);
        }
    }

    public <D extends Operation.Data, T, V extends Operation.Variables> void mutate(Mutation<D, T, V> mutation, ApolloCall.Callback<T> callback) {
        if(getQueuedCallsCount() > MAX_REQUESTS_QUEUED){
            log.warn("Hasura requests building up {}", getQueuedCallsCount());
            ErrorReporter.reportError("Hasura requests building up", ErrorTypes.GATEWAY_ERROR,
                    new Pair<>("queued requests",getQueuedCallsCount()));
            callback.onFailure(new ApolloException("RequestQueue is full"));
        }else{
            client.mutate(mutation).enqueue(callback);
        }
    }

    /**
     * returns the number of requests currently queued in memory. we need to monitor this in order to avoid
     * outOfMemoryErrors during offline states or slow networks
     * @return the number of queued requests in the dispatcher
     */
    public int getQueuedCallsCount(){
        return okHttpClient.dispatcher().queuedCallsCount();
    }

}
