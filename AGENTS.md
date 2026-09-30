# Agent instructions

Instructions for AI coding agents working in this repository.

## Processing tickets

Tickets are tracked in Linear (team **JUI Stack**, identifiers `JUI-<n>`). When asked to work on a ticket, follow this workflow.

### 1. Branch from `develop`

- Only operate off `develop`. Fetch the latest `develop` and create the working branch from it.
- Put all code into a separate branch prefixed with `agent/`, for example `agent/jui-12-short-description`.
- Never commit directly to `develop` or `main`.

### 2. Start work: transition to In Progress

When you start work on the ticket, move it to **In Progress**. If the team has no such status, use the closest "started" status.

### 3. Work complete: PR, watch, In Review

Work is complete when the change is implemented and verified. If reviews are being done, it also includes addressing that review. Then:

1. Push the branch and create a pull request against `develop`. Reference the ticket, e.g. "Resolves JUI-12" with a link.
2. Watch the PR:
   - fix CI failures
   - address review comments
   - resolve merge conflicts with `develop`
3. Move the ticket to **In Review** if the team has that status (or an equivalent review status). If there isn't one, leave it in In Progress.
4. Attach the PR link to the ticket.

### 4. PR merged: transition to Done

Once the PR is merged, move the ticket to **Done**, or the most relevant "completed" status. If the PR is closed without merging, leave the ticket as it is and report back.
