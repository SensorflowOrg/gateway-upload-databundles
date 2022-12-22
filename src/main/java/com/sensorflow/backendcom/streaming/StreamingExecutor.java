package com.sensorflow.backendcom.streaming;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import com.sensorflow.backendcom.hasura.HasuraClient;

import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.backendcom.newrelic.ErrorTypes;
import com.sensorflow.backendcom.streaming.model.DataBundle;
import com.sensorflow.backendcom.streaming.model.DataPacket;
import com.sensorflow.backendcom.streaming.model.GatewayHealthPacket;
import com.sensorflow.backendcom.streaming.model.GatewayPacket;
import com.sensorflow.backendcom.streaming.model.NodeJoinPingPacket;
import com.sensorflow.backendcom.streaming.model.NodeMeasurementPacket;
import com.sensorflow.backendcom.streaming.model.NodeMetadataPacket;
import com.sensorflow.backendcom.streaming.model.NodeSystemStatesPacket;
import com.sensorflow.util.Pair;

@Service
public class StreamingExecutor {
    private static Logger logger = LoggerFactory.getLogger(StreamingExecutor.class);

    @Autowired
    HasuraClient hasuraClient;

    @Autowired
    DataBundlePersistence dataBundlePersistence;

//    @Autowired
//    MappingController mappingController;

    void insertNodeMetaData(DataBundle bundle) {
        final Collection<DataPacket> dataPackets = bundle.getDataPackets();
        List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_meta_data_insert_input> tuples = repackageNodeMetaDataBundle(dataPackets);
        hasuraClient.mutate(new com.sensorflow.backendcom.hasura.graphql.InsertManyNodeMetaDataMutation(tuples), new ApolloCall.Callback<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyNodeMetaDataMutation.Data>>() {
            @Override
            public void onResponse(@NotNull Response<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyNodeMetaDataMutation.Data>> response) {
                handleResponse(response,bundle);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                handleBundleFailure(bundle,e);
            }
        });
    }

    List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_meta_data_insert_input> repackageNodeMetaDataBundle(Collection<DataPacket> dataPackets) {
        return dataPackets.stream()
                .filter(NodeMetadataPacket.class::isInstance)
                .map(dataPacket -> BundleTransformer.buildNodeMetaDataInputItem((NodeMetadataPacket) dataPacket))
                .collect(Collectors.toList());

    }

    void insertNodeJoinData(DataBundle bundle) {
        final Collection<DataPacket> dataPackets = bundle.getDataPackets();
        List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_join_data_insert_input> tuples = dataPackets.stream()
                .filter(NodeJoinPingPacket.class::isInstance)
                .map(dataPacket -> BundleTransformer.buildNodeJoinPingInputItem((NodeJoinPingPacket) dataPacket))
                .collect(Collectors.toList());
        hasuraClient.mutate(new com.sensorflow.backendcom.hasura.graphql.InsertManyNodeJoinDataMutation(tuples), new ApolloCall.Callback<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyNodeJoinDataMutation.Data>>() {
            @Override
            public void onResponse(@NotNull Response<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyNodeJoinDataMutation.Data>> response) {
                handleResponse(response, bundle);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                handleBundleFailure(bundle,e);
            }
        });
    }

    void insertGatewayHealthData(DataBundle bundle) {
        final Collection<DataPacket> dataPackets = bundle.getDataPackets();
        List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_gateway_health_data_insert_input> tuples = dataPackets
                .stream().filter(GatewayHealthPacket.class::isInstance)
                .map(dataPacket -> BundleTransformer.buildGatewayHealthDataInputItem((GatewayHealthPacket) dataPacket))
                .collect(Collectors.toList());
        hasuraClient.mutate(new com.sensorflow.backendcom.hasura.graphql.InsertManyGatewayHealthDataMutation(tuples), new ApolloCall.Callback<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyGatewayHealthDataMutation.Data>>() {
            @Override
            public void onResponse(@NotNull Response<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyGatewayHealthDataMutation.Data>> response) {

                handleResponse(response, bundle);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                handleBundleFailure(bundle, e);
            }
        });
    }

    void  insertNodeMeasurements(DataBundle bundle) {
        final Collection<DataPacket> dataPackets = bundle.getDataPackets();
        List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_measurements_insert_input> tuples = new ArrayList<>();
        for (DataPacket dataPacket : dataPackets) {
            if (dataPacket instanceof NodeMeasurementPacket) {
                final NodeMeasurementPacket nodeMeasurementPacket = (NodeMeasurementPacket) dataPacket;
                if(nodeMeasurementPacket.getPositionId() == null){
                    ErrorReporter.reportError("No mapping info for node",ErrorTypes.MAPPING_ERROR,
                        new Pair<>("node_mac_id",nodeMeasurementPacket.getSrcMacId()));
                    continue;
                }
                com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_measurements_insert_input measurement = BundleTransformer.buildNodeMeasurementInputItem(nodeMeasurementPacket);
                tuples.add(measurement);
            }
        }
        if(!tuples.isEmpty()) {
            hasuraClient.mutate(new com.sensorflow.backendcom.hasura.graphql.InsertManyNodeMeasurementsMutation(tuples), new ApolloCall.Callback<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyNodeMeasurementsMutation.Data>>() {
                @Override
                public void onResponse(@NotNull Response<Optional<com.sensorflow.backendcom.hasura.graphql.InsertManyNodeMeasurementsMutation.Data>> response) {
                    handleResponse(response, bundle);
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    handleBundleFailure(bundle, e);
                }
            });
        }
    }

    void upsertGatewayInfos(DataBundle bundle) {
        final Collection<DataPacket> dataPackets = bundle.getDataPackets();
        List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_gateways_insert_input> input = dataPackets.stream().filter(GatewayPacket.class::isInstance)
                .map(dataPacket -> BundleTransformer.buildGatewayInfoInputItem((GatewayPacket) dataPacket))
                .collect(Collectors.toList());
        hasuraClient.mutate(new com.sensorflow.backendcom.hasura.graphql.UpsertGatewayInfoMutation(input), new ApolloCall.Callback<Optional<com.sensorflow.backendcom.hasura.graphql.UpsertGatewayInfoMutation.Data>>() {
            @Override
            public void onResponse(@NotNull Response<Optional<com.sensorflow.backendcom.hasura.graphql.UpsertGatewayInfoMutation.Data>> response) {
                handleResponse(response, bundle);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                handleBundleFailure(bundle, e);
            }
        });
    }


    <T> void handleResponse(@NotNull Response<T> response, DataBundle bundle) {
        if(response.hasErrors()) {
            logger.error("error when upload bundle {} {}", bundle.toString(), response.getErrors());
            handleErrorResponse(bundle, Objects.requireNonNull(response.getErrors()));
        }else{
            logger.info("success upload bundle {}", bundle.toString());
            removeBundleIfPersisted(bundle);
        }
    }

    void handleBundleFailure(DataBundle bundle, ApolloException e) {
        logger.error("error when upload bundle {} {}", bundle.toString(),e );
        ErrorReporter.reportError(e, new Pair<>("bundle_info", bundle.toString()));
        // persistDataBundle(bundle);
    }

    void handleErrorResponse(DataBundle bundle, List<Error> errors){
        boolean delete = false;
        for (Error error : errors) {
            String message = "bundle streaming error";
            if(error.getMessage().startsWith("Uniqueness violation")){
                message = "duplicate data uploaded";
                delete = true;
            }else if(error.getMessage().startsWith("Foreign key violation")){
                message = "Foreign key violation";
                delete = true;
            }else if(error.getMessage().startsWith("insert/update/delete not permitted")){
                message = "insert error";
                delete = true;
            }else if(error.getMessage().startsWith("database query error")){
                message = "db query error";
                delete = true;
            }
            String bundleString = "unserializable";
            try{
                bundleString = bundle.toString();
            }catch(Exception e){
//                ignore this
            }
            ErrorReporter.reportError(message, ErrorTypes.STREAMING_ERROR,
                    new Pair<>("message", error.getMessage()),
                    new Pair<>("bundle_info", bundleString));
        }
        if (delete) {
            removeBundleIfPersisted(bundle);
        }
    }

    /**
     * checks if sucessfully transmitted bundle was persisted in DB before and removes it if yes
     * @param bundle the bundle to remove
     */
    void removeBundleIfPersisted(DataBundle bundle) {
        if(bundle.getId() != null) {
            try {
                dataBundlePersistence.removeDataBundle(bundle.getId());
            } catch (SQLException e) {
                logger.info("removeBundleIfPersisted error {}", e.getMessage());
                ErrorReporter.reportError(e);
            }
        }
    }

    void insertNodeSystemStates(DataBundle bundle) {
        final Collection<DataPacket> dataPackets = bundle.getDataPackets();
        List<com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_system_states_insert_input> input = dataPackets.stream().filter(NodeSystemStatesPacket.class::isInstance)
                .map(dataPacket -> BundleTransformer.buildNodeSystemStatesInput((NodeSystemStatesPacket) dataPacket)).collect(Collectors.toList());
        hasuraClient.mutate(new com.sensorflow.backendcom.hasura.graphql.InsertNodeSystemStatesRecordMutation(input), new ApolloCall.Callback<Optional<com.sensorflow.backendcom.hasura.graphql.InsertNodeSystemStatesRecordMutation.Data>>() {
            @Override
            public void onResponse(@NotNull Response<Optional<com.sensorflow.backendcom.hasura.graphql.InsertNodeSystemStatesRecordMutation.Data>> response) {
                handleResponse(response, bundle);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                handleBundleFailure(bundle, e);
            }
        });
    }
}
