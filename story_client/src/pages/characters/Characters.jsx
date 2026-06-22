/**
 * Страница списка персонажей истории
 * Отображает всех персонажей с возможностью создания, редактирования и удаления
 */
import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import ImageWithFallback from '../../components/common/ImageWithFallback';

const Characters = () => {
  const { storyId } = useParams();
  const [characters, setCharacters] = useState([]);
  const [story, setStory] = useState(null);
  const { loading, get, del } = useApi();

  useEffect(() => {
    fetchData();
  }, [storyId]);

  const fetchData = async () => {
    const [storyData, charactersData] = await Promise.all([
      get(`/stories/${storyId}`),
      get(`/stories/${storyId}/characters`)
    ]);
    setStory(storyData);
    setCharacters(charactersData);
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Удалить персонажа "${name}"?`)) {
      await del(`/characters/${id}`);
      setCharacters(prev => prev.filter(c => c.id !== id));
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="characters-page">
      <div className="characters-header">
        <div className="characters-header-info">
          <Link to={`/stories/${storyId}`} className="back-link">
            ← Назад к истории
          </Link>
          <h1 className="page-title">Персонажи</h1>
          {story && <p className="story-subtitle">{story.title}</p>}
        </div>
        <Link to={`/stories/${storyId}/characters/new`}>
          <Button variant="primary">
            ✛ Новый персонаж
          </Button>
        </Link>
      </div>

      {characters.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-icon">
            <span className="icon icon-4xl">✦</span>
          </div>
          <p className="empty-state-text">Нет персонажей</p>
          <Link to={`/stories/${storyId}/characters/new`}>
            <Button variant="primary">Создать первого персонажа</Button>
          </Link>
        </Card>
      ) : (
        <div className="characters-grid">
          {characters.map(char => (
            <Card key={char.id} className="character-card">
              <Link to={`/characters/${char.id}`} className="character-card-link">
                <div className="character-card-content">
                  <ImageWithFallback
                    src={char.imageUrl}
                    alt={char.name}
                    className="character-avatar"
                    fallbackIcon="👤"
                  />
                  <h3 className="character-name">{char.name}</h3>
                  <p className="character-description">
                    {char.description || 'Нет описания'}
                  </p>
                </div>
              </Link>
              <div className="character-actions">
                <Link to={`/characters/${char.id}/edit`} className="character-action-edit">
                  Редактировать
                </Link>
                <button
                  onClick={() => handleDelete(char.id, char.name)}
                  className="character-action-delete"
                >
                  Удалить
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default Characters;