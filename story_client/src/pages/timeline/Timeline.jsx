/**
 * Страница таймлайна событий
 * Отображает события в виде графа или списка с визуализацией связей
 */
import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import EventGraph from '../../components/graphs/EventGraph';

const Timeline = () => {
  const { storyId } = useParams();
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [links, setLinks] = useState([]);
  const [story, setStory] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const { loading, get } = useApi();

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

    const linksPromises = eventsData.map(event =>
      get(`/events/${event.id}/links/outgoing`).catch(() => [])
    );
    const allLinks = await Promise.all(linksPromises);
    setLinks(allLinks.flat());
  };

  const getEventLinks = (eventId) => links.filter(link => link.fromEventId === eventId);
  const getIncomingLinks = (eventId) => links.filter(link => link.toEventId === eventId);

  if (loading) return <Spinner />;

  return (
    <div className="timeline-page">
      <div className="timeline-header">
        <Link to={`/stories/${storyId}`} className="back-link">
          ← Назад к истории
        </Link>
        <div className="timeline-header-main">
          <div>
            <h1 className="page-title">Таймлайн событий</h1>
            {story && <p className="story-subtitle">{story.title}</p>}
          </div>
          <div className="timeline-view-toggle">
            <button
              onClick={() => navigate(`/stories/${storyId}/timeline`)}
              className="timeline-toggle-btn timeline-toggle-btn--active"
            >
              ⌘ Граф
            </button>
            <button
              onClick={() => navigate(`/stories/${storyId}/events`)}
              className="timeline-toggle-btn"
            >
              ◈ Список
            </button>
          </div>
        </div>
      </div>

      {events.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-icon">
            <span className="icon icon-4xl">◆</span>
          </div>
          <p className="empty-state-text">Нет событий для отображения</p>
          <Link to={`/stories/${storyId}/events/new`}>
            <Button variant="primary">Создать первое событие</Button>
          </Link>
        </Card>
      ) : (
        <EventGraph storyId={storyId} />
      )}
    </div>
  );
};

export default Timeline;