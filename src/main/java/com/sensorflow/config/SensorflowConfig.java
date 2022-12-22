package com.sensorflow.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class SensorflowConfig {

	@Value("${BASE_DIRECTORY:/data/}")
	private String baseDirector;

	@Value("${DEVICE_NAME:unknown}")
	private String balenaDeviceNameAtInit;

	@Value("${NEW_RELIC_INSERT_KEY:unknown}")
	private String newRelicInsertKey;

	@Value("${NEW_RELIC_ENTITY_GUID:unknown}")
	private String newRelicEntityGUID;

	@Value("${LOCATION_ID}")
	private String locationId;

	@Value("${NODE_EXPIRY_TIMEOUT_MIN:30}")
	private int nodeExpiryTimeout;

	@Value("${HASURA_ENDPOINT:unknown}")
	private String hasuraEndpoint;

    @Value("${HASURA_AUTHENTICATION_DOMAIN:unknown}")
	private String hasuraAuthenticationDomain;

    @Value("${HASURA_AUTHENTICATION_AUDIENCE:unknown}")
	private String hasuraAuthenticationAudience;

    @Value("${HASURA_AUTHENTICATION_CLIENT_ID:unknown}")
	private String hasuraAuthenticationClientId;

    @Value("${HASURA_AUTHENTICATION_CLIENT_SECRET:unknown}")
	private String hasuraAuthenticationClientSecret;

	@Value("${HASURA_AUTHENTICATION_TYPE:unknown}")
	private String hasuraAuthenticationType;

	@Value("${HASURA_ADMIN_SECRET:unknown}")
	private String hasuraAdminSecret;
}

