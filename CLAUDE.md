# kafka-patterns

## Project purpose
A reference library of Kafka patterns — production-ready examples covering common messaging, streaming, and integration scenarios.

## Tech stack
- **Language:** Java
- **Kafka client:** confluent-kafka
- **Kafka version:** 3.x
- **Infrastructure:** Docker Compose confluentinc/confluent-local

## Project structure
```
plans/     feature specs and task breakdowns — read these before starting work
prompts/   reusable prompt templates for recurring tasks
agents/    scripts that orchestrate multi-step automated workflows
src/       source code, organised by pattern category
```

## Coding conventions
- One pattern per directory under `src/`, self-contained and runnable
- Each pattern has its own README explaining the problem it solves
- Prefer idiomatic code over clever code — these are reference examples
- No shared utility libraries between patterns — keep them standalone

## How to work
- Before implementing a pattern, check `plans/` for a spec
- After implementing, update the plan file to mark it done
- Keep patterns runnable with a single command (e.g. `make run` or `docker-compose up`)

## Domain context
Key Kafka concepts relevant to this project:
- Consumer groups and partition assignment
- Exactly-once semantics (EOS) vs at-least-once
- Schema Registry and Avro/Protobuf schemas
- Kafka Streams vs consumer API
- Dead letter queues and error handling patterns

## Out of scope
- Production infrastructure
- Managed Kafka (Confluent Cloud, MSK) specifics