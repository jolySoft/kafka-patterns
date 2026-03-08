# Agent: [Agent Name]

## Purpose
What does this agent automate? What would otherwise be manual steps?

## Trigger
When/how is this agent invoked? (manually, on a schedule, on file change, etc.)

## Steps
1. Step one — what Claude does
2. Step two — what Claude does
3. ...

## Inputs
- What files or data does the agent read?
- What parameters does it accept?

## Outputs
- What files does it create or modify?
- What side effects does it have?

## Script
```bash
# Example: shell script that chains claude -p calls
# claude -p "$(cat prompts/add-pattern.md)" --input "pattern_name=dead-letter-queue"
```

## Notes
Gotchas, failure modes, how to recover.