# Plan: Fan-In Pattern

## Status
`draft`

## Problem
Consolidate multiple source topics into a single destination topic using Kafka Streams. Source topics follow the naming convention `<entity>.<operation>` (e.g. `order.created`, `order.deleted`, `order.updated`). The destination topic is `<entity>`.

Each incoming message must be wrapped in a `MessageEnvelope<K, V>` where:
- `K` is the entity id, extracted from the source message — this drives partition assignment on the destination topic
- `V` is the original message payload, unchanged
- The operation type (`created`, `updated`, `deleted`, etc.) is set in the envelope `MessageType` header, derived from the source topic name suffix

All source and destination topics are supplied via config — nothing is hardcoded.

## Approach
Create `src/fan-in-pattern/` as a self-contained Gradle project. It depends on `src/common/` for `MessageEnvelope<K, V>` — that pattern must be implemented first.

**Topology:** multiple source nodes (one per configured source topic) → `KStream.merge()` → envelope construction → sink to destination topic.

**Config:** application properties file (`application.properties`) loaded at startup. Required keys:
- `fan-in.source-topics` — comma-separated list of source topics
- `fan-in.destination-topic` — destination topic name
- `kafka.bootstrap-servers` — broker connection string

**Topic auto-creation:** on startup, use the Kafka admin client to create the destination topic if it does not exist. Partition count and replication factor are configurable. Note: requires broker-side `auto.create.topics.enable=false` to be the norm — the app handles creation explicitly.

**Error handling:**
- Null or missing entity id → log a warning and route to a dead-letter topic (`<destination>.dlq`)
- Unrecognised source topic suffix → log a warning and route to the dead-letter topic
- Deserialization failure → log and skip, do not crash the stream

**Runnable:** Dockerfile builds the app and connects to the Docker Compose Kafka broker from the project root.

## Acceptance criteria
- [ ] All messages from configured source topics appear in the destination topic with no loss
- [ ] Partition key on the destination topic is the entity id from the envelope key
- [ ] `MessageType` header in the envelope reflects the operation type derived from the source topic suffix
- [ ] Source and destination topics are fully config-driven — no hardcoded topic names
- [ ] Destination topic is created on startup if it does not exist (partition count and replication factor configurable)
- [ ] Message with a null or missing entity id is routed to the dead-letter topic and does not crash the stream
- [ ] Message from an unrecognised source topic suffix is routed to the dead-letter topic
- [ ] End-to-end test: produce messages to multiple source topics, assert all appear in the destination topic with correct `MessageType` headers
- [ ] App is runnable end-to-end with a single command (`docker-compose up` or `./gradlew run`)
- [ ] README explains the problem, when to use this pattern, and how to run it

## Out of scope
- Schema Registry / Avro serialization (messages are JSON via Jackson, matching the common package)
- Exactly-once semantics (at-least-once delivery is acceptable for this pattern)
- Multi-entity fan-in (all source topics are for a single entity type per deployment)
- Monitoring, metrics, and alerting

## Notes
- Depends on the Message Envelope pattern (`src/common/`) being implemented first.
- Operation type is inferred from the source topic name suffix (the part after the last `.`). If the naming convention changes, this derivation logic is the single point to update.
- Kafka Streams `KStream.merge()` does not guarantee ordering across source topics — document this in the README as a known trade-off.