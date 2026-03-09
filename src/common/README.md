# common — Message Envelope

## What problem does this solve?

Producers and consumers often need more than just a raw payload. They need to know *who* sent a message, *when*, *why*, and how to correlate it with other messages in the same flow. Without a standard structure, each team invents their own header conventions, making cross-service tracing and routing inconsistent.

`MessageEnvelope<K, V>` gives every Kafka message a uniform identity and traceability layer — wrapping any typed payload with a standard set of metadata headers — without coupling producers or consumers to a specific schema.

## When to use it

Use `MessageEnvelope` as the value type for your `ProducerRecord` when you need:

- **Distributed tracing** — correlate messages across services via `CorrelationId` and `ConversationId`
- **Request/response flows** — route replies back via `ResponseAddress`
- **Dead-letter handling** — direct faults to a specific topic via `FaultAddress`
- **Audit trails** — know who sent what, from where, and when

## Header field reference

| Field | Type | Set by | Description |
|---|---|---|---|
| `MessageId` | UUID | Auto | Unique ID for this message instance |
| `ConversationId` | String | Auto | Groups all messages in one logical conversation |
| `SentTime` | Instant | Auto | UTC timestamp when the envelope was created |
| `Host` | String | Auto | Hostname of the producing machine |
| `MessageType` | List\<String\> | Auto | URN(s) identifying the payload type(s) |
| `CorrelationId` | String | User | Identifies the operation or event this message belongs to |
| `FaultAddress` | String | User | Topic to route consumer faults to |
| `ExpirationTime` | Instant | User | When the message should be considered stale |
| `Headers` | Map\<String,String\> | User | Arbitrary extension headers |
| `RequestId` | String | Request | Set by the request client; copied to responses |
| `DestinationAddress` | String | Request | The target topic |
| `ResponseAddress` | String | Request | Where to send the reply |

**Type legend:** `Auto` = set by envelope infrastructure; `User` = set by the application; `Request` = set for request/response flows only.

## How to run

```bash
./gradlew test
```

## Using this in another pattern

Add the `common` project as a dependency in the sibling pattern's `settings.gradle`:

```groovy
includeBuild '../../common'
```

And in its `build.gradle`:

```groovy
dependencies {
    implementation 'com.kafkapatterns:common:1.0-SNAPSHOT'
}
```

## Extending to Avro / Schema Registry (production)

This implementation uses Jackson JSON to keep the pattern self-contained and readable. In production you would typically:

1. Define the envelope as an Avro schema (`.avsc`) registered with Confluent Schema Registry
2. Replace the Jackson `ObjectMapper` serializer/deserializer with `KafkaAvroSerializer` / `KafkaAvroDeserializer`
3. Configure the `schema.registry.url` in your producer/consumer properties

The field structure maps directly to Avro record fields; `Instant` fields become `long` (epoch millis) with `logicalType: timestamp-millis`.

## Note on the no-shared-utils convention

This `common/` directory is an explicit exception to the project's rule that each pattern is standalone. The message envelope is a foundational data structure that subsequent patterns depend on. Its role is purely structural — it contains no routing or delivery behaviour of its own.
