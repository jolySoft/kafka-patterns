# Plan: Message Envelope Pattern

## Status
`done`

## Problem
Create a message envelope pattern that wraps a Kafka message payload with a standard set of metadata headers. This gives every message a consistent identity, traceability, and routing context without coupling producers and consumers to a specific schema.

The envelope header contains the following properties:

| Property | Type | Description |
| ------------ | ------------ | ------------- |
| MessageId | Auto | Generated for each message using UUID.randomUUID(). |
| CorrelationId | User | Assigned by the application, or automatically by convention, and should uniquely identify the operation, event, etc. |
| RequestId | Request | Assigned by the request client, and automatically copied by the Respond methods to correlate responses to the original request. |
| InitiatorId | Auto | Assigned when publishing or sending from a consumer, saga, or activity to the value of the CorrelationId on the consumed message. |
| ConversationId | Auto | Assigned when the first message is sent or published and no consumed message is available, ensuring that a set of messages within the same conversation have the same identifier. |
| SourceAddress | Auto | Where the message originated (e.g. the topic and partition it was produced to). |
| DestinationAddress | Auto | Where the message was sent. |
| ResponseAddress | Request | Where responses to the request should be sent. If not present, responses are published. |
| FaultAddress | User | Where consumer faults should be sent. If not present, faults are published. |
| ExpirationTime | User | When the message should expire, which may be used by the transport to remove the message if it isn't consumed by the expiration time. |
| SentTime | Auto | When the message was sent, in UTC. |
| MessageType | Auto | An array of message types, in a MessageUrn format, which can be deserialized. |
| Host | Auto | The host information of the machine that sent or published the message. |
| Headers | User | Additional headers, which can be added by the user, middleware, or diagnostic trace filters. |

**Type legend:** `Auto` = set by the envelope infrastructure; `User` = set by the application; `Request` = set for request/response flows only.

The body of the message is made up of a partition key and a typed payload, as is standard for Kafka.

## Approach
Create a dedicated `src/common/` directory for this pattern. It is an explicit exception to the no-shared-utils convention — it is a foundational data structure that other patterns depend on, and its README must make that role clear.

The envelope is a generic Java class `MessageEnvelope<K, V>` where `K` is the partition key type and `V` is the payload type. It must be serializable and deserializable.

Serialization format: JSON (using Jackson) to keep the pattern self-contained and readable without a Schema Registry dependency. Add a note in the README about how to swap in Avro/Protobuf with Schema Registry if needed in production.

The envelope sits inside the Kafka `ProducerRecord` value — the key field of the envelope is also used as the `ProducerRecord` key to drive partitioning.

Auto-populated fields (MessageId, SentTime, Host, etc.) are set in the constructor. User and Request fields default to null and are set via builder or setters.

## Acceptance criteria
- [x] `MessageEnvelope<K, V>` is a generic, serializable Java class
- [x] Auto-populated fields are set on construction with no caller input required
- [x] Null partition key is handled gracefully (falls back to null key in `ProducerRecord`, Kafka round-robins)
- [x] Serialization roundtrip test: envelope with all header fields populated deserializes to an equal object
- [x] Serialization roundtrip test: envelope with only required (Auto) fields set
- [x] `common/` package builds and can be imported by a sibling pattern module
- [x] README explains the pattern, the Type legend, and how to extend it with Avro/Schema Registry

## Out of scope
- Usage of this envelope within a specific messaging pattern (covered by subsequent pattern plans)
- Schema Registry integration (noted in README as a production extension)

## Notes
The header design is inspired by MassTransit's message envelope convention, adapted for Kafka. MassTransit targets .NET; references to `NewId.NextGuid` and `IBus` do not apply here — use `UUID.randomUUID()` and standard Kafka producer APIs instead.

This is intentionally a simple data structure with no routing or delivery behaviour of its own.