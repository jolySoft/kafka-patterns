# Prompt: Implement Message Envelope Pattern

## When to use
When you want to implement the Message Envelope pattern using the TDD Java Engineer agent.
Run this once — the output is `src/common/` which other patterns depend on.

## Context to provide
- Plan: `plans/messageEnvelopePattern.plan.md`
- No existing source code to build on — this is greenfield

## Prompt

```
You are acting as the TDD Java Engineer agent defined in `agents/tddJavaEngineer.agent.md`.

Implement the following plan in full, following every step in the agent definition.

Plan file: `plans/messageEnvelopePattern.plan.md`

Key implementation notes:
- The output directory is `src/common/` — this is an explicit exception to the no-shared-utils convention because other patterns depend on this envelope
- The pattern is `MessageEnvelope<K, V>` — a generic, serializable Java class where K is the partition key type and V is the payload type
- Serialization: JSON using Jackson
- Java version: 21
- Build tool: Gradle
- Test framework: JUnit 5

Before writing any code, read and validate the plan:
- Confirm status is `in-progress` (update it if it is still `draft`)
- Confirm all acceptance criteria are concrete and testable

Then proceed through the agent steps in order:
1. Create branch `message-envelope-pattern`
2. Scaffold `src/common/` with a Gradle build
3. TDD loop for each acceptance criterion — commit after each one passes
4. Final code review and cleanup
5. Write `src/common/README.md`
6. Update `plans/messageEnvelopePattern.md` status to `done` and tick all criteria
7. Push and open a PR targeting `main`
```

## Example usage
```
claude -p "$(cat prompts/implementMessageEnvelopePattern.md)"
```
First update the plan status to `in-progress` before running, or the agent will halt.