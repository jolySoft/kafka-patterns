package com.kafkapatterns.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageEnvelopeTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // AC: MessageEnvelope<K, V> is a generic, serializable Java class

    @Test
    void isGenericOverKeyAndPayload() {
        MessageEnvelope<String, OrderPlaced> envelope = new MessageEnvelope<>("order-1", new OrderPlaced("order-1", 42.00));
        assertEquals("order-1", envelope.getKey());
        assertInstanceOf(OrderPlaced.class, envelope.getPayload());
    }

    @Test
    void isGenericWithIntegerKey() {
        MessageEnvelope<Integer, String> envelope = new MessageEnvelope<>(99, "hello");
        assertEquals(99, envelope.getKey());
        assertEquals("hello", envelope.getPayload());
    }

    // AC: Auto-populated fields are set on construction with no caller input required

    @Test
    void autoFieldsSetOnConstruction() {
        Instant before = Instant.now();
        MessageEnvelope<String, String> envelope = new MessageEnvelope<>("k", "v");
        Instant after = Instant.now();

        assertNotNull(envelope.getMessageId(), "messageId must be set");
        assertNotNull(envelope.getConversationId(), "conversationId must be set");
        assertNotNull(envelope.getSentTime(), "sentTime must be set");
        assertFalse(envelope.getSentTime().isBefore(before), "sentTime must not be before construction");
        assertFalse(envelope.getSentTime().isAfter(after), "sentTime must not be after construction");
        assertNotNull(envelope.getHost(), "host must be set");
        assertNotNull(envelope.getMessageType(), "messageType must be set");
        assertNotNull(envelope.getHeaders(), "headers must be initialized");
    }

    @Test
    void eachEnvelopeGetsUniqueMessageId() {
        MessageEnvelope<String, String> a = new MessageEnvelope<>("k", "v");
        MessageEnvelope<String, String> b = new MessageEnvelope<>("k", "v");
        assertNotEquals(a.getMessageId(), b.getMessageId());
    }

    @Test
    void messageTypeContainsPayloadUrn() {
        MessageEnvelope<String, OrderPlaced> envelope = new MessageEnvelope<>("k", new OrderPlaced("o", 1.0));
        assertFalse(envelope.getMessageType().isEmpty());
        assertTrue(envelope.getMessageType().get(0).startsWith("urn:message:"));
        assertTrue(envelope.getMessageType().get(0).contains("OrderPlaced"));
    }

    // AC: Null partition key is handled gracefully

    @Test
    void nullKeyIsAccepted() {
        assertDoesNotThrow(() -> {
            MessageEnvelope<String, String> envelope = new MessageEnvelope<>(null, "payload");
            assertNull(envelope.getKey());
        });
    }

    // AC: Serialization roundtrip — all header fields populated

    @Test
    void serializationRoundtripAllFields() throws Exception {
        MessageEnvelope<String, OrderPlaced> envelope = new MessageEnvelope<>("order-1", new OrderPlaced("order-1", 99.99));
        envelope.setCorrelationId(UUID.randomUUID().toString());
        envelope.setRequestId(UUID.randomUUID().toString());
        envelope.setDestinationAddress("orders-topic");
        envelope.setResponseAddress("replies-topic");
        envelope.setFaultAddress("faults-topic");
        envelope.setSourceAddress("origin-topic/0");
        envelope.setExpirationTime(Instant.now().plusSeconds(3600));
        envelope.setHeaders(Map.of("x-tenant", "acme", "x-version", "2"));

        String json = mapper.writeValueAsString(envelope);
        @SuppressWarnings("unchecked")
        MessageEnvelope<String, OrderPlaced> restored = mapper.readValue(
                json,
                mapper.getTypeFactory().constructParametricType(
                        MessageEnvelope.class, String.class, OrderPlaced.class));

        assertEquals(envelope.getMessageId(), restored.getMessageId());
        assertEquals(envelope.getConversationId(), restored.getConversationId());
        assertEquals(envelope.getSentTime().toEpochMilli(), restored.getSentTime().toEpochMilli());
        assertEquals(envelope.getHost(), restored.getHost());
        assertEquals(envelope.getMessageType(), restored.getMessageType());
        assertEquals(envelope.getCorrelationId(), restored.getCorrelationId());
        assertEquals(envelope.getRequestId(), restored.getRequestId());
        assertEquals(envelope.getDestinationAddress(), restored.getDestinationAddress());
        assertEquals(envelope.getResponseAddress(), restored.getResponseAddress());
        assertEquals(envelope.getFaultAddress(), restored.getFaultAddress());
        assertEquals(envelope.getSourceAddress(), restored.getSourceAddress());
        assertEquals(envelope.getExpirationTime(), restored.getExpirationTime());
        assertEquals(envelope.getHeaders(), restored.getHeaders());
        assertEquals(envelope.getKey(), restored.getKey());
        assertEquals(envelope.getPayload().orderId(), restored.getPayload().orderId());
        assertEquals(envelope.getPayload().amount(), restored.getPayload().amount());
    }

    // AC: Serialization roundtrip — only required (Auto) fields set

    @Test
    void serializationRoundtripAutoFieldsOnly() throws Exception {
        MessageEnvelope<String, OrderPlaced> envelope = new MessageEnvelope<>("order-2", new OrderPlaced("order-2", 1.00));

        String json = mapper.writeValueAsString(envelope);
        @SuppressWarnings("unchecked")
        MessageEnvelope<String, OrderPlaced> restored = mapper.readValue(
                json,
                mapper.getTypeFactory().constructParametricType(
                        MessageEnvelope.class, String.class, OrderPlaced.class));

        assertEquals(envelope.getMessageId(), restored.getMessageId());
        assertEquals(envelope.getConversationId(), restored.getConversationId());
        assertEquals(envelope.getHost(), restored.getHost());
        assertNull(restored.getCorrelationId());
        assertNull(restored.getRequestId());
        assertNull(restored.getFaultAddress());
        assertNull(restored.getExpirationTime());
    }

    // Fixture payload record
    record OrderPlaced(String orderId, double amount) {}
}
