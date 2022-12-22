package com.sensorflow.backendcom.streaming.model;

import java.util.Collection;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import lombok.Getter;
import lombok.Setter;

public class DataBundle {
    @SuppressWarnings("squid:S1068")
    @Getter
    private BundleTypes bundledDataType;

    @Getter
    private Collection<DataPacket> dataPackets;

    //shared instance amongst all bundles to avoid creation of new gson instance on every serialisation
    private static RuntimeTypeAdapterFactory<DataPacket> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
            .of(DataPacket.class, "type")
            .registerSubtype(NodeMeasurementPacket.class, "NodeMeasurementPacket")
            .registerSubtype(GatewayHealthPacket.class, "GatewayHealthPacket")
            .registerSubtype(NodeJoinPingPacket.class, "NodeJoinPingPacket")
            .registerSubtype(GatewayPacket.class, "GatewayPacket")
            .registerSubtype(NodeMetadataPacket.class, "NodeMetadataPacket")
            .registerSubtype(NodePacket.class, "NodePacket")
            .registerSubtype(NodeDiagnosticPacket.class, "NodeDiagnosticPacket")
            .registerSubtype(NodeSystemStatesPacket.class, "NodeSystemStatesPacket");
    private static Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().
            registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

    @Getter
    @Setter
    private Integer id = null;

    public DataBundle(){

    }

    public DataBundle(BundleTypes bundledDataType) {
        this.bundledDataType = bundledDataType;
        this.dataPackets = new Vector<>();
    }

    public DataBundle(BundleTypes bundledDataType, Collection<DataPacket> dataPackets){
       this.bundledDataType = bundledDataType;
       this.dataPackets = dataPackets;
    }

    public void addDataPacket(DataPacket packet){
        dataPackets.add(packet);
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static DataBundle fromJson(String json){
        return gson.fromJson(json, DataBundle.class);
    }

    @Override
    public String toString() {
        return this.toJson();
    }
}
