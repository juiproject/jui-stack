(() => {
const SVG_NS = "http://www.w3.org/2000/svg";

/* ── Palette ───────────────────────────────────────────────── */

const PALETTE = {
  bg:           "#f8f7f3",
  title:        "#1a2a3a",
  // terminals
  termFill:     "#1e3a5f",
  termStroke:   "#16304f",
  termText:     "#ffffff",
  // activities
  actFill:      "#eef3f8",
  actStroke:    "#5a7a8f",
  actText:      "#1f2933",
  // interaction units
  iuFill:       "#fffdf5",
  iuStroke:     "#c8a44e",
  iuHeader:     "#f0dca0",
  iuDivider:    "#d4b05a",
  iuText:       "#2c2416",
  iuFieldText:  "#1a2a3a",
  iuInfoText:   "#5a6a7a",
  iuAccentText: "#9f580a",
  // edges by kind
  edgeDefault:  "#6b7b8d",
  edgeForward:  "#5a6a7a",
  edgeReturn:   "#5a82a8",
  edgeSuccess:  "#4a8a5a",
  edgeError:    "#b85450",
  // labels
  labelBg:      "#fffef8",
  labelText:    "#2c3e50",
  // shadow
  shadowColor:  "rgba(30,50,70,0.10)",
};

/* ── Helpers ───────────────────────────────────────────────── */

function escapeXml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&apos;");
}

function dedupe(values) {
  return [...new Set(values)];
}

/* ── Parse & validate ──────────────────────────────────────── */

function parseInteractionDocument(source) {
  const trimmed = source.trim();
  if (!trimmed) {
    throw new Error("The interaction source is empty.");
  }

  if (trimmed.startsWith("```")) {
    const lines = trimmed.split("\n");
    const first = lines.shift()?.trim() ?? "";
    const last = lines.at(-1)?.trim() ?? "";
    if (first !== "```interaction" || last !== "```") {
      throw new Error("Only fenced ```interaction blocks are supported.");
    }
    lines.pop();
    return JSON.parse(lines.join("\n"));
  }

  return JSON.parse(trimmed);
}

function validateDiagramModel(model) {
  if (!model || typeof model !== "object") {
    throw new Error("The diagram must be an object.");
  }
  if (!model.diagram || typeof model.diagram !== "object") {
    throw new Error("Missing required diagram section.");
  }
  if (!model.diagram.id || !model.diagram.view) {
    throw new Error("diagram.id and diagram.view are required.");
  }

  const terminals = new Map((model.terminals ?? []).map((item) => [item.id, item]));
  const units = new Map((model.interaction_units ?? []).map((item) => [item.id, item]));
  const activities = new Map((model.activities ?? []).map((item) => [item.id, item]));
  const flows = model.flows ?? [];

  for (const flow of flows) {
    if (!terminals.has(flow.from) && !units.has(flow.from)) {
      throw new Error(`Unknown flow source: ${flow.from}`);
    }
    if (!terminals.has(flow.to) && !units.has(flow.to)) {
      throw new Error(`Unknown flow target: ${flow.to}`);
    }
    if (flow.via && !activities.has(flow.via)) {
      throw new Error(`Unknown flow activity: ${flow.via}`);
    }
    if (flow.trigger && units.has(flow.from)) {
      const source = units.get(flow.from);
      const actionIds = collectActionIds(source);
      if (!actionIds.has(flow.trigger)) {
        throw new Error(`Unknown trigger '${flow.trigger}' on source IU '${flow.from}'.`);
      }
    }
  }

  return model;
}

function collectActionIds(unit) {
  const ids = new Set();
  const addActions = (actions) => {
    for (const action of actions ?? []) {
      if (typeof action === "string") {
        // Support both single names and comma-separated lists
        for (const part of action.split(",")) ids.add(part.trim());
      } else {
        ids.add(action.id);
      }
    }
  };
  addActions(unit.actions);
  for (const contained of unit.units ?? []) {
    if (typeof contained === "string") continue;
    addActions(contained.contains?.actions);
  }
  return ids;
}

/**
 * Extract labels from a loose content declaration.
 * A plain string stays as one item (comma-separated text is preserved).
 * An array gives one item per entry.
 * Accepts: "a, b, c" | ["a", "b"] | [{ label: "a" }] | [{ id: "x" }]
 */
function extractLabels(items) {
  if (!items) return [];
  if (typeof items === "string") return [items.trim()];
  if (Array.isArray(items)) {
    return items.map((item) => {
      if (typeof item === "string") return item.trim();
      return item.label ?? item.id ?? "";
    }).filter(Boolean);
  }
  return [];
}

/* ── Layout hints ──────────────────────────────────────────── */

function getLayoutHints(model) {
  return model.layout?.hints ?? [];
}

function findNodeHint(hints, nodeId) {
  return hints.find((hint) => hint.node === nodeId || hint.place === nodeId);
}

function findFlowLayout(flow) {
  return flow.layout ?? {};
}

function findFlowHint(hints, flow) {
  return hints.find((hint) => {
    if (!hint.flow) {
      return false;
    }
    return (
      hint.flow.from === flow.from &&
      hint.flow.to === flow.to &&
      (hint.flow.trigger ?? null) === (flow.trigger ?? null) &&
      (hint.flow.via ?? null) === (flow.via ?? null) &&
      (hint.flow.condition ?? null) === (flow.condition ?? null)
    );
  });
}

/* ── Node building ─────────────────────────────────────────── */

function buildNodes(model) {
  const nodes = [];
  const nodeMap = new Map();
  const layoutHints = getLayoutHints(model);

  // Identify start terminals and which IUs they flow into
  const startTerminalIds = new Set(
    (model.terminals ?? [])
      .filter((t) => t.type === "start")
      .map((t) => t.id)
  );
  const entryIuIds = new Set();
  for (const flow of model.flows ?? []) {
    if (startTerminalIds.has(flow.from)) {
      entryIuIds.add(flow.to);
    }
  }

  const ensureNode = (id, type, label, data = null) => {
    if (nodeMap.has(id)) {
      return nodeMap.get(id);
    }
    const node = { id, type, label, data };
    nodeMap.set(id, node);
    nodes.push(node);
    return node;
  };

  // Skip start terminals — entry IUs get a start indicator instead
  for (const terminal of model.terminals ?? []) {
    if (terminal.type === "start") continue;
    ensureNode(terminal.id, "terminal", terminal.name ?? terminal.id, terminal);
  }
  for (const unit of model.interaction_units ?? []) {
    const node = ensureNode(unit.id, "interaction_unit", unit.name ?? unit.id, unit);
    node.isEntry = entryIuIds.has(unit.id);
  }
  for (const activity of model.activities ?? []) {
    ensureNode(activity.id, "activity", activity.name ?? activity.id, activity);
  }

  // Use entry IUs as layout roots instead of start terminals
  const startIds = new Set(entryIuIds);
  const graphEdges = buildGraphEdges(model);
  // Filter out edges from/to start terminals
  const filteredEdges = graphEdges.filter(
    (e) => !startTerminalIds.has(e.sourceId) && !startTerminalIds.has(e.targetId)
  );
  const layout = buildLayeredLayout(nodes, filteredEdges, startIds, layoutHints);

  for (const node of nodes) {
    const style = describeNode(node);
    node.width = style.width;
    node.height = style.height;
    const position = layout.positions.get(node.id) ?? { x: 120, y: 120, level: 0, row: 0 };
    node.level = position.level;
    node.row = position.row;
    node.x = position.x;
    node.y = position.y;
  }

  return { nodes, nodeMap };
}

/* ── Node dimensions ───────────────────────────────────────── */

function describeNode(node) {
  if (node.type === "terminal") {
    return {
      shape: "circle",
      width: 56,
      height: 56,
      radius: 28,
    };
  }
  if (node.type === "activity") {
    return {
      shape: "pill",
      width: 190,
      height: 52,
      radius: 26,
    };
  }
  const height = computeInteractionUnitHeight(node.data);
  return {
    shape: "rect",
    width: 280,
    height,
    radius: 10,
  };
}

function itemWidth(text, prefix) {
  const full = prefix ? `${prefix} ${text}` : text;
  return full.length * 6.8 + 14;
}

function flowRowCount(labels, prefix, maxWidth, gap) {
  if (labels.length === 0) return 0;
  let rows = 1;
  let rowX = 0;
  for (const label of labels) {
    const w = itemWidth(label, prefix);
    if (rowX > 0 && rowX + gap + w > maxWidth) {
      rows++;
      rowX = w;
    } else {
      rowX += (rowX > 0 ? gap : 0) + w;
    }
  }
  return rows;
}

function computeInteractionUnitHeight(unit) {
  const flowW = 260; // 280 node - 20 inset
  const gap = 6;
  const rowH = 22;
  const sectionGap = 6;
  let totalRows = 0;
  let sections = 0;

  if (unit?.description) { totalRows += 1; sections += 1; }

  const infoRows = flowRowCount(extractLabels(unit?.info), "\u25cb", flowW, gap);
  if (infoRows > 0) { totalRows += infoRows; sections += 1; }

  const fieldRows = flowRowCount(extractLabels(unit?.fields), "\u25a1", flowW, gap);
  if (fieldRows > 0) { totalRows += fieldRows; sections += 1; }

  const actionRows = flowRowCount(extractLabels(unit?.actions), "\u25b7", flowW, gap);
  if (actionRows > 0) { totalRows += actionRows; sections += 1; }

  const unitRows = flowRowCount(extractLabels(unit?.units), "\u25a0", flowW, gap);
  if (unitRows > 0) { totalRows += unitRows; sections += 1; }

  return Math.max(84, 48 + totalRows * rowH + Math.max(0, sections - 1) * sectionGap + 14);
}

/* ── Graph edges ───────────────────────────────────────────── */

function buildGraphEdges(model) {
  const edges = [];
  const layoutHints = getLayoutHints(model);
  for (const flow of model.flows ?? []) {
    const flowLayout = findFlowLayout(flow);
    const hintedLayout = findFlowHint(layoutHints, flow) ?? {};
    if (flow.via) {
      edges.push({
        sourceId: flow.from,
        targetId: flow.via,
        label: flow.trigger ? `${flow.trigger}` : "",
        className: "to-activity",
        route: "direct",
        kind: "forward",
        style: flowLayout.style ?? hintedLayout.style,
      });
      edges.push({
        sourceId: flow.via,
        targetId: flow.to,
        label: [flow.condition, flow.outcome].filter(Boolean).join(" / "),
        className: "to-target",
        route: flowLayout.route ?? hintedLayout.route,
        kind: flowLayout.kind ?? hintedLayout.kind,
        style: flowLayout.style ?? hintedLayout.style,
      });
    } else {
      edges.push({
        sourceId: flow.from,
        targetId: flow.to,
        label: [flow.trigger, flow.condition, flow.outcome].filter(Boolean).join(" / "),
        className: "direct",
        route: flowLayout.route ?? hintedLayout.route,
        kind: flowLayout.kind ?? hintedLayout.kind,
        style: flowLayout.style ?? hintedLayout.style,
      });
    }
  }
  return dedupeGraphEdges(edges);
}

function dedupeGraphEdges(edges) {
  const seen = new Set();
  const result = [];
  for (const edge of edges) {
    const key = [
      edge.sourceId,
      edge.targetId,
      edge.label,
      edge.className,
      edge.className === "to-activity" ? "" : edge.route ?? "",
      edge.className === "to-activity" ? "" : edge.kind ?? "",
      edge.className === "to-activity" ? "" : edge.style ?? "",
    ].join("|");
    if (seen.has(key)) {
      continue;
    }
    seen.add(key);
    result.push(edge);
  }
  return result;
}

/* ── DAG construction ──────────────────────────────────────── */

function buildAcyclicGraph(nodeIds, graphEdges, startIds) {
  const orderedStartIds = nodeIds.filter((id) => startIds.has(id));
  const preferredRoots = orderedStartIds.length > 0 ? orderedStartIds : nodeIds;
  const adjacency = new Map(nodeIds.map((id) => [id, []]));

  graphEdges.forEach((edge, index) => {
    adjacency.get(edge.sourceId)?.push({ targetId: edge.targetId, index });
  });

  const state = new Map(nodeIds.map((id) => [id, 0]));
  const reversed = new Set();

  function visit(nodeId) {
    state.set(nodeId, 1);
    for (const link of adjacency.get(nodeId) ?? []) {
      const targetState = state.get(link.targetId) ?? 0;
      if (targetState === 0) {
        visit(link.targetId);
      } else if (targetState === 1) {
        reversed.add(link.index);
      }
    }
    state.set(nodeId, 2);
  }

  for (const rootId of preferredRoots) {
    if ((state.get(rootId) ?? 0) === 0) {
      visit(rootId);
    }
  }
  for (const nodeId of nodeIds) {
    if ((state.get(nodeId) ?? 0) === 0) {
      visit(nodeId);
    }
  }

  const dagEdges = graphEdges.map((edge, index) => {
    if (!reversed.has(index)) {
      return { ...edge, dagSourceId: edge.sourceId, dagTargetId: edge.targetId, reversed: false };
    }
    return { ...edge, dagSourceId: edge.targetId, dagTargetId: edge.sourceId, reversed: true };
  });

  const incoming = new Map(nodeIds.map((id) => [id, []]));
  const outgoing = new Map(nodeIds.map((id) => [id, []]));
  for (const edge of dagEdges) {
    outgoing.get(edge.dagSourceId)?.push(edge);
    incoming.get(edge.dagTargetId)?.push(edge);
  }

  return { edges: dagEdges, incoming, outgoing };
}

/* ── Layer assignment ──────────────────────────────────────── */

function assignLayers(nodes, dag, startIds, layoutHints) {
  const nodeIds = nodes.map((node) => node.id);
  const hintByNode = new Map(
    nodes
      .map((node) => [node.id, findNodeHint(layoutHints, node.id)])
      .filter((entry) => entry[1])
  );
  const indegree = new Map(nodeIds.map((id) => [id, dag.incoming.get(id)?.length ?? 0]));
  const queue = [];
  for (const nodeId of nodeIds) {
    if ((indegree.get(nodeId) ?? 0) === 0) {
      queue.push(nodeId);
    }
  }

  const topo = [];
  while (queue.length > 0) {
    const nodeId = queue.shift();
    topo.push(nodeId);
    for (const edge of dag.outgoing.get(nodeId) ?? []) {
      const next = edge.dagTargetId;
      indegree.set(next, (indegree.get(next) ?? 0) - 1);
      if ((indegree.get(next) ?? 0) === 0) {
        queue.push(next);
      }
    }
  }

  const layerMap = new Map(nodeIds.map((id) => [id, 0]));
  for (const nodeId of topo) {
    const nodeHint = hintByNode.get(nodeId);
    let layer = layerMap.get(nodeId) ?? 0;
    if (typeof nodeHint?.rank === "number") {
      layer = Math.max(layer, nodeHint.rank);
    }
    if (typeof nodeHint?.column === "number") {
      layer = Math.max(layer, nodeHint.column);
    }
    if (startIds.has(nodeId)) {
      layer = 0;
    }
    layerMap.set(nodeId, layer);
    for (const edge of dag.outgoing.get(nodeId) ?? []) {
      const next = edge.dagTargetId;
      const requiredGap = nodeHint?.rank === layer && edge.reversed ? 0 : 1;
      const candidate = layer + requiredGap;
      layerMap.set(next, Math.max(layerMap.get(next) ?? 0, candidate));
    }
  }

  for (let i = 0; i < 3; i += 1) {
    let changed = false;
    for (const node of nodes) {
      const hint = hintByNode.get(node.id);
      const forcedLayer = typeof hint?.column === "number" ? hint.column : hint?.rank;
      if (typeof forcedLayer === "number" && (layerMap.get(node.id) ?? 0) !== forcedLayer) {
        layerMap.set(node.id, Math.max(startIds.has(node.id) ? 0 : forcedLayer, forcedLayer));
        changed = true;
      }
    }
    for (const edge of dag.edges) {
      const sourceLayer = layerMap.get(edge.dagSourceId) ?? 0;
      const targetLayer = layerMap.get(edge.dagTargetId) ?? 0;
      if (targetLayer <= sourceLayer) {
        layerMap.set(edge.dagTargetId, sourceLayer + 1);
        changed = true;
      }
    }
    if (!changed) {
      break;
    }
  }

  return layerMap;
}

/* ── Layer ordering ────────────────────────────────────────── */

function orderLayers(nodes, dag, layerMap, layoutHints) {
  const layers = new Map();
  const encounterOrder = new Map(nodes.map((node, index) => [node.id, index]));
  for (const node of nodes) {
    const layer = layerMap.get(node.id) ?? 0;
    const bucket = layers.get(layer) ?? [];
    bucket.push(node.id);
    layers.set(layer, bucket);
  }

  for (const bucket of layers.values()) {
    bucket.sort((a, b) => compareNodeHints(a, b, layoutHints) || (encounterOrder.get(a) - encounterOrder.get(b)));
  }

  const sortedLayerIds = [...layers.keys()].sort((a, b) => a - b);
  for (let pass = 0; pass < 6; pass += 1) {
    const downward = pass % 2 === 0;
    const sequence = downward ? sortedLayerIds : [...sortedLayerIds].reverse();
    for (const layerId of sequence) {
      const bucket = layers.get(layerId) ?? [];
      bucket.sort((left, right) => {
        const hintOrder = compareNodeHints(left, right, layoutHints);
        if (hintOrder !== 0) {
          return hintOrder;
        }
        const leftScore = barycenterForOrdering(left, layerId, layers, layerMap, dag, downward);
        const rightScore = barycenterForOrdering(right, layerId, layers, layerMap, dag, downward);
        if (leftScore !== rightScore) {
          return leftScore - rightScore;
        }
        return (encounterOrder.get(left) ?? 0) - (encounterOrder.get(right) ?? 0);
      });
    }
  }

  return layers;
}

function compareNodeHints(leftId, rightId, layoutHints) {
  const leftHint = findNodeHint(layoutHints, leftId);
  const rightHint = findNodeHint(layoutHints, rightId);
  if (leftHint?.before === rightId || rightHint?.after === leftId) {
    return -1;
  }
  if (leftHint?.after === rightId || rightHint?.before === leftId) {
    return 1;
  }
  return 0;
}

function barycenterForOrdering(nodeId, layerId, layers, layerMap, dag, downward) {
  const neighborEdges = downward ? dag.incoming.get(nodeId) ?? [] : dag.outgoing.get(nodeId) ?? [];
  const positions = [];
  for (const edge of neighborEdges) {
    const neighborId = downward ? edge.dagSourceId : edge.dagTargetId;
    const neighborLayer = layerMap.get(neighborId);
    if (neighborLayer === undefined || neighborLayer === layerId) {
      continue;
    }
    const bucket = layers.get(neighborLayer) ?? [];
    const index = bucket.indexOf(neighborId);
    if (index >= 0) {
      positions.push(index);
    }
  }
  if (positions.length === 0) {
    return Number.MAX_SAFE_INTEGER / 2;
  }
  return positions.reduce((sum, value) => sum + value, 0) / positions.length;
}

/* ── Coordinate assignment ─────────────────────────────────── */

function assignCoordinates(nodes, orderedLayers, layoutHints, nodeMap, graphEdges) {
  const positions = new Map();
  const layerIds = [...orderedLayers.keys()].sort((a, b) => a - b);
  const horizontalSpacing = 360;
  const verticalSpacing = 200;
  const baseX = 120;
  // Leave room above nodes for title (~50px) + backward arc clearance (~70px)
  const hasTitle = true; // title is always rendered
  const baseY = hasTitle ? 200 : 120;

  for (const layerId of layerIds) {
    const bucket = orderedLayers.get(layerId) ?? [];
    bucket.forEach((nodeId, rowIndex) => {
      positions.set(nodeId, {
        x: baseX + layerId * horizontalSpacing,
        y: baseY + rowIndex * verticalSpacing,
        level: layerId,
        row: rowIndex,
      });
    });
  }

  // Auto-compact activities below their source IU (before user hints override)
  const compacted = compactActivities(nodes, positions, nodeMap, graphEdges, layoutHints);
  applyCoordinateHints(positions, layoutHints);
  centerActivities(nodes, positions, nodeMap, layoutHints, graphEdges, compacted);
  return positions;
}

/**
 * For each activity with a single source IU, move it to the same X as
 * that IU and place it below. This avoids needing explicit same_column_as
 * and below_of hints for the common pattern where an activity processes
 * an action from one IU.
 */
function compactActivities(nodes, positions, nodeMap, graphEdges, layoutHints) {
  const compacted = new Set();

  // Group activities by their unique source IU
  const byParent = new Map();
  for (const node of nodes) {
    if (node.type !== "activity") continue;
    // Skip if user already provided explicit position hints
    const hint = findNodeHint(layoutHints, node.id);
    if (hint?.align_with || hint?.below_of || hint?.above_of || hint?.same_column_as || hint?.between) {
      continue;
    }

    const sourceIds = new Set();
    for (const edge of graphEdges) {
      if (edge.targetId === node.id) sourceIds.add(edge.sourceId);
    }
    if (sourceIds.size !== 1) continue;

    const parentId = [...sourceIds][0];
    const parent = nodeMap.get(parentId);
    if (!parent || parent.type !== "interaction_unit") continue;

    const list = byParent.get(parentId) ?? [];
    list.push(node);
    byParent.set(parentId, list);
  }

  for (const [parentId, activities] of byParent) {
    const parentPos = positions.get(parentId);
    const parent = nodeMap.get(parentId);
    if (!parentPos || !parent) continue;

    const parentStyle = describeNode(parent);
    const startY = parentPos.y + parentStyle.height / 2 + 100;

    activities.forEach((activity, index) => {
      const pos = positions.get(activity.id);
      if (pos) {
        pos.x = parentPos.x;
        pos.y = startY + index * 80;
        compacted.add(activity.id);
      }
    });
  }

  return compacted;
}

function applyCoordinateHints(positions, layoutHints) {
  for (let i = 0; i < 4; i += 1) {
    let changed = false;
    for (const hint of layoutHints) {
      const nodeId = hint.node ?? hint.place;
      if (!nodeId || !positions.has(nodeId)) {
        continue;
      }
      const current = positions.get(nodeId);
      let y = current.y;
      let x = current.x;
      if (hint.align_with && positions.has(hint.align_with)) {
        y = positions.get(hint.align_with).y;
      }
      if (hint.below_of && positions.has(hint.below_of)) {
        y = positions.get(hint.below_of).y + 220;
      }
      if (hint.above_of && positions.has(hint.above_of)) {
        y = positions.get(hint.above_of).y - 220;
      }
      if (Array.isArray(hint.between) && hint.between.length === 2) {
        const [a, b] = hint.between;
        if (positions.has(a) && positions.has(b)) {
          y = (positions.get(a).y + positions.get(b).y) / 2;
        }
      }
      if (hint.same_column_as && positions.has(hint.same_column_as)) {
        x = positions.get(hint.same_column_as).x;
      }
      if (typeof hint.offset === "number") {
        y += hint.offset * 28;
      }
      if (y !== current.y || x !== current.x) {
        positions.set(nodeId, { ...current, x, y });
        changed = true;
      }
    }
    if (!changed) {
      break;
    }
  }
}

function centerActivities(nodes, positions, nodeMap, layoutHints, graphEdges, skipIds) {
  // Only center activities relative to their directly-connected neighbors
  const edgeList = graphEdges ?? [];
  for (const node of nodes) {
    if (node.type !== "activity") continue;
    if (skipIds?.has(node.id)) continue;
    const position = positions.get(node.id);
    if (!position) continue;
    const hint = findNodeHint(layoutHints, node.id);
    if (hint?.align_with || hint?.between || hint?.below_of || hint?.above_of || hint?.same_column_as) {
      continue;
    }
    const neighborYs = [];
    for (const edge of edgeList) {
      if (edge.sourceId === node.id && positions.has(edge.targetId)) {
        neighborYs.push(positions.get(edge.targetId).y);
      }
      if (edge.targetId === node.id && positions.has(edge.sourceId)) {
        neighborYs.push(positions.get(edge.sourceId).y);
      }
    }
    if (neighborYs.length > 0) {
      position.y = neighborYs.reduce((sum, v) => sum + v, 0) / neighborYs.length;
    }
  }
}

/* ── Build layered layout ──────────────────────────────────── */

function buildLayeredLayout(nodes, graphEdges, startIds, layoutHints) {
  const nodeIds = nodes.map((node) => node.id);
  const nodeMap = new Map(nodes.map((node) => [node.id, node]));
  const dag = buildAcyclicGraph(nodeIds, graphEdges, startIds);
  const layers = assignLayers(nodes, dag, startIds, layoutHints);
  const orderedLayers = orderLayers(nodes, dag, layers, layoutHints);
  const positions = assignCoordinates(nodes, orderedLayers, layoutHints, nodeMap, graphEdges);
  return { dag, layers, orderedLayers, positions };
}

/* ── Edge endpoints & ports ────────────────────────────────── */

function buildRenderedEdges(model, nodeMap) {
  return buildGraphEdges(model).map((edge, index) => ({
    ...edge,
    index,
    source: nodeMap.get(edge.sourceId),
    target: nodeMap.get(edge.targetId),
  })).filter((edge) => edge.source && edge.target); // start terminals are excluded from nodeMap, so their edges are naturally filtered
}

function assignPorts(edges) {
  const outgoing = new Map();
  const incoming = new Map();

  for (const edge of edges) {
    const out = outgoing.get(edge.sourceId) ?? [];
    out.push(edge);
    outgoing.set(edge.sourceId, out);
    const inEdges = incoming.get(edge.targetId) ?? [];
    inEdges.push(edge);
    incoming.set(edge.targetId, inEdges);
  }

  for (const group of outgoing.values()) {
    group.sort((left, right) => left.target.y - right.target.y || left.index - right.index);
    group.forEach((edge, index) => {
      edge.sourcePort = index;
      edge.sourcePortCount = group.length;
    });
  }

  for (const group of incoming.values()) {
    group.sort((left, right) => left.source.y - right.source.y || left.index - right.index);
    group.forEach((edge, index) => {
      edge.targetPort = index;
      edge.targetPortCount = group.length;
    });
  }
}

function portY(node, portIndex, portCount) {
  const style = describeNode(node);
  if (style.shape === "circle") {
    return node.y;
  }

  const usableTop = node.y - style.height / 2 + (node.type === "interaction_unit" ? 50 : 14);
  const usableBottom = node.y + style.height / 2 - 14;
  if (portCount <= 1) {
    return usableTop + (usableBottom - usableTop) * 0.5;
  }
  const step = (usableBottom - usableTop) / (portCount - 1);
  return usableTop + step * portIndex;
}

function topPortX(node, portIndex, portCount) {
  if (portCount <= 1) {
    return node.x;
  }
  const left = node.x - node.width / 2 + 36;
  const right = node.x + node.width / 2 - 36;
  const step = (right - left) / (portCount - 1);
  return left + step * portIndex;
}

function bottomPortX(node, portIndex, portCount) {
  return topPortX(node, portIndex, portCount);
}

function edgeEndpoints(edge) {
  const sourceStyle = describeNode(edge.source);
  const targetStyle = describeNode(edge.target);
  const route = edge.route ?? routeFromKind(edge.kind);
  const isSelfLoop = edge.sourceId === edge.targetId;

  // ── Self-loop ──
  if (isSelfLoop) {
    const r = sourceStyle.shape === "circle" ? sourceStyle.radius : sourceStyle.width / 2;
    return {
      x1: edge.source.x + r, y1: edge.source.y - sourceStyle.height * 0.15,
      x2: edge.source.x + r, y2: edge.source.y + sourceStyle.height * 0.15,
      direction: "self-loop",
    };
  }

  // ── Vertical alignment (same column) ──
  const colThreshold = Math.min(sourceStyle.width, targetStyle.width) * 0.6;
  const isVertical = Math.abs(edge.source.x - edge.target.x) < colThreshold
                     && sourceStyle.shape !== "circle" && targetStyle.shape !== "circle";

  if (isVertical) {
    if (edge.source.y < edge.target.y) {
      // Downward: bottom of source → top of target
      return {
        x1: edge.source.x, y1: edge.source.y + sourceStyle.height / 2,
        x2: edge.target.x, y2: edge.target.y - targetStyle.height / 2,
        direction: "down",
      };
    }
    // Upward: wrap around the right side
    return {
      x1: edge.source.x + sourceStyle.width / 2, y1: edge.source.y,
      x2: edge.target.x + targetStyle.width / 2, y2: edge.target.y,
      direction: "vertical-wrap",
    };
  }

  // ── Backward (target to the left) ──
  const isBackward = edge.target.x < edge.source.x - 20;
  if (isBackward && sourceStyle.shape !== "circle" && targetStyle.shape !== "circle") {
    // Source well below target + lower route: exit left, curve up to target bottom
    if (edge.source.y > edge.target.y + 40 && (route === "lower" || route === "outer")) {
      return {
        x1: edge.source.x - sourceStyle.width / 2, y1: edge.source.y,
        x2: edge.target.x, y2: edge.target.y + targetStyle.height / 2,
        direction: "backward-climb",
      };
    }
    if (route === "upper") {
      return {
        x1: topPortX(edge.source, edge.sourcePort ?? 0, edge.sourcePortCount ?? 1),
        y1: nodeTop(edge.source),
        x2: topPortX(edge.target, edge.targetPort ?? 0, edge.targetPortCount ?? 1),
        y2: nodeTop(edge.target),
        direction: "backward-upper",
      };
    }
    if (route === "lower" || route === "outer") {
      return {
        x1: bottomPortX(edge.source, edge.sourcePort ?? 0, edge.sourcePortCount ?? 1),
        y1: nodeBottom(edge.source),
        x2: bottomPortX(edge.target, edge.targetPort ?? 0, edge.targetPortCount ?? 1),
        y2: nodeBottom(edge.target),
        direction: "backward-lower",
      };
    }
  }

  // ── Forward: right of source → left of target ──
  const sourceY = portY(edge.source, edge.sourcePort ?? 0, edge.sourcePortCount ?? 1);
  const targetY = portY(edge.target, edge.targetPort ?? 0, edge.targetPortCount ?? 1);
  const sx = edge.source.x + (sourceStyle.shape === "circle" ? sourceStyle.radius : sourceStyle.width / 2);
  const tx = edge.target.x - (targetStyle.shape === "circle" ? targetStyle.radius : targetStyle.width / 2);
  return { x1: sx, y1: sourceY, x2: tx, y2: targetY, direction: "forward" };
}

function nodeTop(node) {
  return node.y - node.height / 2;
}

function nodeBottom(node) {
  return node.y + node.height / 2;
}

/* ── Edge color by kind ────────────────────────────────────── */

function edgeColor(kind) {
  switch (kind) {
    case "return":  return PALETTE.edgeReturn;
    case "success": return PALETTE.edgeSuccess;
    case "error":   return PALETTE.edgeError;
    case "forward": return PALETTE.edgeForward;
    default:        return PALETTE.edgeDefault;
  }
}

function edgeDash(kind) {
  if (kind === "return") return "8 4";
  if (kind === "error")  return "4 3";
  return "none";
}

/* ── Path generation (smooth curves) ───────────────────────── */

function buildPath(edge) {
  const ep = edgeEndpoints(edge);
  const { x1, y1, x2, y2, direction } = ep;
  const lp = 10;

  // ── Self-loop: tight quadratic off the right side ──
  if (direction === "self-loop") {
    const bulge = 38 + (edge.sourcePort ?? 0) * 14;
    const cx = x1 + bulge;
    return {
      d: `M ${x1} ${y1} Q ${cx} ${y1}, ${cx} ${(y1 + y2) / 2} Q ${cx} ${y2}, ${x2} ${y2}`,
      labelX: cx + 8,
      labelY: (y1 + y2) / 2,
    };
  }

  // ── Vertical downward: straight or gentle S ──
  if (direction === "down") {
    if (Math.abs(x2 - x1) < 5) {
      return {
        d: `M ${x1} ${y1} L ${x2} ${y2}`,
        labelX: x1 + 16, labelY: (y1 + y2) / 2,
      };
    }
    const dy = y2 - y1;
    return {
      d: `M ${x1} ${y1} C ${x1} ${y1 + dy * 0.4}, ${x2} ${y2 - dy * 0.4}, ${x2} ${y2}`,
      labelX: Math.max(x1, x2) + 16, labelY: (y1 + y2) / 2,
    };
  }

  // ── Vertical wrap: same column, source below, curve around right ──
  if (direction === "vertical-wrap") {
    const bulge = 44 + (edge.sourcePort ?? 0) * 16;
    const cx = Math.max(x1, x2) + bulge;
    return {
      d: `M ${x1} ${y1} C ${cx} ${y1}, ${cx} ${y2}, ${x2} ${y2}`,
      labelX: cx + 8, labelY: (y1 + y2) / 2,
    };
  }

  // ── Backward climb: exit left of source, curve up to bottom of target ──
  if (direction === "backward-climb") {
    const dy = Math.abs(y1 - y2);
    const dx = Math.abs(x1 - x2);
    const cp = Math.max(dy, dx) * 0.45;
    return {
      d: `M ${x1} ${y1} C ${x1 - cp} ${y1}, ${x2} ${y2 + cp}, ${x2} ${y2}`,
      labelX: (x1 + x2) / 2, labelY: (y1 + y2) / 2,
    };
  }

  // ── Backward upper: clean U-arc above both nodes ──
  if (direction === "backward-upper") {
    const gap = 44 + (edge.sourcePort ?? 0) * 18;
    const cy = Math.min(y1, y2) - gap;
    return {
      d: `M ${x1} ${y1} C ${x1} ${cy}, ${x2} ${cy}, ${x2} ${y2}`,
      labelX: (x1 + x2) / 2, labelY: cy - lp,
    };
  }

  // ── Backward lower: U-arc below both nodes ──
  if (direction === "backward-lower") {
    const gap = 44 + (edge.sourcePort ?? 0) * 18;
    const cy = Math.max(y1, y2) + gap;
    return {
      d: `M ${x1} ${y1} C ${x1} ${cy}, ${x2} ${cy}, ${x2} ${y2}`,
      labelX: (x1 + x2) / 2, labelY: cy + lp + 12,
    };
  }

  // ── Forward: smooth cubic with horizontal departure/arrival ──
  const cp = Math.min(Math.abs(x2 - x1) * 0.4, 160);
  const labelY = Math.abs(y2 - y1) < 20 ? Math.min(y1, y2) - lp : (y1 + y2) / 2 - lp;
  return {
    d: `M ${x1} ${y1} C ${x1 + cp} ${y1}, ${x2 - cp} ${y2}, ${x2} ${y2}`,
    labelX: (x1 + x2) / 2, labelY,
  };
}

function routeFromKind(kind) {
  if (kind === "return") return "upper";
  if (kind === "success") return "lower";
  if (kind === "error") return "lower";
  return "direct";
}

/* ── Node rendering ────────────────────────────────────────── */

function renderNode(node) {
  const style = describeNode(node);

  if (style.shape === "circle") {
    const isStart = node.data?.type === "start";
    const isEnd = node.data?.type === "end";
    const r = style.radius;
    let inner = "";
    if (isEnd) {
      inner = `<circle cx="${node.x}" cy="${node.y}" r="${r - 5}" fill="none" stroke="${PALETTE.termText}" stroke-width="2" />`;
    }
    return `
      <g class="node terminal" data-node-id="${escapeXml(node.id)}">
        <circle cx="${node.x}" cy="${node.y}" r="${r}" fill="${PALETTE.termFill}" stroke="${PALETTE.termStroke}" stroke-width="2.5" filter="url(#shadow)" />
        ${inner}
        <text x="${node.x}" y="${node.y + r + 20}" text-anchor="middle" fill="${PALETTE.title}" font-size="12" font-weight="500" font-family="ui-sans-serif, system-ui, sans-serif">${escapeXml(node.label)}</text>
      </g>
    `;
  }

  const x = node.x - style.width / 2;
  const y = node.y - style.height / 2;

  if (node.type === "interaction_unit") {
    return renderInteractionUnitNode(node, style, x, y);
  }

  // Activity pill
  return `
    <g class="node activity" data-node-id="${escapeXml(node.id)}">
      <rect x="${x}" y="${y}" width="${style.width}" height="${style.height}" rx="${style.radius}" ry="${style.radius}" fill="${PALETTE.actFill}" stroke="${PALETTE.actStroke}" stroke-width="1.5" filter="url(#shadow)" />
      <text x="${node.x}" y="${node.y + 5}" text-anchor="middle" fill="${PALETTE.actText}" font-size="13" font-weight="500" font-family="ui-sans-serif, system-ui, sans-serif">${escapeXml(node.label)}</text>
    </g>
  `;
}

function renderInteractionUnitNode(node, style, x, y) {
  const content = buildInteractionUnitContent(node.data, x, y);
  const headerH = 38;
  const r = style.radius;

  // Start indicator: filled circle overlapping the top-left corner
  let startIndicator = "";
  if (node.isEntry) {
    const cr = 9;
    const cx = x + r * 0.45;
    const cy = y + r * 0.45;
    startIndicator = `<circle cx="${cx}" cy="${cy}" r="${cr}" fill="${PALETTE.termFill}" stroke="${PALETTE.iuStroke}" stroke-width="1.5" />`;
  }

  return `
    <g class="node interaction_unit" data-node-id="${escapeXml(node.id)}">
      <rect x="${x}" y="${y}" width="${style.width}" height="${style.height}" rx="${r}" ry="${r}" fill="${PALETTE.iuFill}" stroke="${PALETTE.iuStroke}" stroke-width="1.8" filter="url(#shadow)" />
      <clipPath id="clip-${escapeXml(node.id)}">
        <rect x="${x}" y="${y}" width="${style.width}" height="${headerH}" rx="${r}" ry="${r}" />
        <rect x="${x}" y="${y + r}" width="${style.width}" height="${headerH - r}" />
      </clipPath>
      <rect x="${x}" y="${y}" width="${style.width}" height="${headerH}" clip-path="url(#clip-${escapeXml(node.id)})" fill="${PALETTE.iuHeader}" />
      <line x1="${x}" y1="${y + headerH}" x2="${x + style.width}" y2="${y + headerH}" stroke="${PALETTE.iuDivider}" stroke-width="1" />
      ${startIndicator}
      <text x="${x + 16}" y="${y + 24}" fill="${PALETTE.iuText}" font-size="14" font-weight="600" font-family="ui-sans-serif, system-ui, sans-serif">${escapeXml(node.label)}</text>
      ${content}
    </g>
  `;
}

function buildInteractionUnitContent(unit, x, y) {
  const headerH = 38;
  const nodeW = 280;
  const insetX = 10;
  const flowW = nodeW - insetX * 2;
  const gap = 6;
  const rowH = 22;
  const boxH = 18;
  const boxR = 4;
  const sectionGap = 6;
  const rendered = [];

  // Collect sections: each is { labels[], prefix, tone }
  const sections = [];
  if (unit?.description) {
    sections.push({ labels: [unit.description], prefix: "", tone: "body" });
  }
  const infoLabels = extractLabels(unit?.info);
  if (infoLabels.length > 0) sections.push({ labels: infoLabels, prefix: "\u25cb", tone: "info" });
  const fieldLabels = extractLabels(unit?.fields);
  if (fieldLabels.length > 0) sections.push({ labels: fieldLabels, prefix: "\u25a1", tone: "field" });
  const actionLabels = extractLabels(unit?.actions);
  if (actionLabels.length > 0) sections.push({ labels: actionLabels, prefix: "\u25b7", tone: "accent" });
  const unitLabels = extractLabels(unit?.units);
  if (unitLabels.length > 0) sections.push({ labels: unitLabels, prefix: "\u25a0", tone: "info" });

  let yOffset = 0;
  for (let si = 0; si < sections.length; si++) {
    if (si > 0) yOffset += sectionGap;
    const section = sections[si];
    const fill =
      section.tone === "accent" ? PALETTE.iuAccentText :
      section.tone === "info" ? PALETTE.iuInfoText :
      section.tone === "field" ? PALETTE.iuFieldText :
      PALETTE.iuText;
    const borderColor =
      section.tone === "accent" ? "rgba(159,88,10,0.25)" :
      section.tone === "field" ? "rgba(26,42,58,0.18)" :
      "rgba(90,106,122,0.18)";
    const bgColor =
      section.tone === "accent" ? "rgba(159,88,10,0.05)" :
      section.tone === "field" ? "rgba(26,42,58,0.04)" :
      "rgba(90,106,122,0.04)";

    // Flow items left-to-right, wrapping
    let rowX = 0;
    for (const label of section.labels) {
      const fullText = section.prefix ? `${section.prefix} ${label}` : label;
      const w = Math.min(fullText.length * 6.8 + 14, flowW);

      if (rowX > 0 && rowX + gap + w > flowW) {
        // Wrap to next row
        yOffset += rowH;
        rowX = 0;
      }

      const bx = x + insetX + rowX;
      const by = y + headerH + 7 + yOffset;
      const ty = y + headerH + 20 + yOffset;

      rendered.push(
        `<rect x="${bx}" y="${by}" width="${w}" height="${boxH}" rx="${boxR}" fill="${bgColor}" stroke="${borderColor}" stroke-width="0.8" />` +
        `<text x="${bx + 6}" y="${ty}" fill="${fill}" font-size="12" font-family="ui-sans-serif, system-ui, sans-serif">${escapeXml(fullText)}</text>`
      );

      rowX += w + gap;
    }
    yOffset += rowH;
  }

  return rendered.join("\n      ");
}

/* ── Edge rendering ────────────────────────────────────────── */

function renderEdge(edge) {
  const path = buildPath(edge);
  const color = edgeColor(edge.kind);
  const dash = edgeDash(edge.kind);
  const strokeAttr = dash !== "none" ? ` stroke-dasharray="${dash}"` : "";
  const safeLabel = edge.label ? escapeXml(edge.label) : "";

  let labelSvg = "";
  if (safeLabel) {
    // Estimate text width for auto-sizing background
    const textWidth = Math.max(safeLabel.length * 6.5 + 16, 50);
    const labelH = 20;
    const lx = path.labelX - textWidth / 2;
    const ly = path.labelY - 13;
    labelSvg = `
      <g class="edge-label">
        <rect x="${lx}" y="${ly}" width="${textWidth}" height="${labelH}" rx="6" fill="${PALETTE.labelBg}" fill-opacity="0.94" stroke="${color}" stroke-width="0.5" stroke-opacity="0.3" />
        <text x="${path.labelX}" y="${path.labelY}" text-anchor="middle" fill="${PALETTE.labelText}" font-size="11" font-family="ui-sans-serif, system-ui, sans-serif">${safeLabel}</text>
      </g>
    `;
  }

  return `
    <g class="edge ${edge.className}">
      <path d="${path.d}" fill="none" stroke="${color}" stroke-width="2"${strokeAttr} stroke-linecap="round" stroke-linejoin="round" marker-end="url(#arrow-${edge.kind ?? "default"})" />
      ${labelSvg}
    </g>
  `;
}

function renderEdges(model, nodeMap) {
  const edges = buildRenderedEdges(model, nodeMap);
  assignPorts(edges);
  return edges.map((edge) => renderEdge(edge)).join("\n");
}

/* ── Main render ───────────────────────────────────────────── */

function renderInteractionDiagram(model) {
  validateDiagramModel(model);
  const { nodes, nodeMap } = buildNodes(model);

  // Compute tight bounds: nodes + edge curves + labels
  const edges = buildRenderedEdges(model, nodeMap);
  assignPorts(edges);
  const edgePaths = edges.map((e) => ({ edge: e, path: buildPath(e) }));

  let minX = Math.min(...nodes.map((n) => n.x - n.width / 2));
  let maxX = Math.max(...nodes.map((n) => n.x + n.width / 2));
  let maxY = Math.max(...nodes.map((n) => n.y + n.height / 2));
  for (const { edge, path } of edgePaths) {
    const labelW = edge.label ? edge.label.length * 6.5 + 16 : 0;
    maxX = Math.max(maxX, path.labelX + labelW / 2);
    minX = Math.min(minX, path.labelX - labelW / 2);
    maxY = Math.max(maxY, path.labelY + 20);
  }

  const pad = 40;
  const originX = Math.min(0, minX - pad);
  const width = maxX + pad - originX;
  const height = maxY + pad;
  const title = model.diagram.title ?? model.diagram.id;

  // Build arrowhead markers per kind
  const kinds = ["default", "forward", "return", "success", "error"];
  const markers = kinds.map((kind) => {
    const color = edgeColor(kind);
    return `<marker id="arrow-${kind}" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto" markerUnits="strokeWidth">
          <path d="M 0 0.5 L 8 4 L 0 7.5 Z" fill="${color}" />
        </marker>`;
  }).join("\n        ");

  return `
    <svg xmlns="${SVG_NS}" viewBox="${originX} 0 ${width} ${height}" width="${width}" height="${height}" role="img" aria-label="${escapeXml(title)}">
      <defs>
        ${markers}
        <filter id="shadow" x="-4%" y="-4%" width="108%" height="116%">
          <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="${PALETTE.shadowColor}" />
        </filter>
      </defs>
      <rect x="${originX}" y="0" width="${width}" height="${height}" fill="${PALETTE.bg}" />
      <text x="${originX + 28}" y="40" fill="${PALETTE.title}" font-size="18" font-weight="600" font-family="ui-sans-serif, system-ui, sans-serif">${escapeXml(title)}</text>
      ${renderEdges(model, nodeMap)}
      ${nodes.map((node) => renderNode(node)).join("\n")}
    </svg>
  `.trim();
}

function renderInto(container, source) {
  const model = typeof source === "string" ? parseInteractionDocument(source) : source;
  container.innerHTML = renderInteractionDiagram(model);
}

globalThis.InteractionDiagram = {
  parseInteractionDocument,
  validateDiagramModel,
  renderInteractionDiagram,
  renderInto,
};
})();
