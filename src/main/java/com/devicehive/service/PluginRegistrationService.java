package com.devicehive.service;

import com.devicehive.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PluginRegistrationService {

    private final PluginInfoService pluginInfoService;
    private final Environment environment;

    private JwtToken userToken;

    @Autowired
    public PluginRegistrationService(PluginInfoService pluginInfoService, Environment environment) {
        this.pluginInfoService = pluginInfoService;
        this.environment = environment;
    }

    public void initPlugin() {
        userToken = getUserToken();
        registerPlugin();
    }

    private JwtToken getUserToken() {
        String login = environment.getProperty("dh.user.login");
        String password = environment.getProperty("dh.user.password");
        String authEndpoint = environment.getProperty("dh.endpoint.auth");

        UserAuthRequest authRequest = new UserAuthRequest(login, password);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(authEndpoint, authRequest, JwtToken.class);
    }

    private void registerPlugin() {
        String deviceIds = environment.getProperty("plugin.device-ids");
        String networkIds = environment.getProperty("plugin.network-ids");
        String names = environment.getProperty("plugin.names");
        String timestamp = environment.getProperty("plugin.timestamp");
        boolean returnCommands = environment.getProperty("plugin.return-commands", Boolean.class);
        boolean returnUpdatedCommands = environment.getProperty("plugin.return-updated-commands", Boolean.class);
        boolean returnNotifications = environment.getProperty("plugin.return-notifications", Boolean.class);

        String registrationEndpoint = environment.getProperty("dh.endpoint.plugin");

        PluginRegistrationRequest registrationRequest = new PluginRegistrationRequest(deviceIds, networkIds, names, timestamp,
                returnCommands, returnUpdatedCommands, returnNotifications);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> params = mapper.convertValue(registrationRequest, Map.class);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken.getAccessToken());

        HttpEntity<PluginInfo> requestEntity = new HttpEntity<>(pluginInfoService.getPluginInfo(), headers);

        ResponseEntity<PluginRegistrationResponse> response = restTemplate.postForEntity(registrationEndpoint, requestEntity, PluginRegistrationResponse.class, params);
        response.getBody();
    }
}