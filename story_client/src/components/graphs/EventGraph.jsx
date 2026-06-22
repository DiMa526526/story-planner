/**
 * Граф событий истории
 * Визуализирует связи между событиями в виде направленного графа
 * Автоматически располагает узлы по уровням (layered layout)
 */
import { useState, useEffect, useCallback } from 'react';
import ReactFlow, {
  addEdge,
  ConnectionLineType,
  Panel,
  useNodesState,
  useEdgesState,
  MarkerType,
  Background,
  Controls,
  MiniMap,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../ui/Button';
import Spinner from '../ui/Spinner';

const EventGraph = ({ storyId }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const { get } = useApi();

  useEffect(() => {
    fetchEventsAndLinks();
  }, [storyId]);

  const fetchEventsAndLinks = async () => {
    try {
      const eventsData = await get(`/stories/${storyId}/events`);
      setEvents(eventsData);

      const linksPromises = eventsData.map(event =>
        get(`/events/${event.id}/links/outgoing`).catch(() => [])
      );
      const allLinks = await Promise.all(linksPromises);
      const links = allLinks.flat();

      buildGraph(eventsData, links);
    } catch (error) {
      console.error('Failed to fetch graph data:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculatePositions = (eventsData, links) => {
    const outgoingMap = new Map();
    const incomingCount = new Map();

    eventsData.forEach(event => {
      outgoingMap.set(event.id, []);
      incomingCount.set(event.id, 0);
    });

    links.forEach(link => {
      outgoingMap.get(link.fromEventId).push(link.toEventId);
      incomingCount.set(link.toEventId, (incomingCount.get(link.toEventId) || 0) + 1);
    });

    const startNodes = eventsData.filter(event => incomingCount.get(event.id) === 0);

    const levels = new Map();
    const queue = [...startNodes];
    startNodes.forEach(node => levels.set(node.id, 0));

    while (queue.length > 0) {
      const current = queue.shift();
      const currentLevel = levels.get(current.id);
      const outgoing = outgoingMap.get(current.id) || [];

      for (const targetId of outgoing) {
        if (!levels.has(targetId) || levels.get(targetId) <= currentLevel + 1) {
          levels.set(targetId, currentLevel + 1);
          const targetEvent = eventsData.find(e => e.id === targetId);
          if (targetEvent && !queue.includes(targetEvent)) {
            queue.push(targetEvent);
          }
        }
      }
    }

    const levelGroups = new Map();
    eventsData.forEach(event => {
      const level = levels.get(event.id) ?? 0;
      if (!levelGroups.has(level)) levelGroups.set(level, []);
      levelGroups.get(level).push(event);
    });

    const positions = new Map();
    const levelHeight = 180;
    const nodeWidth = 220;
    const startY = 50;

    for (const [level, levelEvents] of levelGroups.entries()) {
      const levelCount = levelEvents.length;
      const totalWidth = levelCount * nodeWidth;
      const startX = Math.max(50, (window.innerWidth - totalWidth) / 4);

      levelEvents.forEach((event, idx) => {
        const x = startX + idx * nodeWidth;
        const y = startY + level * levelHeight;
        positions.set(event.id, { x, y });
      });
    }

    const isolatedEvents = eventsData.filter(event =>
      outgoingMap.get(event.id).length === 0 && incomingCount.get(event.id) === 0
    );

    if (isolatedEvents.length > 0) {
      const isolatedStartX = 100;
      isolatedEvents.forEach((event, idx) => {
        positions.set(event.id, {
          x: isolatedStartX + idx * nodeWidth,
          y: startY + levelGroups.size * levelHeight + 50
        });
      });
    }

    return positions;
  };

  const buildGraph = (eventsData, links) => {
    const positions = calculatePositions(eventsData, links);

    const newNodes = eventsData.map((event) => {
      const incomingLinks = links.filter(l => l.toEventId === event.id);
      const outgoingLinks = links.filter(l => l.fromEventId === event.id);

      let nodeType = '';
      if (incomingLinks.length === 0 && outgoingLinks.length > 0) nodeType = 'start-node';
      else if (outgoingLinks.length === 0 && incomingLinks.length > 0) nodeType = 'end-node';
      else if (incomingLinks.length === 0 && outgoingLinks.length === 0) nodeType = 'isolated-node';

      const position = positions.get(event.id) || { x: 0, y: 0 };

      return {
        id: String(event.id),
        type: 'default',
        data: {
          label: (
            <div className="event-node">
              <div className="event-node-title">{event.title}</div>
              <Link
                to={`/stories/${storyId}/events/${event.id}`}
                className="event-node-link"
                onClick={(e) => e.stopPropagation()}
              >
                Подробнее →
              </Link>
            </div>
          ),
        },
        position: { x: position.x, y: position.y },
        className: nodeType,
      };
    });

    const newEdges = links.map(link => ({
      id: `${link.fromEventId}-${link.toEventId}`,
      source: String(link.fromEventId),
      target: String(link.toEventId),
      label: link.choiceText || '',
      type: 'smoothstep',
      animated: true,
      markerEnd: { type: MarkerType.ArrowClosed, color: 'var(--accent)' },
      style: { stroke: 'var(--accent)', strokeWidth: 2 },
      labelStyle: { fill: 'var(--text-secondary)', fontSize: 10 },
    }));

    setNodes(newNodes);
    setEdges(newEdges);
  };

  const onConnect = useCallback((params) => {
    setEdges((eds) => addEdge({ ...params, type: ConnectionLineType.SmoothStep, animated: true }, eds));
  }, [setEdges]);

  const reLayout = () => {
    if (events.length > 0) {
      fetchEventsAndLinks();
    }
  };

  if (loading) return <Spinner />;

  if (events.length === 0) {
    return (
      <div className="card event-graph-empty">
        <div className="event-graph-empty-icon">
          <span className="icon icon-4xl">🗺</span>
        </div>
        <p className="event-graph-empty-text">Нет событий для отображения графа</p>
        <Link to={`/stories/${storyId}/events/new`}>
          <Button variant="primary">Создать первое событие</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="event-graph-container">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        connectionLineType={ConnectionLineType.SmoothStep}
        fitView
        attributionPosition="bottom-right"
        defaultViewport={{ x: 0, y: 0, zoom: 0.8 }}
      >
        <Background color="#aaa" gap={16} />
        <Controls />
        <MiniMap
          nodeColor={(node) => {
            if (node.className?.includes('start-node')) return '#22c55e';
            if (node.className?.includes('end-node')) return '#ef4444';
            if (node.className?.includes('isolated-node')) return '#9ca3af';
            return '#8b5cf6';
          }}
        />
        <Panel position="top-right" className="event-graph-panel">
          <button onClick={reLayout} className="event-graph-relayout-btn">
            ⟳ Перестроить
          </button>
        </Panel>
      </ReactFlow>
    </div>
  );
};

export default EventGraph;