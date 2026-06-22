/**
 * Граф связей персонажа
 * Визуализирует отношения выбранного персонажа с другими
 * Позволяет фильтровать по событиям и добавлять новые связи
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
import Select from '../ui/Select';
import Input from '../ui/Input';
import Card from '../ui/Card';

const CharacterRelationGraph = ({ characterId, storyId, currentEventId = null }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [relationships, setRelationships] = useState([]);
  const [events, setEvents] = useState([]);
  const [selectedEventId, setSelectedEventId] = useState(currentEventId || '');
  const [character, setCharacter] = useState(null);
  const [relationshipHistory, setRelationshipHistory] = useState([]);
  const [allCharacters, setAllCharacters] = useState([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(null);
  const [branchEvents, setBranchEvents] = useState([]);
  const [newRelationData, setNewRelationData] = useState({
    character2Id: '',
    eventId: '',
    relationshipType: '',
    color: '#8b5cf6',
  });
  const { get, post, put, del } = useApi();

  useEffect(() => {
    if (storyId) {
      fetchData();
    }
  }, [characterId, storyId]);

  useEffect(() => {
    if (character && relationships.length > 0) {
      buildGraph();
    }
  }, [character, relationships, selectedEventId, relationshipHistory]);

  const fetchData = async () => {
    try {
      const [charData, allCharsData, relsData, eventsData] = await Promise.all([
        get(`/characters/${characterId}`),
        get(`/stories/${storyId}/characters`),
        get(`/stories/${storyId}/relationships`),
        get(`/stories/${storyId}/events`)
      ]);

      setCharacter(charData);
      setAllCharacters(allCharsData);

      const characterRelations = relsData.filter(rel =>
        rel.character1?.id === parseInt(characterId) ||
        rel.character2?.id === parseInt(characterId)
      );
      setRelationships(characterRelations);
      setEvents(eventsData);

      const historyPromises = characterRelations.map(rel =>
        get(`/relationships/${rel.id}/history`).catch(() => [])
      );
      const allHistory = await Promise.all(historyPromises);
      setRelationshipHistory(allHistory.flat());

      if (currentEventId) {
        await fetchBranchEvents(currentEventId);
      }
    } catch (error) {
      console.error('Failed to fetch graph data:', error);
    }
  };

  const fetchBranchEvents = async (startEventId) => {
    const branchIds = new Set([parseInt(startEventId)]);
    const queue = [parseInt(startEventId)];

    while (queue.length > 0) {
      const currentId = queue.shift();
      try {
        const links = await get(`/events/${currentId}/links/outgoing`).catch(() => []);
        for (const link of links) {
          if (!branchIds.has(link.toEventId)) {
            branchIds.add(link.toEventId);
            queue.push(link.toEventId);
          }
        }
      } catch (e) {
        // Silently handle error
      }
    }

    const branchEventsList = events.filter(e => branchIds.has(e.id));
    setBranchEvents(branchEventsList);
  };

  const getCurrentRelationships = (eventId) => {
    if (!eventId) return relationships;

    const relationshipMap = new Map();

    for (const rel of relationships) {
      const otherChar = rel.character1?.id === parseInt(characterId)
        ? rel.character2?.id
        : rel.character1?.id;
      if (!otherChar) continue;

      const key = `${Math.min(parseInt(characterId), otherChar)}-${Math.max(parseInt(characterId), otherChar)}`;

      const relevantHistory = relationshipHistory
        .filter(h => h.relationshipId === rel.id && h.eventId <= parseInt(eventId))
        .sort((a, b) => b.eventId - a.eventId);

      const latestHistory = relevantHistory[0];

      if (latestHistory) {
        relationshipMap.set(key, {
          ...rel,
          relationshipType: latestHistory.relationshipType,
          color: latestHistory.color,
          eventId: latestHistory.eventId,
          historyId: latestHistory.id,
        });
      }
    }

    return Array.from(relationshipMap.values());
  };

  const buildGraph = () => {
    const currentRels = getCurrentRelationships(selectedEventId);

    const centerNode = {
      id: String(characterId),
      data: {
        label: (
          <div className="character-graph-center-node">
            <div className="character-graph-center-name">{character?.name}</div>
            <div className="character-graph-center-badge">[главный]</div>
          </div>
        ),
      },
      position: { x: 250, y: 250 },
      className: 'center-node',
    };

    const otherCharacters = [];
    currentRels.forEach((rel) => {
      const otherChar = rel.character1?.id === parseInt(characterId)
        ? rel.character2
        : rel.character1;
      if (otherChar && !otherCharacters.find(c => c.id === otherChar.id)) {
        otherCharacters.push({ ...otherChar, relation: rel });
      }
    });

    const angleStep = (2 * Math.PI) / Math.max(otherCharacters.length, 1);
    const radius = 200;

    const otherNodes = otherCharacters.map((item, index) => {
      const angle = angleStep * index;
      const x = 250 + radius * Math.cos(angle);
      const y = 250 + radius * Math.sin(angle);
      // Цвет обводки узла - выбранный пользователем цвет отношений
      const borderColor = item.relation?.color || '#9ca3af';

      return {
        id: String(item.id),
        data: {
          label: (
            <div 
              className="character-graph-other-node"
              style={{ 
                borderColor: borderColor,
                borderWidth: '3px'
              }}
            >
              <div className="character-graph-other-name">{item.name}</div>
            </div>
          ),
        },
        position: { x, y },
        className: 'other-node',
      };
    });

    // Единый цвет для всех линий (стандартный)
    const defaultLineColor = 'var(--accent)';
    
    const graphEdges = currentRels.map((rel, idx) => {
      const otherChar = rel.character1?.id === parseInt(characterId)
        ? rel.character2
        : rel.character1;
      
      return {
        id: `${characterId}-${otherChar.id}-${rel.eventId || idx}`,
        source: String(characterId),
        target: String(otherChar.id),
        label: rel.relationshipType,
        type: 'default',
        animated: true,
        markerEnd: { type: MarkerType.ArrowClosed, color: defaultLineColor },
        style: { stroke: defaultLineColor, strokeWidth: 2 },
        labelStyle: { fill: 'var(--text-secondary)', fontSize: 9, fontWeight: '500' },
        labelBgStyle: { fill: 'transparent' },
      };
    });

    setNodes([centerNode, ...otherNodes]);
    setEdges(graphEdges);
  };

  const handleAddRelation = async (e) => {
    e.preventDefault();
    const targetCharId = parseInt(newRelationData.character2Id);
    const eventId = parseInt(newRelationData.eventId);
    const relType = newRelationData.relationshipType;
    const color = newRelationData.color;

    if (!targetCharId || !eventId || !relType) {
      alert('Заполните все поля');
      return;
    }

    let existingRel = relationships.find(rel => 
      (rel.character1?.id === parseInt(characterId) && rel.character2?.id === targetCharId) ||
      (rel.character1?.id === targetCharId && rel.character2?.id === parseInt(characterId))
    );

    try {
      let relId;
      if (existingRel) {
        relId = existingRel.id;
      } else {
        const relResponse = await post(`/stories/${storyId}/relationships`, {
          character1Id: parseInt(characterId),
          character2Id: targetCharId,
        });
        relId = relResponse.id;
      }

      await post(`/relationships/${relId}/history`, {
        eventId: eventId,
        relationshipType: relType,
        color: color,
      });

      setShowAddForm(false);
      setNewRelationData({ character2Id: '', eventId: '', relationshipType: '', color: '#8b5cf6' });
      fetchData();
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || 'Ошибка добавления отношений');
    }
  };

  const openEditForm = (rel) => {
    let currentType = '';
    let currentColor = '#8b5cf6';
    let currentEventIdForEdit = selectedEventId;
    let currentHistoryId = null;

    if (selectedEventId) {
      const relevantHistory = relationshipHistory
        .filter(h => h.relationshipId === rel.id && h.eventId <= parseInt(selectedEventId))
        .sort((a, b) => b.eventId - a.eventId);
      const latest = relevantHistory[0];
      if (latest) {
        currentType = latest.relationshipType;
        currentColor = latest.color;
        currentEventIdForEdit = latest.eventId;
        currentHistoryId = latest.id;
      }
    } else {
      const allHistoryForRel = relationshipHistory.filter(h => h.relationshipId === rel.id);
      if (allHistoryForRel.length > 0) {
        const latest = allHistoryForRel.sort((a, b) => b.eventId - a.eventId)[0];
        currentType = latest.relationshipType;
        currentColor = latest.color;
        currentEventIdForEdit = latest.eventId;
        currentHistoryId = latest.id;
      }
    }

    setShowEditForm({
      rel,
      relationshipType: currentType,
      color: currentColor,
      eventId: currentEventIdForEdit ? String(currentEventIdForEdit) : '',
      historyId: currentHistoryId,
    });
  };

  const handleEditRelation = async () => {
    if (!showEditForm) return;
    const { rel, relationshipType, color, eventId, historyId } = showEditForm;
    
    if (!eventId) {
      alert('Пожалуйста, выберите событие для изменения отношений');
      return;
    }
    if (!relationshipType.trim()) {
      alert('Введите тип отношений');
      return;
    }
    
    try {
      if (historyId) {
        await put(`/history/${historyId}`, {
          eventId: parseInt(eventId),
          relationshipType: relationshipType,
          color: color,
        });
      } else {
        await post(`/relationships/${rel.id}/history`, {
          eventId: parseInt(eventId),
          relationshipType: relationshipType,
          color: color,
        });
      }
      setShowEditForm(null);
      fetchData();
    } catch (error) {
      alert(error.response?.data?.message || 'Ошибка изменения связи');
    }
  };

  const handleDeleteRelation = async (relId) => {
    if (window.confirm('Удалить связь между персонажами? Вся история будет потеряна.')) {
      try {
        await del(`/relationships/${relId}`);
        fetchData();
      } catch (error) {
        alert(error.response?.data?.message || 'Ошибка удаления связи');
      }
    }
  };

  if (!storyId) {
    return (
      <div className="character-graph-error">
        <p>⚠️ ID истории не найден. Пожалуйста, перезагрузите страницу.</p>
      </div>
    );
  }

  const availableCharacters = allCharacters.filter(c => c.id !== parseInt(characterId));
  const availableEvents = currentEventId
    ? branchEvents.filter(e => e.id !== parseInt(currentEventId))
    : events;

  const eventOptions = availableEvents.map(event => ({
    value: event.id,
    label: `${event.title} ${event.id === parseInt(currentEventId) ? '[текущее]' : ''}`
  }));

  const characterOptions = availableCharacters.map(char => ({
    value: char.id,
    label: char.name
  }));

  const relationList = getCurrentRelationships(selectedEventId);

  return (
    <div className="character-graph">
      <div className="character-graph-toolbar">
        <div className="character-graph-event-selector">
          <span className="character-graph-event-label">
            <span className="icon icon-sm">◈</span> Событие:
          </span>
          <select
            value={selectedEventId}
            onChange={(e) => setSelectedEventId(e.target.value)}
            className="character-graph-event-select"
          >
            <option value="">Все события (текущие отношения)</option>
            {eventOptions.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <Button variant="primary" onClick={() => setShowAddForm(!showAddForm)}>
          ✛ Добавить связь
        </Button>
      </div>

      {showAddForm && (
        <Card className="character-graph-form">
          <h3 className="character-graph-form-title">Новая связь с персонажем</h3>
          <form onSubmit={handleAddRelation} className="character-graph-form-inner">
            <div className="character-graph-form-grid">
              <select
                value={newRelationData.character2Id}
                onChange={(e) => setNewRelationData({ ...newRelationData, character2Id: e.target.value })}
                className="input-field"
                required
              >
                <option value="">Выберите персонажа</option>
                {characterOptions.map(option => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>

              <select
                value={newRelationData.eventId}
                onChange={(e) => setNewRelationData({ ...newRelationData, eventId: e.target.value })}
                className="input-field"
                required
              >
                <option value="">Выберите событие</option>
                {eventOptions.map(option => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>

              <input
                type="text"
                placeholder="Тип отношений (друг, враг, союзник...)"
                value={newRelationData.relationshipType}
                onChange={(e) => setNewRelationData({ ...newRelationData, relationshipType: e.target.value })}
                className="input-field"
                required
              />

              <div className="character-graph-form-color">
                <span>Цвет:</span>
                <input
                  type="color"
                  value={newRelationData.color}
                  onChange={(e) => setNewRelationData({ ...newRelationData, color: e.target.value })}
                  className="character-graph-color-input"
                />
              </div>
            </div>
            <div className="character-graph-form-actions">
              <Button type="submit" variant="primary">Сохранить</Button>
              <Button type="button" variant="secondary" onClick={() => setShowAddForm(false)}>
                Отмена
              </Button>
            </div>
          </form>
        </Card>
      )}

      {showEditForm && (
        <Card className="character-graph-form">
          <h3 className="character-graph-form-title">Редактировать связь</h3>
          <div className="character-graph-form-inner">
            <div className="character-graph-form-grid">
              <select
                value={showEditForm.eventId || ''}
                onChange={(e) => setShowEditForm({ ...showEditForm, eventId: e.target.value })}
                className="input-field"
                required
              >
                <option value="">Выберите событие</option>
                {eventOptions.map(option => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>

              <input
                type="text"
                placeholder="Тип отношений"
                value={showEditForm.relationshipType || ''}
                onChange={(e) => setShowEditForm({ ...showEditForm, relationshipType: e.target.value })}
                className="input-field"
                required
              />

              <div className="character-graph-form-color">
                <span>Цвет:</span>
                <input
                  type="color"
                  value={showEditForm.color || '#8b5cf6'}
                  onChange={(e) => setShowEditForm({ ...showEditForm, color: e.target.value })}
                  className="character-graph-color-input"
                />
              </div>
            </div>
            <div className="character-graph-form-actions">
              <Button variant="primary" onClick={handleEditRelation}>
                Сохранить
              </Button>
              <Button type="button" variant="secondary" onClick={() => setShowEditForm(null)}>
                Отмена
              </Button>
            </div>
          </div>
        </Card>
      )}

      <div className="character-graph-container">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          connectionLineType={ConnectionLineType.Bezier}
          fitView
          attributionPosition="bottom-right"
          defaultViewport={{ x: 0, y: 0, zoom: 0.8 }}
        >
          <Background color="#aaa" gap={16} />
          <Controls />
          <MiniMap />
        </ReactFlow>
      </div>

      <div className="character-graph-footer">
        <p>✦ Выберите событие, чтобы увидеть отношения на тот момент.
          {currentEventId && " Показаны только события на текущей ветке сюжета."}
        </p>
        <p className="character-graph-footer-note">◉ Цвет обводки узла показывает характер отношений.</p>
      </div>

      {relationList.length > 0 && (
        <div className="character-graph-relations-list" style={{ marginTop: '1rem' }}>
          <h3>Список связей</h3>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {relationList.map(rel => {
              const otherChar = rel.character1?.id === parseInt(characterId) ? rel.character2 : rel.character1;
              return (
                <li key={rel.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid var(--border)' }}>
                  <span>
                    <strong>{otherChar?.name}</strong>: {rel.relationshipType} <span style={{ color: rel.color }}>●</span>
                  </span>
                  <div>
                    <button
                      onClick={() => openEditForm(rel)}
                      style={{ marginRight: '0.5rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--accent)' }}
                    >
                      ✎
                    </button>
                    <button
                      onClick={() => handleDeleteRelation(rel.id)}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }}
                    >
                      ✕
                    </button>
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </div>
  );
};

export default CharacterRelationGraph;