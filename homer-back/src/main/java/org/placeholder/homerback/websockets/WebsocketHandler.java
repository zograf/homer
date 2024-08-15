package org.placeholder.homerback.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class WebsocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);
    // Map<propertyId, List<WebSocketSession>> || Map<deviceId, List<WebSocketSession>> -> message: deviceId, moduleId -> json(module)
    private HashMap<Integer, List<WebSocketSession>> propertySessionMap;
    private HashMap<Integer, List<WebSocketSession>> deviceSessionMap;
    public WebsocketHandler() {
        this.propertySessionMap = new HashMap<Integer, List<WebSocketSession>>();
        this.deviceSessionMap = new HashMap<Integer, List<WebSocketSession>>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String propertyIdParam = session.getUri().getQuery();
            String[] queryParams = propertyIdParam.split("=");
            if (queryParams[0].equals("propertyId")) {
                Integer propertyId = Integer.parseInt(queryParams[1]);

                if (propertySessionMap.containsKey(propertyId)) {
                    propertySessionMap.get(propertyId).add(session);
                } else {
                    propertySessionMap.put(propertyId, new ArrayList<WebSocketSession>(Arrays.asList(session)));
                }
            } else {
                Integer deviceId = Integer.parseInt(queryParams[1]);

                if (deviceSessionMap.containsKey(deviceId)) {
                    deviceSessionMap.get(deviceId).add(session);
                } else {
                    deviceSessionMap.put(deviceId, new ArrayList<WebSocketSession>(Arrays.asList(session)));
                }
            }

            super.afterConnectionEstablished(session);
        } catch(Exception ex) {
            logger.info("Failed to get id from websocket connection header");
            return;
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("Received websocket message: " + payload);
        super.handleTextMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            String propertyIdParam = session.getUri().getQuery();
            String[] queryParams = propertyIdParam.split("=");
            if (queryParams[0].equals("propertyId")) {
                Integer propertyId = Integer.parseInt(queryParams[1]);
                propertySessionMap.remove(propertyId);
                logger.info("Websocket connection closed -  Property id: " + propertyId);
            } else {
                Integer deviceId = Integer.parseInt(queryParams[1]);
                deviceSessionMap.remove(deviceId);
                logger.info("Websocket connection closed -  Device id: " + deviceId);
            }

            super.afterConnectionEstablished(session);
        } catch(Exception ex) {
            logger.info("Failed to get id from websocket connection header");
        }
        super.afterConnectionClosed(session, status);
    }

    public void sendMessageProperty(Integer propertyId, Object message) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        if (!this.propertySessionMap.containsKey(propertyId)){
            return;
        }
        for (WebSocketSession session : this.propertySessionMap.get(propertyId)) {
            if (session == null) continue;
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                    logger.info("Websocket message to property: " + objectMapper.writeValueAsString(message));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendMessageDevice(Integer deviceId, Object message) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        if (!this.deviceSessionMap.containsKey(deviceId)){
            return;
        }
        for (WebSocketSession session : this.deviceSessionMap.get(deviceId)) {
            if (session == null) continue;
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                    logger.info("Websocket message to device: " + objectMapper.writeValueAsString(message));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}