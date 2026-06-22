/**
 * Детальная страница события
 * Отображает информацию о событии, участвующих персонажах и связях
 */
import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import { formatDate } from '../../utils/helpers';

const EventDetail = () => {
  const { eventId, storyId } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [outgoingLinks, setOutgoingLinks] = useState([]);
  const [incomingLinks, setIncomingLinks] = useState([]);
  const [currentStoryId, setCurrentStoryId] = useState(storyId);
  const [editingLink, setEditingLink] = useState(null);
  const [allEvents, setAllEvents] = useState([]);
  const [editFormData, setEditFormData] = useState({ toEventId: '', choiceText: '' });
  const { loading, get, post, put, del } = useApi();

  useEffect(() => {
    fetchEvent();
  }, [eventId]);

  const fetchEvent = async () => {
    try {
      const eventData = await get(`/events/${eventId}`);
      setEvent(eventData);
      if (!currentStoryId && eventData.storyId) {
        setCurrentStoryId(eventData.storyId);
      }
      await fetchLinks(eventId);
      if (currentStoryId || eventData.storyId) {
        const eventsData = await get(`/stories/${currentStoryId || eventData.storyId}/events`);
        setAllEvents(eventsData.filter(e => e.id !== parseInt(eventId)));
      }
    } catch {
      navigate(-1);
    }
  };

  const fetchLinks = async (id) => {
    const [outgoing, incoming] = await Promise.all([
      get(`/events/${id}/links/outgoing`).catch(() => []),
      get(`/events/${id}/links/incoming`).catch(() => [])
    ]);
    // Исключаем циклические ссылки
    const validOutgoing = outgoing.filter(link => link.fromEventId !== link.toEventId);
    const validIncoming = incoming.filter(link => link.fromEventId !== link.toEventId);
    setOutgoingLinks(validOutgoing);
    setIncomingLinks(validIncoming);
  };

  const handleDeleteLink = async (linkId) => {
    if (window.confirm('Удалить связь?')) {
      await del(`/events/links/${linkId}`);
      fetchLinks(eventId);
    }
  };

  const handleEditLink = (link) => {
    setEditingLink(link);
    setEditFormData({
      toEventId: link.toEventId,
      choiceText: link.choiceText || ''
    });
  };

  const handleUpdateLink = async () => {
    if (!editingLink) return;
    if (parseInt(editFormData.toEventId) === parseInt(eventId)) {
      alert('Нельзя создать ссылку события на само себя');
      return;
    }
    try {
      await put(`/events/links/${editingLink.id}`, editFormData);
      setEditingLink(null);
      fetchLinks(eventId);
    } catch (err) {
      alert(err.response?.data?.message || 'Ошибка обновления связи');
    }
  };

  // Получаем название события по ID
  const getEventTitle = (eventId) => {
    const found = allEvents.find(e => e.id === eventId);
    return found ? found.title : 'Событие не найдено';
  };

  const eventOptions = allEvents.map(event => ({
    value: event.id,
    label: event.title
  }));

  if (loading) return <Spinner />;
  if (!event) return null;

  const effectiveStoryId = currentStoryId || event?.storyId;

  if (!effectiveStoryId) {
    return (
      <div className="event-detail-error">
        <p className="event-detail-error-text">Ошибка: ID истории не найден</p>
        <Button variant="primary" onClick={() => navigate(-1)}>
          ← Вернуться назад
        </Button>
      </div>
    );
  }

  return (
    <div className="event-detail-page">
      <div className="event-detail-back">
        <Link to={`/stories/${effectiveStoryId}/events`} className="back-link">
          ← Назад к событиям
        </Link>
      </div>

      {/* Модальное окно для редактирования связи */}
      {editingLink && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <Card style={{ maxWidth: '500px', width: '90%' }}>
            <h3>Редактировать связь</h3>
            <Select
              label="Целевое событие"
              name="toEventId"
              value={editFormData.toEventId}
              options={eventOptions}
              onChange={(e) => setEditFormData({ ...editFormData, toEventId: e.target.value })}
              placeholder="Выберите событие"
              required
            />
            <Input
              label="Текст выбора (для читателя)"
              name="choiceText"
              value={editFormData.choiceText}
              onChange={(e) => setEditFormData({ ...editFormData, choiceText: e.target.value })}
              placeholder="Например: Пойти налево, Сражаться с драконом..."
            />
            <div className="form-actions" style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <Button variant="primary" onClick={handleUpdateLink}>Сохранить</Button>
              <Button variant="secondary" onClick={() => setEditingLink(null)}>Отмена</Button>
            </div>
          </Card>
        </div>
      )}

      <div className="event-detail-grid">
        <div className="event-detail-main">
          <Card>
            <div className="event-detail-header">
              <h1 className="event-detail-title">{event.title}</h1>
              <div className="event-detail-actions">
                <Link to={`/events/${eventId}/edit`} className="edit-link">
                  ✎ Редактировать
                </Link>
              </div>
            </div>
            <div className="event-detail-content">
              <p className="event-detail-text">{event.content || 'Нет описания'}</p>
            </div>
            <div className="event-detail-meta">
              Создано: {formatDate(event.createdAt)}
            </div>
          </Card>
        </div>

        <div className="event-detail-sidebar">
          <Card>
            <h2 className="sidebar-title">
              <span className="icon icon-md">◉</span> Персонажи в событии
            </h2>
            {event.characters?.length === 0 ? (
              <p className="sidebar-empty-text">Нет персонажей</p>
            ) : (
              <div className="characters-list">
                {event.characters?.map(char => (
                  <Link key={char.id} to={`/characters/${char.id}`} className="character-badge">
                    {char.name}
                  </Link>
                ))}
              </div>
            )}
          </Card>

          <Card>
            <h2 className="sidebar-title">
              <span className="icon icon-md">←</span> Откуда можно прийти
            </h2>
            {incomingLinks.length === 0 ? (
              <p className="sidebar-empty-text">Нет входящих связей</p>
            ) : (
              <div className="links-list">
                {incomingLinks.map(link => {
                  // Показываем название ПРЕДЫДУЩЕГО события (откуда пришли)
                  const sourceEventTitle = getEventTitle(link.fromEventId);
                  return (
                    <div key={link.id} className="link-item">
                      <Link to={`/events/${link.fromEventId}`} className="link-item-title">
                        {sourceEventTitle}
                      </Link>
                      {link.choiceText && (
                        <div className="link-item-subtitle" style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                          Выбор: "{link.choiceText}"
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </Card>

          <Card>
            <div className="flex justify-between items-center mb-3">
              <h2 className="sidebar-title">
                <span className="icon icon-md">→</span> Куда можно перейти
              </h2>
              {effectiveStoryId && (
                <Link to={`/stories/${effectiveStoryId}/events/${eventId}/links/new`} className="add-link">
                  ✛ Добавить связь
                </Link>
              )}
            </div>
            {outgoingLinks.length === 0 ? (
              <p className="sidebar-empty-text">Нет исходящих связей</p>
            ) : (
              <div className="links-list">
                {outgoingLinks.map(link => {
                  const targetEventTitle = getEventTitle(link.toEventId);
                  return (
                    <div key={link.id} className="link-item-with-delete">
                      <div style={{ flex: 1 }}>
                        <Link to={`/events/${link.toEventId}`} className="link-item-title">
                          {targetEventTitle}
                        </Link>
                        {link.choiceText && (
                          <div className="link-item-subtitle" style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                            Текст выбора: "{link.choiceText}"
                          </div>
                        )}
                      </div>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button
                          onClick={() => handleEditLink(link)}
                          className="edit-link-btn"
                          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--accent)' }}
                          title="Редактировать связь"
                        >
                          ✎
                        </button>
                        <button onClick={() => handleDeleteLink(link.id)} className="delete-link-btn" title="Удалить связь">
                          ✕
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
};

export default EventDetail;