package com.sensorflow.backendcom.hasura;

import org.springframework.stereotype.Component;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.config.SensorflowConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HasuraAuthenticationProvider {

    private final SensorflowConfig sensorflowConfig;

    private final AuthAPI authAPI;

    public HasuraAuthenticationProvider(SensorflowConfig sensorflowConfig) {
        this.sensorflowConfig = sensorflowConfig;
        this.authAPI = new AuthAPI(sensorflowConfig.getHasuraAuthenticationDomain(), sensorflowConfig.getHasuraAuthenticationClientId(), sensorflowConfig.getHasuraAuthenticationClientSecret());
    }

    public String getToken() {
        try {
            AuthRequest request = authAPI.requestToken(sensorflowConfig.getHasuraAuthenticationAudience());

            TokenHolder holder = request.execute();
            return String.format("%s %s", holder.getTokenType(), holder.getAccessToken());
        } catch (Auth0Exception e) {
            ErrorReporter.reportError(e);
        }

        return null;
    }
}
