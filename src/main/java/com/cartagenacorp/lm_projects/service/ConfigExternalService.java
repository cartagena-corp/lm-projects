package com.cartagenacorp.lm_projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class ConfigExternalService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigExternalService.class);

    @Value("${config.service.url}")
    private String configServiceUrl;

    private final RestTemplate restTemplate;

    public ConfigExternalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void initializeDefaultProjectConfig(UUID projectId, String token) {
        logger.debug("Inicializando configuraciones por defecto para el proyecto: {}", projectId);
        try {
            String url = String.format("%s/%s", configServiceUrl, projectId.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
            logger.info("Configuraciones por defecto creadas exitosamente para el proyecto: {}", projectId);
        } catch (HttpClientErrorException ex) {
            logger.error("Error al inicializar configuraciones. Código de estado: {}. Mensaje: {}", ex.getStatusCode(), ex.getMessage());
        } catch (ResourceAccessException ex) {
            logger.error("El servicio de configuración no está disponible: {}", ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error inesperado al inicializar configuración para el proyecto {}: {}", projectId, ex.getMessage());
        }
    }

    public void deleteByProjectId(UUID projectId, String token) {
        logger.debug("Eliminando todas las configuraciones para el proyecto: {}", projectId);
        try {
            String url = String.format("%s/%s", configServiceUrl, projectId.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);

            logger.info("Todas las configuraciones eliminadas exitosamente para el proyecto: {}", projectId);
        } catch (HttpClientErrorException ex) {
            logger.error("Error al eliminar la configuración. Código de estado: {}. Mensaje: {}", ex.getStatusCode(), ex.getMessage());
        } catch (ResourceAccessException ex) {
            logger.error("El servicio de configuración no está disponible: {}", ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error inesperado al eliminar configuración para el proyecto {}: {}", projectId, ex.getMessage());
        }
    }
}
