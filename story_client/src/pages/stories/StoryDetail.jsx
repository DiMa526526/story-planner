/**
 * Детальная страница истории
 * Отображает информацию, статистику и ссылки на связанные разделы
 */
import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { GENRE_LABELS } from '../../utils/constants';
import { truncateText } from '../../utils/helpers';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';

const StoryDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [story, setStory] = useState(null);
  const [stats, setStats] = useState({
    characters: [],
    events: [],
    relationships: [],
    eventLinks: []
  });
  const { loading, get, del } = useApi();

  useEffect(() => {
    fetchAllData();
  }, [id]);

  const fetchAllData = async () => {
    try {
      const [storyData, charactersData, eventsData, relationshipsData] = await Promise.all([
        get(`/stories/${id}`),
        get(`/stories/${id}/characters`),
        get(`/stories/${id}/events`),
        get(`/stories/${id}/relationships`)
      ]);

      setStory(storyData);
      
      const linksPromises = eventsData.map(event =>
        get(`/events/${event.id}/links/outgoing`).catch(() => [])
      );
      const allLinks = await Promise.all(linksPromises);
      const eventLinks = allLinks.flat();

      setStats({
        characters: charactersData,
        events: eventsData,
        relationships: relationshipsData,
        eventLinks
      });
    } catch {
      navigate('/dashboard');
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Удалить историю? Все данные будут потеряны.')) {
      await del(`/stories/${id}`);
      navigate('/dashboard');
    }
  };

  if (loading) return <Spinner />;
  if (!story) return null;

  const statCards = [
    { icon: '✦', label: 'Персонажей', count: stats.characters.length, link: `/stories/${id}/characters`, color: 'stat-card--purple' },
    { icon: '◈', label: 'Событий', count: stats.events.length, link: `/stories/${id}/events`, color: 'stat-card--blue' },
    { icon: '⌘', label: 'Связей событий', count: stats.eventLinks.length, link: `/stories/${id}/timeline`, color: 'stat-card--green' },
    { icon: '♡', label: 'Связей персонажей', count: stats.relationships.length, link: `/stories/${id}/relationships`, color: 'stat-card--pink' }
  ];

  return (
    <div className="story-detail-page">
      <div className="story-detail-header">
        <div className="story-detail-info">
          <h1 className="story-detail-title">{story.title}</h1>
          {story.genre && (
            <span className="story-detail-genre">
              {GENRE_LABELS[story.genre] || story.genre}
            </span>
          )}
          <p className="story-detail-description">
            {story.fullDescription || story.shortDescription}
          </p>
        </div>
        <div className="story-detail-actions">
          <Link to={`/stories/${id}/edit`}>
            <Button variant="secondary">Редактировать</Button>
          </Link>
          <Button variant="danger" onClick={handleDelete}>Удалить</Button>
        </div>
      </div>

      <div className="stats-grid">
        {statCards.map((stat) => (
          <Link key={stat.label} to={stat.link} className={`stat-card ${stat.color}`}>
            <div className="stat-card-icon">
              <span className="icon">{stat.icon}</span>
            </div>
            <div className="stat-card-count">{stat.count}</div>
            <div className="stat-card-label">{stat.label}</div>
          </Link>
        ))}
      </div>

      <div className="story-detail-sections">
        <Card className="story-section">
          <div className="story-section-header">
            <h2 className="story-section-title">
              <span className="icon icon-md">✦</span> Персонажи
            </h2>
            <Link to={`/stories/${id}/characters/new`} className="story-section-add">
              ✛ Добавить
            </Link>
          </div>
          {stats.characters.length === 0 ? (
            <p className="story-section-empty">Нет персонажей</p>
          ) : (
            <div className="story-section-list">
              {stats.characters.map(char => (
                <Link key={char.id} to={`/characters/${char.id}`} className="story-section-item">
                  <span className="story-section-item-name">{char.name}</span>
                  <span className="story-section-item-preview">
                    {truncateText(char.description, 50)}
                  </span>
                </Link>
              ))}
            </div>
          )}
        </Card>

        <Card className="story-section">
          <div className="story-section-header">
            <h2 className="story-section-title">
              <span className="icon icon-md">◈</span> События
            </h2>
            <Link to={`/stories/${id}/events/new`} className="story-section-add">
              ✛ Добавить
            </Link>
          </div>
          {stats.events.length === 0 ? (
            <p className="story-section-empty">Нет событий</p>
          ) : (
            <div className="story-section-list">
              {stats.events.map(event => (
                <Link key={event.id} to={`/stories/${id}/events/${event.id}`} className="story-section-item story-section-item--block">
                  <div className="story-section-item-name">{event.title}</div>
                  <div className="story-section-item-preview">{event.content?.slice(0, 80)}</div>
                </Link>
              ))}
            </div>
          )}
        </Card>
      </div>

      <div className="story-footer-links">
        <Link to={`/stories/${id}/relationships`} className="story-footer-link story-footer-link--purple">
          ♡ Отношения персонажей
        </Link>
        <Link to={`/stories/${id}/timeline`} className="story-footer-link story-footer-link--green">
          ⌘ Таймлайн событий
        </Link>
      </div>
    </div>
  );
};

export default StoryDetail;