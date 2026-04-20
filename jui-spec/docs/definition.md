# Interaction Specification Definition

## Purpose

This document defines a textual specification for describing interaction diagrams. The specification is intended to:

1. Be writable by a technically oriented human.
2. Be structured enough for validation by an LLM or deterministic tooling.
3. Be transcribable into a diagram-oriented language without mixing semantic interaction concerns with rendering concerns.

The model sits somewhere between a workflow definition, a UML robustness diagram, and a Canonical Abstract Prototype. The primary abstraction is the **Interaction Unit (IU)**, which represents a user-visible interaction state.

## Design Principles

The specification is based on the following principles:

- The IU is the center of the model.
- The specification captures interaction semantics, not visual layout.
- Transitions between IUs occur through explicit activities.
- Conditions, constraints, and rules are first-class constructs.
- The base language should be stable and diagram-neutral.
- Diagram generation should be a downstream transcription step.

## Core Concepts

### Interaction Unit

An **Interaction Unit** is a coherent interaction state in which information is presented, information may be captured from the user, and actions may be initiated.

An IU typically corresponds to a screen, page, dialog, panel state, step in a wizard, or other stable interaction boundary.

Each IU may define:

- `info`: information blocks presented to the user.
- `fields`: user input fields.
- `actions`: user-triggered actions such as buttons or links.
- `units`: represented units such as lists, tables, cards, or repeated item groups.
- `rules`: rules, constraints, preconditions, postconditions, or invariants relevant to the IU.

### Information Block

An **information block** is read-only information shown to the user, for example:

- name
- address
- email
- premium amount
- warning message

Information blocks may be grouped semantically but are not inherently layout elements.

### Field

A **field** captures information from the user. A field may carry:

- type
- requiredness
- constraints
- default value
- visibility conditions

### Action

An **action** represents an interaction the user may initiate from an IU, such as:

- submit
- continue
- cancel
- view details
- navigate to another IU

Actions are distinct from transitions. A transition is defined in the flow model and references an action as its trigger where relevant.

### Represented Unit

A **represented unit** is a structured sub-unit shown within an IU, such as:

- a list of policies
- a table of claims
- a set of payment options
- a repeated group of household members

Represented units may themselves contain information blocks, fields, and actions, but for this specification they are described as contained structures of the parent IU rather than separate IUs unless they define an independent interaction state.

### Activity

An **activity** is the processing step that sits between IUs in a flow. It usually represents server-side or application-side behavior, for example:

- validate details
- create quote
- load policy summary
- persist application
- route decision

Activities are important for diagram transcription because they make explicit that movement from one IU to another is mediated by system behavior.

### Flow

A **flow** defines the transition structure between IUs. A flow:

- starts at a declared start IU or terminal start point
- moves from an IU through an activity
- may branch based on conditions
- ends at another IU or a terminal end point

### Terminal Point

A **terminal point** represents start or end outside the IU model itself. Typical terminals are:

- `start`
- `end`
- `cancelled`
- `completed`
- `rejected`

## Separation of Concerns

The specification intentionally separates three concerns:

### 1. Interaction semantics

This defines what the user sees, enters, and does, and what system activities occur.

### 2. Validation semantics

This defines constraints, rules, and structural expectations so the model can be checked by an LLM or by code.

### 3. Diagram transcription

This defines how the semantic model is rendered into a diagram language. Diagram concerns may include:

- node styling
- swim lanes
- edge routing
- grouping
- labels optimized for display

These are not part of the core interaction specification, though optional transcription hints may be added separately.

## Document Structure

An interaction specification document contains these top-level sections:

1. `spec`
2. `vocabulary` (optional)
3. `terminals`
4. `interaction_units`
5. `activities`
6. `flows`

Recommended section order is fixed to simplify human reading and machine validation.

## Canonical Textual Form

The canonical form is a YAML-like structured text. JSON could be used as an alternative serialization, but the human-authoring form should prefer the structure below.

### Skeleton

```yaml
spec:
  id: quote-journey
  name: Quote Journey
  version: 1.0
  description: Create a quote from applicant details.

vocabulary:
  policy: Insurance product selected by the applicant.
  quote: Indicative price offered by the system.

terminals:
  - id: start
    type: start
  - id: completed
    type: end

interaction_units:
  - id: applicant_details
    name: Applicant Details
    info: []
    fields: []
    actions: []
    units: []
    rules: []

activities:
  - id: validate_applicant
    name: Validate Applicant

flows:
  - from: start
    to: applicant_details
  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    to: completed
```

## Top-Level Sections

### `spec`

Metadata for the definition.

Required properties:

- `id`
- `name`
- `version`

Optional properties:

- `description`
- `status`
- `authors`
- `notes`

### `vocabulary`

Optional domain glossary used to make the specification unambiguous. This is particularly useful when LLM validation is expected.

### `terminals`

Declares terminal points.

Required properties:

- `id`
- `type`: `start` or `end`

Optional properties:

- `name`
- `description`

### `interaction_units`

Declares all IUs in the model.

Each IU has:

- `id`: stable identifier
- `name`: human-readable title

Optional properties:

- `description`
- `info`
- `fields`
- `actions`
- `units`
- `rules`
- `entry_conditions`
- `exit_conditions`

### `activities`

Declares intermediate processing steps used by flows.

Required properties:

- `id`
- `name`

Optional properties:

- `description`
- `kind`: for example `validation`, `load`, `persist`, `decision`, `integration`
- `rules`

### `flows`

Declares transitions.

Required properties:

- `from`
- `to`

Optional properties:

- `trigger`
- `via`
- `condition`
- `outcome`
- `description`

## IU Content Model

### `info`

`info` is a list of information blocks.

Structure:

```yaml
info:
  - id: applicant_name
    label: Applicant Name
    content: Current applicant full name
```

Recommended properties:

- `id`
- `label`

Optional properties:

- `content`
- `source`
- `condition`

### `fields`

`fields` is a list of user input fields.

Structure:

```yaml
fields:
  - id: email
    label: Email
    type: email
    required: true
    constraints:
      - must be a valid email address
```

Recommended properties:

- `id`
- `label`
- `type`

Optional properties:

- `required`
- `default`
- `constraints`
- `condition`
- `example`

### `actions`

`actions` is a list of user-initiated actions.

Structure:

```yaml
actions:
  - id: submit_details
    label: Submit Details
    kind: primary
```

Recommended properties:

- `id`
- `label`

Optional properties:

- `kind`
- `condition`
- `effect`

### `units`

`units` is a list of represented units embedded in the IU.

Structure:

```yaml
units:
  - id: policy_options
    label: Policy Options
    kind: list
    contains:
      info:
        - id: premium
          label: Premium
      actions:
        - id: select_policy
          label: Select
```

Recommended properties:

- `id`
- `label`
- `kind`

Optional properties:

- `contains`
- `selection`
- `condition`

### `rules`

Rules capture conditions, constraints, and statements that govern behavior.

Structure:

```yaml
rules:
  - id: applicant_must_be_adult
    rule: Applicant age must be 18 or greater.
    kind: constraint
```

Recommended properties:

- `id`
- `rule`

Optional properties:

- `kind`: for example `constraint`, `precondition`, `postcondition`, `business_rule`, `validation_rule`
- `condition`

## Flow Model

The minimum meaningful flow patterns are:

1. Terminal to IU
2. IU to IU through an activity
3. IU to terminal
4. IU to activity to multiple destinations under different conditions

### Direct entry

```yaml
- from: start
  to: applicant_details
```

### Triggered transition through an activity

```yaml
- from: applicant_details
  trigger: submit_details
  via: validate_applicant
  to: eligibility_result
```

### Conditional branching

```yaml
- from: eligibility_result
  via: route_outcome
  condition: eligible
  to: quote_summary

- from: eligibility_result
  via: route_outcome
  condition: not eligible
  to: declined
```

### End transition

```yaml
- from: quote_summary
  trigger: accept_quote
  via: create_policy
  to: completed
```

## Validation Expectations

The language should be simple enough that both a human reviewer and an LLM can validate it. A valid specification should satisfy the following rules.

### Structural Rules

- `spec.id`, `spec.name`, and `spec.version` must exist.
- Every declared `id` must be unique within its section.
- Every `flow.from` and `flow.to` must reference either an IU or a terminal.
- Every `flow.via` must reference a declared activity when present.
- Every `flow.trigger` should reference an action defined on the source IU or on a represented unit contained by that IU.
- At least one start terminal must exist.
- At least one end terminal or terminal outcome should exist.
- At least one flow must start from a start terminal.

### Behavioral Rules

- An IU should only define actions that have a clear purpose in a flow or in local IU behavior.
- A branching flow should use mutually intelligible conditions.
- Rules should be declarative statements, not implementation code.
- Activities should describe system behavior, not UI behavior.
- Represented units should only be modeled as separate IUs when they have an independent interaction state.

### LLM-Friendly Authoring Rules

- Use stable, explicit identifiers.
- Use short, declarative sentences.
- Prefer one business rule per rule entry.
- Avoid pronouns when an explicit noun is available.
- Keep conditions textually precise, for example `applicant is eligible` rather than `success`.

## Diagram Transcription Model

The interaction specification is not itself a diagram language. Instead it should be transcribed into one.

A transcriber can apply the following mapping:

- terminal -> start/end node
- interaction unit -> interface/state node
- activity -> processing node
- flow -> edge
- flow condition -> branch label
- flow trigger -> edge label
- rules -> annotations, notes, or validation overlays
- represented unit -> embedded IU detail or collapsed note depending on diagram density

This allows one semantic definition to support multiple diagram outputs:

- compact interaction overview
- detailed robustness-style interaction diagram
- UI flow with system activities

## Self-Contained Presentation Language

To support embedding in Markdown in the style of Mermaid or PlantUML, a presentation language can be defined as a **self-contained diagram block**. Unlike the core interaction specification, a presentation block must not assume that any external source document is available at render time.

This means:

- a presentation block must contain all semantic elements needed to render the requested diagram
- a presentation block may contain the complete interaction model or a self-sufficient subset
- a renderer must be able to parse and render the block in isolation

The presentation language is therefore not just a view selector over an external specification. It is a compact, renderable package containing:

- embedded interaction semantics
- view instructions
- optional rendering hints

### Design Intent

The core interaction specification remains the canonical authoring format for broader analysis and traceability. The presentation language is a packaging and rendering format intended for:

- Markdown embedding
- documentation pages
- IDE previews
- browser rendering
- Java-based offline image generation

### Relationship to the Core Specification

The presentation language should reuse the same concepts:

- terminals
- interaction units
- activities
- flows
- rules

However, it should be optimized for embedding and rendering rather than full-document completeness. A presentation block may therefore include only the subset needed for a given diagram, as long as that subset is structurally closed.

### Structural Closure

A presentation block is **structurally closed** when:

- every referenced IU is declared in the block
- every referenced terminal is declared in the block
- every referenced activity is declared in the block
- every referenced trigger action is declared on the source IU or represented unit in the block
- every rule shown in the rendered diagram is declared in the block

Structural closure is the key property that makes standalone rendering possible.

### Markdown Embedding Form

A fenced code block is the intended embedding form.

Example:

````markdown
```interaction
diagram:
  id: quote-overview
  title: Quote Request Overview
  view: flow

terminals:
  - id: start
    type: start
  - id: completed
    type: end
  - id: declined
    type: end

interaction_units:
  - id: applicant_details
    name: Applicant Details
    actions:
      - id: submit_details
        label: Get Quote
  - id: quote_summary
    name: Quote Summary
    actions:
      - id: accept_quote
        label: Accept Quote
      - id: cancel
        label: Cancel

activities:
  - id: validate_applicant
    name: Validate Applicant
  - id: create_policy
    name: Create Policy

flows:
  - from: start
    to: applicant_details
  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    condition: applicant details are valid
    to: quote_summary
  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    condition: applicant is not eligible
    to: declined
  - from: quote_summary
    trigger: accept_quote
    via: create_policy
    to: completed
  - from: quote_summary
    trigger: cancel
    to: declined
```
````

### Top-Level Sections of a Presentation Block

The recommended top-level sections are:

1. `diagram`
2. `terminals`
3. `interaction_units`
4. `activities`
5. `flows`
6. `presentation` (optional)

### `diagram`

`diagram` defines the identity and purpose of the block.

Required properties:

- `id`
- `view`

Optional properties:

- `title`
- `description`
- `version`

Suggested values for `view`:

- `flow`
- `interaction_unit`
- `subgraph`
- `robustness`

### `presentation`

`presentation` contains optional hints that affect rendering but not semantic meaning.

Example:

```yaml
presentation:
  style: compact
  direction: left-to-right
  show_rules: true
  show_actions: true
  collapse_units: false
```

Typical properties may include:

- `style`
- `direction`
- `show_rules`
- `show_actions`
- `show_fields`
- `show_info`
- `collapse_units`
- `theme`

These hints are renderer-facing and must not alter the underlying interaction semantics.

### `layout`

`layout` contains optional structural hints for diagram arrangement. These hints are still presentation-only, but they are more specific than visual style and are intended to guide formal layout algorithms.

The expected baseline rendering model is:

- layered placement for nodes
- crossing minimisation within layers
- orthogonal or semi-orthogonal routing for edges
- optional constraint handling from author-supplied hints

Example:

```yaml
layout:
  algorithm: layered
  direction: left-to-right
  routing: orthogonal
  crossing_minimisation: true
  hints:
    - place: update_customer
      between: [edit_customer, customer_summary]
    - route: update_fails
      style: outer
    - route: update_succeeds
      style: upper
```

Recommended properties:

- `algorithm`: normally `layered`
- `direction`: for example `left-to-right` or `top-to-bottom`
- `routing`: for example `orthogonal` or `curved`
- `crossing_minimisation`: boolean
- `hints`: list of optional placement and routing hints

The renderer may ignore unsupported hints, but it must do so without changing the semantic interpretation of the diagram.

### Layout Hints

Layout hints should be advisory rather than absolute. They are intended to influence the renderer when the default layout is not sufficiently expressive.

Useful hint categories include:

- preferred rank or layer
- relative position to another node
- preferred grouping
- preferred routing style for a flow
- emphasis of forward, return, success, or error paths

Example:

```yaml
layout:
  algorithm: layered
  direction: left-to-right
  routing: orthogonal
  hints:
    - node: customer_summary
      rank: 1
    - node: edit_customer
      rank: 2
    - node: update_customer
      rank: 3
      align_with: edit_customer
    - flow:
        from: edit_customer
        trigger: update
        condition: update succeeds
      route: upper
    - flow:
        from: edit_customer
        trigger: update
        condition: update fails
      route: lower
      style: outer
```

### Rank and Ordering Hints

For layered layout, the most useful hint is usually rank placement.

Recommended node-level hints:

- `rank`: preferred layer index
- `before`: prefer ordering before another node in the same rank
- `after`: prefer ordering after another node in the same rank
- `align_with`: prefer alignment with another node
- `between`: prefer placement between two nodes

These hints should not require exact coordinates. The renderer should preserve freedom to optimise readability.

### Flow Routing Hints

Flows may also carry routing hints. These are especially useful for interaction diagrams because success, return, and error paths often have recognizable presentation patterns.

Recommended flow-level hints:

- `route`: for example `direct`, `upper`, `lower`, `outer`
- `kind`: for example `forward`, `return`, `success`, `error`
- `attach_from`: preferred attachment zone on the source node
- `attach_to`: preferred attachment zone on the target node

Example:

```yaml
flows:
  - from: customer_summary
    trigger: edit
    to: edit_customer
    layout:
      kind: forward
      route: direct

  - from: edit_customer
    trigger: return
    to: customer_summary
    layout:
      kind: return
      route: upper

  - from: edit_customer
    trigger: update
    via: update_customer
    condition: update fails
    to: edit_customer
    layout:
      kind: error
      route: lower
      style: outer
```

### Separation from Semantic Meaning

Layout hints must not change the meaning of the interaction model.

For example:

- `route: lower` does not mean failure unless the flow semantics say so
- `rank: 2` does not imply chronological ordering unless the flows say so
- `align_with` does not imply equivalence or grouping in the domain model

This is important because the same self-contained block should remain semantically valid even if rendered by a tool that ignores layout hints entirely.

### Minimal Self-Contained Block

The smallest meaningful block is a flow with terminals, at least one IU, and any referenced activities.

```yaml
diagram:
  id: simple-navigation
  view: flow

terminals:
  - id: start
    type: start
  - id: end
    type: end

interaction_units:
  - id: home
    name: Home
    actions:
      - id: continue
        label: Continue

activities:
  - id: proceed
    name: Proceed

flows:
  - from: start
    to: home
  - from: home
    trigger: continue
    via: proceed
    to: end
```

### IU Detail View

The same language can render an IU-focused view by embedding only one IU and optionally omitting flows that are not relevant to that view.

```yaml
diagram:
  id: applicant-details-unit
  view: interaction_unit
  title: Applicant Details

interaction_units:
  - id: applicant_details
    name: Applicant Details
    info:
      - id: page_help
        label: Help
        content: Enter applicant details to request a quote.
    fields:
      - id: full_name
        label: Full Name
        type: text
        required: true
      - id: email
        label: Email
        type: email
        required: true
    actions:
      - id: submit_details
        label: Get Quote
    rules:
      - id: adult_only
        kind: validation_rule
        rule: Applicant age must be 18 or greater.

presentation:
  show_info: true
  show_fields: true
  show_actions: true
  show_rules: true
```

### Subgraph View

A presentation block may represent only a focused slice of a larger interaction model. This is valid as long as the block is self-contained and structurally closed.

```yaml
diagram:
  id: quote-decision-subgraph
  view: subgraph
  title: Quote Decision

terminals:
  - id: declined
    type: end

interaction_units:
  - id: applicant_details
    name: Applicant Details
    actions:
      - id: submit_details
        label: Get Quote
  - id: quote_summary
    name: Quote Summary

activities:
  - id: validate_applicant
    name: Validate Applicant

flows:
  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    condition: applicant details are valid
    to: quote_summary
  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    condition: applicant is not eligible
    to: declined
```

### Validation Rules for Presentation Blocks

A valid presentation block should satisfy the following:

- `diagram.id` and `diagram.view` must exist
- all graph references must resolve within the block
- if `flows` are present, every `from` and `to` must resolve to a declared IU or terminal in the block
- if `via` is present, it must resolve to a declared activity in the block
- if `trigger` is present, it must resolve to an action declared in the source IU or in a represented unit contained by that IU
- a block with `view: flow`, `view: subgraph`, or `view: robustness` should declare enough terminals, IUs, and flows to form a meaningful diagram
- a block with `view: interaction_unit` may omit flows if the IU content alone is the subject of the diagram
- if `layout.hints` are present, every node or flow reference used by a hint must resolve within the block
- unsupported layout hints may be ignored, but invalid references should be reported

### Renderer Pipeline

A renderer for the presentation language should proceed in four stages:

1. Parse the fenced block into a structured model.
2. Validate structural closure and semantic consistency.
3. Transcribe the block into an intermediate diagram model.
4. Render that model to SVG or another image format.

This architecture supports:

- JavaScript rendering in a browser or Markdown preview
- Java generation of SVG
- Java raster generation from SVG or from the intermediate model
- a VS Code preview extension built on the JavaScript renderer

The layout stage will commonly use:

- Sugiyama-style layered layout for primary placement
- crossing minimisation heuristics for node ordering
- orthogonal or semi-orthogonal routing for connectors
- optional constraint handling derived from `layout.hints`

### Why Self-Containment Matters

Self-containment makes the presentation language practical in real documentation workflows:

- Markdown pages remain portable
- copy-and-paste reuse is possible
- IDE previews do not need project-wide resolution
- static site generators can render diagrams without custom document linking
- Java tooling can render diagrams in build pipelines from a single block of text

The trade-off is some duplication between the canonical interaction specification and embedded diagram blocks. That duplication is acceptable because the two artifacts serve different purposes:

- the core specification is the authoritative semantic model
- the presentation block is a portable, renderable excerpt

### Recommended Implementation Order

For implementation, the lowest-risk path is:

1. Define and stabilize the self-contained presentation block syntax.
2. Add a small presentation-only layout hint model.
3. Build a parser and validator for the block format.
4. Build a shared intermediate diagram model.
5. Implement JavaScript SVG rendering.
6. Add a VS Code preview extension using the JavaScript renderer.
7. Implement Java SVG generation.
8. Add optional PNG generation in Java.

## Example 1: Quote Request

```yaml
spec:
  id: quote-request
  name: Quote Request
  version: 1.0
  description: Capture applicant details and produce a quote outcome.

vocabulary:
  applicant: Person requesting insurance cover.
  quote: Indicative offer returned by the pricing service.

terminals:
  - id: start
    type: start
  - id: completed
    type: end
  - id: declined
    type: end

interaction_units:
  - id: applicant_details
    name: Applicant Details
    info:
      - id: page_help
        label: Help
        content: Enter applicant details to request a quote.
    fields:
      - id: full_name
        label: Full Name
        type: text
        required: true
      - id: date_of_birth
        label: Date of Birth
        type: date
        required: true
      - id: email
        label: Email
        type: email
        required: true
    actions:
      - id: submit_details
        label: Get Quote
        kind: primary
    rules:
      - id: adult_only
        kind: validation_rule
        rule: Applicant age must be 18 or greater.

  - id: quote_summary
    name: Quote Summary
    info:
      - id: quoted_premium
        label: Quoted Premium
      - id: cover_type
        label: Cover Type
    actions:
      - id: accept_quote
        label: Accept Quote
        kind: primary
      - id: cancel
        label: Cancel
        kind: secondary

activities:
  - id: validate_applicant
    name: Validate Applicant
    kind: validation
  - id: create_policy
    name: Create Policy
    kind: persist

flows:
  - from: start
    to: applicant_details

  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    condition: applicant details are valid
    to: quote_summary

  - from: applicant_details
    trigger: submit_details
    via: validate_applicant
    condition: applicant is not eligible
    to: declined

  - from: quote_summary
    trigger: accept_quote
    via: create_policy
    to: completed

  - from: quote_summary
    trigger: cancel
    to: declined
```

## Example 2: Search and Select from a Represented Unit

```yaml
spec:
  id: policy-search
  name: Policy Search
  version: 1.0

terminals:
  - id: start
    type: start
  - id: end
    type: end

interaction_units:
  - id: search_policies
    name: Search Policies
    fields:
      - id: search_text
        label: Search
        type: text
    actions:
      - id: run_search
        label: Search
        kind: primary
    units:
      - id: results
        label: Search Results
        kind: list
        contains:
          info:
            - id: policy_name
              label: Policy Name
            - id: policy_status
              label: Policy Status
          actions:
            - id: select_policy
              label: Open
    rules:
      - id: results_only_when_searched
        kind: constraint
        rule: Search Results are shown only after a search has been executed.

  - id: policy_detail
    name: Policy Detail
    info:
      - id: header
        label: Policy Header
    actions:
      - id: finish
        label: Done

activities:
  - id: execute_search
    name: Execute Search
    kind: load
  - id: load_policy
    name: Load Policy
    kind: load

flows:
  - from: start
    to: search_policies

  - from: search_policies
    trigger: run_search
    via: execute_search
    to: search_policies

  - from: search_policies
    trigger: select_policy
    via: load_policy
    to: policy_detail

  - from: policy_detail
    trigger: finish
    to: end
```

## Authoring Guidance

When authoring a specification:

1. Identify the stable interaction states first and model them as IUs.
2. Model user-visible content inside the IU before defining flows.
3. Define actions on the IU, then connect them through activities in flows.
4. Express business rules declaratively in `rules`.
5. Use activities only where application behavior mediates a transition.
6. Add diagram-specific rendering details only in a downstream transcription layer.

## Recommended Future Extensions

The core specification above is intentionally small. The following could be added later without changing the basic model:

- actor roles
- permissions
- error categories
- data binding references
- reusable IU fragments
- transcription hints for diagram generators
- traceability links to use cases or requirements

## Summary

The key modeling idea is that an interaction diagram specification is centered on **Interaction Units** and their **content**, while **flows** connect those units through explicit **activities** and **conditions**. This keeps the specification understandable to humans, checkable by LLMs, and transcribable into a separate diagram language.
