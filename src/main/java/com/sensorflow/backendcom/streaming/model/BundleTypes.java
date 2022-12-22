package com.sensorflow.backendcom.streaming.model;

public enum BundleTypes {
    GATEWAYS("gateways"),
    NODE_MEASUREMENTS("node_measurements"),
    NODE_META_DATA("node_meta_data"),
    NODE_JOIN_DATA("node_join_data"),
    GATEWAY_HEALTH_DATA("gateway_health_data"),
    NODE_SYSTEM_STATES("node_system_states");

    private String bundleType;


    BundleTypes(String bundleTypeName) {
        this.bundleType = bundleTypeName;
    }
}
