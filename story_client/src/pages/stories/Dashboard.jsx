/**
 * Страница со списком историй
 * Отображает список всех историй пользователя с возможностью создания/удаления
 */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import ImageWithFallback from '../../components/common/ImageWithFallback';

const Dashboard = () => {
  const [stories, setStories] = useState([]);
  const { loading, get, del } = useApi();

  useEffect(() => {
    fetchStories();
  }, []);

  const fetchStories = async () => {
    const data = await get('/stories');
    const storiesData = Array.isArray(data) ? data : (data?.content || data?.stories || []);
    setStories(storiesData);
  };

  const handleDelete = async (id, title) => {
    if (window.confirm(`Удалить историю "${title}"? Все данные будут потеряны.`)) {
      await del(`/stories/${id}`);
      setStories(prev => prev.filter(s => s.id !== id));
    }
  };

  if (loading) {
    return <Spinner />;
  }

  if (stories.length === 0) {
    return (
      <div className="dashboard-page">
        <div className="dashboard-header">
          <div>
            <h1 className="dashboard-title">Мои истории</h1>
            <p className="dashboard-subtitle">Всего: 0</p>
          </div>
          <Link to="/stories/new">
            <Button variant="primary" className="dashboard-new-btn">
              ✛ Новая история
            </Button>
          </Link>
        </div>
        <Card className="empty-state">
          <div className="empty-state-icon">
            <span className="icon icon-2xl">◆</span>
          </div>
          <p className="empty-state-text">У вас пока нет историй</p>
          <Link to="/stories/new">
            <Button variant="primary">Создать первую историю</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-title">Мои истории</h1>
          <p className="dashboard-subtitle">Всего: {stories.length}</p>
        </div>
        <Link to="/stories/new">
          <Button variant="primary" className="dashboard-new-btn">
            ✛ Новая история
          </Button>
        </Link>
      </div>

      <div className="dashboard-stories-grid">
        {stories.map((story) => (
          <Card key={story.id} className="dashboard-story-card">
            <ImageWithFallback
              src={story.coverUrl}
              alt={story.title}
              className="dashboard-story-cover"
              fallbackIcon="📖"
            />
            <div className="dashboard-story-content">
              <Link to={`/stories/${story.id}`} className="dashboard-story-link">
                <h3 className="dashboard-story-title">{story.title}</h3>
                <p className="dashboard-story-description">
                  {story.shortDescription || 'Нет описания'}
                </p>
                <div className="dashboard-story-stats">
                  <span>◆ {story.eventsCount || 0} событий</span>
                  <span>✦ {story.charactersCount || 0} персонажей</span>
                </div>
              </Link>
              <div className="dashboard-story-actions">
                <Link to={`/stories/${story.id}/edit`} className="dashboard-story-edit">
                  Редактировать
                </Link>
                <button
                  onClick={() => handleDelete(story.id, story.title)}
                  className="dashboard-story-delete"
                >
                  Удалить
                </button>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default Dashboard;