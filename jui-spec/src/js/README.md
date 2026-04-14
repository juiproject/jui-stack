# interaction-diagram.js

Renders interaction diagrams from a JSON (or fenced `\`\`\`interaction` block) model to inline SVG. Designed for embedding in Markdown documentation in the style of Mermaid or PlantUML.

## Public API

```js
InteractionDiagram.parseInteractionDocument(source)  // string -> model object
InteractionDiagram.validateDiagramModel(model)        // throws on invalid references
InteractionDiagram.renderInteractionDiagram(model)    // model -> SVG string
InteractionDiagram.renderInto(container, source)      // convenience: parse + render into a DOM element
```

## Rendering pipeline

The renderer follows a four-stage pipeline:

```
source text
  -> parseInteractionDocument   (JSON or fenced block -> model)
  -> validateDiagramModel       (structural checks)
  -> buildNodes + layout        (Sugiyama layered layout -> x,y positions)
  -> renderInteractionDiagram   (SVG string generation)
```

### 1. Parse & validate

`parseInteractionDocument` accepts either raw JSON or a fenced `` ```interaction `` block (for Markdown embedding). `validateDiagramModel` checks that every flow `from`/`to` resolves to a declared IU, terminal, or activity, and that every `trigger` references an action on the source IU.

### 2. Node building

`buildNodes` creates the internal node list from terminals, interaction units, and activities. Key behaviour:

- **Start terminals are eliminated.** The IU that a start terminal flows into is marked with `isEntry = true` and rendered with a filled circle overlapping the top-left corner. Edges from start terminals are filtered out of the layout graph entirely. End terminals are still rendered as circle nodes.
- **Entry IUs become layout roots.** The Sugiyama algorithm uses entry IUs (not start terminals) as the source nodes for layer assignment.

### 3. Layout algorithm

A Sugiyama-style layered layout in four phases:

#### 3a. DAG construction (`buildAcyclicGraph`)

DFS from root nodes. Back-edges (cycles from return/error flows) are reversed to produce an acyclic graph. The reversed flag is preserved so edge routing knows the original direction.

#### 3b. Layer assignment (`assignLayers`)

Topological-order sweep assigning each node to a layer (column). Respects `column` and `rank` hints from the model. Runs up to 3 fixup passes to enforce that hint-forced columns don't violate the DAG edge ordering.

#### 3c. Layer ordering (`orderLayers`)

Barycenter heuristic with 6 alternating up/down sweep passes. Minimises edge crossings within each layer. Respects `before`/`after` ordering hints.

#### 3d. Coordinate assignment (`assignCoordinates`)

Converts layer/row indices to pixel positions:

- `horizontalSpacing = 360` between columns
- `verticalSpacing = 200` between rows
- `baseX = 120`, `baseY = 200` (baseY leaves room for title + backward arc clearance above top row)

Three post-processing steps run in order:

1. **`compactActivities`** — For each activity with a single source IU, moves it to the same x as that IU and places it below. This avoids needing explicit `same_column_as` / `below_of` hints for the common pattern. Skipped if the user provides explicit position hints for the activity.
2. **`applyCoordinateHints`** — Applies user-supplied `align_with`, `below_of`, `above_of`, `between`, `same_column_as`, and `offset` hints. Runs up to 4 iterative passes for dependent hints.
3. **`centerActivities`** — For remaining unpositioned activities (not compacted, no hints), centres them vertically between their directly-connected edge neighbours.

### 4. Edge routing

#### Endpoint selection (`edgeEndpoints`)

Chooses attachment points and returns a `direction` tag that drives curve generation. The cases are detected in priority order:

| Direction | Condition | Attachment |
|---|---|---|
| `self-loop` | `sourceId === targetId` | Right side, two vertically offset points |
| `down` | Same column, source above target | Bottom of source -> top of target |
| `vertical-wrap` | Same column, source below target | Right side of both, curves around outside |
| `backward-climb` | Target left of source, source well below target, route lower | Left of source -> bottom of target |
| `backward-upper` | Target left of source, route upper | Top port of both |
| `backward-lower` | Target left of source, route lower/outer | Bottom port of both |
| `forward` | Default | Right of source -> left of target |

"Same column" is detected when `|source.x - target.x| < min(sourceWidth, targetWidth) * 0.6`. This handles the common case where an activity (190px wide) and an IU (280px wide) share the same column.

#### Port assignment (`assignPorts`)

When multiple edges leave or enter the same node, they are spread along the usable height of that side. Outgoing ports are sorted by target y; incoming by source y.

#### Path generation (`buildPath`)

All paths use cubic or quadratic Bezier curves. Each `direction` has its own curve formula:

- **forward** — single cubic with horizontal departure and arrival: `C x1+cp y1, x2-cp y2, x2 y2`
- **backward-upper / backward-lower** — U-arc: `C x1 cy, x2 cy, x2 y2` where `cy` is above (upper) or below (lower) both endpoints. Gap = `44 + portIndex * 18`.
- **backward-climb** — smooth curve exiting left and arriving at target bottom. Control points use `max(dy, dx) * 0.45`.
- **down** — straight line if nodes are aligned, else gentle S-curve.
- **vertical-wrap** — cubic bulging right: `C cx y1, cx y2, x2 y2` with `cx = rightEdge + 44 + portIndex * 16`.
- **self-loop** — two quadratic segments forming a tight loop off the right side.

#### Route inference (`routeFromKind`)

When no explicit `route` is set on a flow, it is inferred from the `kind`:

- `return` -> `upper`
- `success` -> `lower`
- `error` -> `lower`
- anything else -> `direct`

#### Edge styling

Each edge `kind` gets a distinct colour and dash pattern:

| Kind | Colour | Dash |
|---|---|---|
| `forward` | slate `#5a6a7a` | solid |
| `return` | blue `#5a82a8` | `8 4` |
| `success` | green `#4a8a5a` | solid |
| `error` | red `#b85450` | `4 3` |
| default | gray `#6b7b8d` | solid |

Arrowhead markers are generated per kind so the arrow colour matches the edge.

### 5. Node rendering

#### Interaction units

Rendered as a rounded rect (`280 x dynamic height`, radius 10) with:

- **Header** — gold fill with clipped rounded top corners, node name in bold.
- **Start indicator** — if the IU is an entry point (target of a start terminal), a dark filled circle overlaps the top-left corner.
- **Content** — items laid out with a flow algorithm (see below).
- **Drop shadow** — SVG `feDropShadow` filter.

#### Activities

Pill-shaped rounded rect (190 x 52, radius 26). Light blue-gray fill.

#### Terminals (end only)

Dark filled circle (radius 28). End terminals get an inner ring. Label renders below the circle.

### 6. IU content — flow layout

Info, fields, actions, and contained units are rendered as individual badges that **flow left-to-right and wrap** to the next row when they exceed the available width (260px = 280 node - 20 inset).

The author controls grouping:

- `"info": "Name, Address, Email"` — **one** badge with all text.
- `"info": ["Name", "Address", "Email"]` — **three** separate badges that flow.
- `"info": ["Name, Address", "Email"]` — **two** badges.

Each badge is a small rounded rect with tinted background and subtle border. The prefix symbol differentiates the type:

| Type | Prefix | Text colour | Tint |
|---|---|---|---|
| Info / output | `○` | gray | gray |
| Fields / input | `□` | dark slate | slate |
| Actions | `▷` | amber | amber |
| Contained units | `■` | gray | gray |

The same flow-row-count logic is used in `computeInteractionUnitHeight` so the node height matches the rendered content exactly.

## Input format

The renderer accepts a loose, illustrative format. All of these are valid for `info`, `fields`, and `units`:

```jsonc
"info": "Name, Address, Email"              // comma-separated string -> one badge
"info": ["Name", "Address"]                 // string array -> one badge each
"info": [{ "id": "name", "label": "Name" }] // object array -> label extracted
```

Actions can also be strings (`"actions": ["Edit", "Save"]`) but typically use objects with `id` so that `flow.trigger` references can be validated.

## Layout hints

Node position hints and flow route hints are optional. The algorithm produces reasonable layouts automatically for common patterns (activities placed below their source IU, routes inferred from `kind`). Hints are available for when the automatic layout needs overriding.

### Node hints (in `layout.hints[]`)

| Property | Effect |
|---|---|
| `column` / `rank` | Force node into a specific layer |
| `align_with` | Match y of another node |
| `below_of` / `above_of` | Place below/above another node (220px offset) |
| `same_column_as` | Match x of another node |
| `between: [a, b]` | Centre y between two nodes |
| `before` / `after` | Prefer ordering within same layer |
| `offset` | Fine-tune y by `offset * 28` px |

### Flow hints (in `flow.layout`)

| Property | Effect |
|---|---|
| `kind` | Semantic type: `forward`, `return`, `success`, `error`. Drives edge colour and default route. |
| `route` | Override route: `direct`, `upper`, `lower`, `outer`. |

## ViewBox calculation

The SVG viewBox is computed from the actual extents of all nodes **and** all edge label positions (not just node bounds). This ensures that edge arcs and labels that extend beyond the outermost nodes are never clipped. The origin x shifts left if any content extends past x=0.

## File structure

The file is wrapped in an IIFE. Sections are separated by banner comments:

```
Palette          — colour constants
Helpers          — escapeXml, dedupe
Parse & validate — parseInteractionDocument, validateDiagramModel
Layout hints     — getLayoutHints, findNodeHint, findFlowLayout, findFlowHint
Node building    — buildNodes (start terminal elimination, entry IU detection)
Node dimensions  — describeNode, computeInteractionUnitHeight, flow row counting
Graph edges      — buildGraphEdges, dedupeGraphEdges
DAG construction — buildAcyclicGraph (cycle removal via DFS)
Layer assignment — assignLayers (topological sweep + hint fixup)
Layer ordering   — orderLayers (barycenter heuristic)
Coordinate assignment — assignCoordinates, compactActivities, applyCoordinateHints, centerActivities
Build layered layout  — buildLayeredLayout (orchestrates the four layout phases)
Edge endpoints & ports — edgeEndpoints (direction detection), port assignment
Edge color       — edgeColor, edgeDash
Path generation  — buildPath (curve formulas per direction), routeFromKind
Node rendering   — renderNode, renderInteractionUnitNode, buildInteractionUnitContent
Edge rendering   — renderEdge, renderEdges
Main render      — renderInteractionDiagram, renderInto
```
