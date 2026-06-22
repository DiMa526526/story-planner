/**
 * Страница списка событий истории
 * Отображает все события с возможностью создания, просмотра и удаления
 */
import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import { formatDate, truncateText } from '../../utils/helpers';

const Events = () => {
  const { storyId } = useParams();
  const [events, setEvents] = useState([]);
  const [story, setStory] = useState(null);
  const { loading, get, del } = useApi();

  useEffect(() => {
    fetchData();
  }, [storyId]);

  const fetchData = async () => {
    const [storyData, eventsData] = await Promise.all([
      get(`/stories/${storyId}`),
      get(`/stories/${storyId}/events`)
    ]);
    setStory(storyData);
    setEvents(eventsData);
  };

  const handleDelete = async (id, title) => {
    if (window.confirm(`Удалить событие "${title}"?`)) {
      await del(`/events/${id}`);
      setEvents(prev => prev.filter(e => e.id !== id));
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="events-page">
      <div className="events-header">
        <div className="events-header-info">
          <Link to={`/stories/${storyId}`} className="back-link">
            ← Назад к истории
          </Link>
          <h1 className="page-title">События</h1>
          {story && <p className="story-subtitle">{story.title}</p>}
        </div>
        <Link to={`/stories/${storyId}/events/new`}>
          <Button variant="primary" className="events-new-btn">
            ✛ Новое событие
          </Button>
        </Link>
      </div>

      {events.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-icon">
            <span className="icon icon-4xl">◈</span>
          </div>
          <p className="empty-state-text">Нет событий</p>
          <Link to={`/stories/${storyId}/events/new`}>
            <Button variant="primary">Создать первое событие</Button>
          </Link>
        </Card>
      ) : (
        <div className="events-list">
          {events.map((event) => (
            <Card key={event.id} className="event-item">
              <div className="event-item-content">
                <div className="event-item-header">
                  <div className="event-item-icon">
                    <span className="icon icon-lg">◉</span>
                  </div>
                  <h3 className="event-item-title">{event.title}</h3>
                </div>
                <p className="event-item-description">
                  {truncateText(event.content, 100) || 'Нет описания'}
                </p>
                <div className="event-item-meta">
                  <span>⌘ {formatDate(event.createdAt)}</span>
                  {event.characters && (
                    <span>✦ Участвует: {event.characters.length} персонажей</span>
                  )}
                </div>
              </div>
              <div className="event-item-actions">
                <Link to={`/stories/${storyId}/events/${event.id}`} className="event-action-view">
                  Подробнее
                </Link>
                <Link to={`/events/${event.id}/edit`} className="event-action-edit">
                  ✎
                </Link>
                <button onClick={() => handleDelete(event.id, event.title)} className="event-action-delete">
                  ✕
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default Events;