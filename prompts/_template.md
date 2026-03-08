# Prompt: [Task Name]

## When to use
Describe the recurring task this prompt handles.

## Context to provide
- Link to the relevant plan: `plans/[name].md`
- Any existing code to build on: `src/[pattern]/`

## Prompt

```
[Paste the reusable prompt text here. Use {{PLACEHOLDERS}} for values you fill in each time.]

Context:
- Pattern name: {{PATTERN_NAME}}
- Plan: [paste plan content or reference the file]

Task:
[Describe the task clearly]

Constraints:
- Follow the conventions in CLAUDE.md
- Keep the pattern self-contained under src/{{PATTERN_NAME}}/
- Include a README.md for the pattern
```

## Example usage
How you've used this prompt and what worked well.