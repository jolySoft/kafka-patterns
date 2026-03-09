package com.kafkapatterns.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A generic Kafka message envelope that wraps a typed payload with standard
 * metadata headers for identity, traceability, and routing.
 *
 * <p>Auto-populated fields (MessageId, SentTime, Host, ConversationId, MessageType)
 * are set on construction. User and Request fields default to null and are set
 * via setters or the builder.
 *
 * @param <K> the partition key type
 * @param <V> the payload type
 */
public class MessageEnvelope<K, V> {

    // Auto fields
    private UUID messageId;
    private String conversationId;
    private Instant sentTime;
    private String sourceAddress;
    private String host;
    private List<String> messageType;

    // User fields
    private String correlationId;
    private String faultAddress;
    private Instant expirationTime;
    private Map<String, String> headers;

    // Request fields
    private String requestId;
    private String destinationAddress;
    private String responseAddress;

    // Body
    private K key;
    private V payload;

    /** Required by Jackson for deserialization. */
    public MessageEnvelope() {}

    public MessageEnvelope(K key, V payload) {
        this.messageId = UUID.randomUUID();
        this.conversationId = UUID.randomUUID().toString();
        this.sentTime = Instant.now();
        this.host = resolveHostname();
        this.messageType = new ArrayList<>();
        if (payload != null) {
            this.messageType.add(toMessageUrn(payload.getClass()));
        }
        this.headers = new HashMap<>();
        this.key = key;
        this.payload = payload;
    }

    // --- Getters and setters ---

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public Instant getSentTime() { return sentTime; }
    public void setSentTime(Instant sentTime) { this.sentTime = sentTime; }

    public String getSourceAddress() { return sourceAddress; }
    public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public List<String> getMessageType() { return messageType; }
    public void setMessageType(List<String> messageType) { this.messageType = messageType; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getFaultAddress() { return faultAddress; }
    public void setFaultAddress(String faultAddress) { this.faultAddress = faultAddress; }

    public Instant getExpirationTime() { return expirationTime; }
    public void setExpirationTime(Instant expirationTime) { this.expirationTime = expirationTime; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public String getResponseAddress() { return responseAddress; }
    public void setResponseAddress(String responseAddress) { this.responseAddress = responseAddress; }

    public K getKey() { return key; }
    public void setKey(K key) { this.key = key; }

    public V getPayload() { return payload; }
    public void setPayload(V payload) { this.payload = payload; }

    // --- Helpers ---

    private static String resolveHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            return "unknown";
        }
    }

    static String toMessageUrn(Class<?> type) {
        return "urn:message:" + type.getName().replace('.', '/');
    }
}
