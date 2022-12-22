package com.sensorflow.backendcom.streaming;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.jetbrains.annotations.NotNull;

import com.sensorflow.backendcom.hasura.graphql.type.*;
import com.sensorflow.backendcom.streaming.model.GatewayHealthPacket;
import com.sensorflow.backendcom.streaming.model.GatewayPacket;
import com.sensorflow.backendcom.streaming.model.NodeJoinPingPacket;
import com.sensorflow.backendcom.streaming.model.NodeMeasurementPacket;
import com.sensorflow.backendcom.streaming.model.NodeMetadataPacket;
import com.sensorflow.backendcom.streaming.model.NodePacket;
import com.sensorflow.backendcom.streaming.model.NodeSystemStatesPacket;

public class BundleTransformer {

    private BundleTransformer(){

    }

    @NotNull
    static com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_measurements_insert_input buildNodeMeasurementInputItem(NodeMeasurementPacket nodeMeasurementPacket) {
        return com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_measurements_insert_input.builder()
            .measurementType(nodeMeasurementPacket.getMeasurementType())
            .measurementValue(nodeMeasurementPacket.getMeasurementValue())
            .positionId(nodeMeasurementPacket.getPositionId())
            .srcMacId(nodeMeasurementPacket.getSrcMacId())
            .streamIndex(nodeMeasurementPacket.getStreamIndex())
            .time(LocalDateTime.ofEpochSecond(nodeMeasurementPacket.getUnixTimestamp(), 0, ZoneOffset.UTC))
            .build();
    }

    @NotNull
    static com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_gateways_insert_input buildGatewayInfoInputItem(GatewayPacket gatewayPacket) {
        return com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_gateways_insert_input.builder()
            .gatewayName(gatewayPacket.getBalenaDeviceName())
            .gatewayId(gatewayPacket.getBalenaId())
            .ethernetMacAddress(gatewayPacket.getEthernetMAcAddress())
            .gatewayMac(gatewayPacket.getGatewayMac())
            .networkConnectionStatus(gatewayPacket.getNetworkConnectionStatus())
            .positionId(gatewayPacket.getPositionId())
            .version(gatewayPacket.getVersion())
            .wifiMacAddress(gatewayPacket.getWifiMacAddress())
            .build();
    }

    @NotNull
    static com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_nodes_insert_input buildNodeInfoInputItem(NodePacket nodePacket) {
        return com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_nodes_insert_input.builder()
            .node_mac_id(nodePacket.getNodeMacId())
            .node_type(nodePacket.getNodeType())
            .associated_gateway(nodePacket.getAssociatedGateway())
            .nodeSubType(nodePacket.getNodeSubType())
            .currentFirmwareVersionNumber(nodePacket.getFirmwareVersionNumber())
            .build();
    }

    @NotNull
    static com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_meta_data_insert_input buildNodeMetaDataInputItem(NodeMetadataPacket nodeMetadataPacket) {

        return com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_meta_data_insert_input.builder()
            .measurementType(nodeMetadataPacket.getMeasurementType())
            .measurementValue(nodeMetadataPacket.getMeasurementValue())
            .nodeMacId(nodeMetadataPacket.getNodeMacId())
            .time(LocalDateTime.ofEpochSecond(nodeMetadataPacket.getUnixTimestamp(), 0, ZoneOffset.UTC))
            .build();
    }

    @NotNull
    static com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_join_data_insert_input buildNodeJoinPingInputItem(NodeJoinPingPacket nodeJoinPingPacket) {

        return com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_node_join_data_insert_input.builder()
            .balena_id(nodeJoinPingPacket.getBalenaId())
            .node_mac_id(nodeJoinPingPacket.getNodeMacId())
            .rssi(nodeJoinPingPacket.getRssi())
            .snr(nodeJoinPingPacket.getSnr())
            .time(LocalDateTime.ofEpochSecond(nodeJoinPingPacket.getTime(), 0, ZoneOffset.UTC))
            .build();
    }

    @NotNull
    static com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_gateway_health_data_insert_input buildGatewayHealthDataInputItem(GatewayHealthPacket gatewayHealthPacket) {

        return com.sensorflow.backendcom.hasura.graphql.type.Sensorflow_gateway_health_data_insert_input.builder()
            .balenaId(gatewayHealthPacket.getBalenaId())
            .measurementType(gatewayHealthPacket.getMeasurementType())
            .time(LocalDateTime.ofEpochSecond(gatewayHealthPacket.getTime(), 0, ZoneOffset.UTC))
            .value(gatewayHealthPacket.getValue())
            .build();
    }


//                required for future code related to the node diagnostics module
//    @NotNull
//    static Sensorflow_node_diagnostics_insert_input buildNodeDiagnosticInput(NodeDiagnosticPacket nodeDiagnosticPacket) {
//
//        return Sensorflow_node_diagnostics_insert_input.builder()
//                .time(nodeDiagnosticPacket.getTime())
//                .node_mac_id(nodeDiagnosticPacket.getNodeMacId())
//                .flash_erase_count(nodeDiagnosticPacket.getFlashEraseCount())
//                .cancelt_hw(nodeDiagnosticPacket.getCanceltHw())
//                .joint_hw(nodeDiagnosticPacket.getJoinTHw())
//                .synct_hw(nodeDiagnosticPacket.getSyncTHw())
//                .listent_hw(nodeDiagnosticPacket.getListenTHw())
//                .contlistt_hw(nodeDiagnosticPacket.getContListTHw())
//                .otaprept_hw(nodeDiagnosticPacket.getOtaPrepTHw())
//                .startup_hw(nodeDiagnosticPacket.getStartupHw())
//                .amtxt_hw(nodeDiagnosticPacket.getAmTxTHw())
//                .amrecvt_hw(nodeDiagnosticPacket.getAmRecvTHw())
//                .procsett_hw(nodeDiagnosticPacket.getProcSetTHw())
//                .timertask_hw(nodeDiagnosticPacket.getTimerTaskHw())
//                .sendmon_hw(nodeDiagnosticPacket.getSendMonHw())
//                .device_specific_1_hw(nodeDiagnosticPacket.getDeviceSpecific1Hw())
//                .device_specific_2_hw(nodeDiagnosticPacket.getDeviceSpecific2Hw())
//                .device_specific_3_hw(nodeDiagnosticPacket.getDeviceSpecific3Hw())
//                .device_specific_4_hw(nodeDiagnosticPacket.getDeviceSpecific4Hw())
//                .device_specific_5_hw(nodeDiagnosticPacket.getDeviceSpecific5Hw())
//                .device_specific_6_hw(nodeDiagnosticPacket.getDeviceSpecific6Hw())
//                .free_heap(nodeDiagnosticPacket.getFreeHeap())
//                .avg_beacon_signal_strength(nodeDiagnosticPacket.getAvgBeaconSignalStrength())
//                .min_sleep_mode(nodeDiagnosticPacket.getMinSleepMode())
//                .num_gateways_tracked(nodeDiagnosticPacket.getNumGatewaysTracked())
//                .failed_transmissions(nodeDiagnosticPacket.getFailedTransmissions())
//                .min_sync_drift_ms(nodeDiagnosticPacket.getMinSyncDriftMs())
//                .max_sync_drift_ms(nodeDiagnosticPacket.getMaxSyncDriftMs())
//                .successful_join_attempts(nodeDiagnosticPacket.getSuccessfulJoinAttempts())
//                .failed_join_attempts(nodeDiagnosticPacket.getFailedJoinAttempts())
//                .build();
//    }
//
    @NotNull
    static Sensorflow_node_system_states_insert_input buildNodeSystemStatesInput(NodeSystemStatesPacket nodeSystemStatesPacket) {

        return Sensorflow_node_system_states_insert_input.builder()
                .time(nodeSystemStatesPacket.getTimestamp())
                .nodeMacId(nodeSystemStatesPacket.getNodeMacId())
                .asleepS(nodeSystemStatesPacket.getMcuAsleepS())
                .awakeS(nodeSystemStatesPacket.getMcuAwakeS())
                .radioRxS(nodeSystemStatesPacket.getRadioListenS())
                .radioTxS(nodeSystemStatesPacket.getRadioSendingS())
                .extra1S(nodeSystemStatesPacket.getExtra1S())
                .extra2S(nodeSystemStatesPacket.getExtra2S())
                .build();
    }

}
