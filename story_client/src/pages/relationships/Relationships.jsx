/**
 * Страница управления связями между персонажами
 * Отображает все связи и позволяет создавать новые
 */
import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import Select from '../../components/ui/Select';

const Relationships = () => {
  const { storyId } = useParams();
  const [relationships, setRelationships] = useState([]);
  const [characters, setCharacters] = useState([]);
  const [story, setStory] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [selectedChar1, setSelectedChar1] = useState('');
  const [selectedChar2, setSelectedChar2] = useState('');
  const [error, setError] = useState('');
  const [filterCharacterId, setFilterCharacterId] = useState('');
  const { loading, get, post, del } = useApi();

  useEffect(() => {
    fetchData();
  }, [storyId]);

  const fetchData = async () => {
    const [storyData, charsData, relsData] = await Promise.all([
      get(`/stories/${storyId}`),
      get(`/stories/${storyId}/characters`),
      get(`/stories/${storyId}/relationships`)
    ]);
    setStory(storyData);
    setCharacters(charsData);
    setRelationships(relsData);
  };

  const handleCreateRelationship = async (e) => {
    e.preventDefault();
    if (selectedChar1 === selectedChar2) {
      setError('Нельзя создать связь персонажа с самим собой');
      return;
    }
    setError('');
    try {
      await post(`/stories/${storyId}/relationships`, {
        character1Id: parseInt(selectedChar1),
        character2Id: parseInt(selectedChar2)
      });
      setShowForm(false);
      setSelectedChar1('');
      setSelectedChar2('');
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка создания связи');
    }
  };

  const handleDeleteRelationship = async (id) => {
    if (window.confirm('Удалить связь между персонажами?')) {
      await del(`/relationships/${id}`);
      fetchData();
    }
  };

  const filteredRelationships = filterCharacterId
    ? relationships.filter(rel =>
        rel.character1?.id === parseInt(filterCharacterId) ||
        rel.character2?.id === parseInt(filterCharacterId)
      )
    : relationships;

  const characterOptions = characters.map(char => ({
    value: char.id,
    label: char.name
  }));

  const filterOptions = [
    { value: '', label: 'Все персонажи' },
    ...characterOptions
  ];

  if (loading) return <Spinner />;

  return (
    <div className="relationships-page">
      <div className="relationships-header">
        <Link to={`/stories/${storyId}`} className="back-link">
          ← Назад к истории
        </Link>
        <h1 className="page-title">Отношения персонажей</h1>
        {story && <p className="story-subtitle">{story.title}</p>}
      </div>

      <Card>
        <div className="relationships-card-header">
          <h2 className="sidebar-title">Все связи</h2>
          <Button variant="primary" onClick={() => setShowForm(!showForm)}>
            ✛ Новая связь
          </Button>
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <Select
            label="Фильтр по персонажу"
            name="filterCharacter"
            value={filterCharacterId}
            options={filterOptions}
            onChange={(e) => setFilterCharacterId(e.target.value)}
            placeholder="Все персонажи"
          />
        </div>

        {showForm && (
          <form onSubmit={handleCreateRelationship} className="relationships-form">
            <h3 className="relationships-form-title">Создать связь</h3>
            <div className="relationships-form-grid">
              <Select
                name="character1"
                value={selectedChar1}
                options={characterOptions}
                onChange={(e) => setSelectedChar1(e.target.value)}
                placeholder="Выберите персонажа 1"
                required
              />
              <Select
                name="character2"
                value={selectedChar2}
                options={characterOptions}
                onChange={(e) => setSelectedChar2(e.target.value)}
                placeholder="Выберите персонажа 2"
                required
              />
            </div>
            {error && <p className="relationships-form-error">{error}</p>}
            <div className="relationships-form-actions">
              <Button type="submit" variant="primary">Сохранить</Button>
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>
                Отмена
              </Button>
            </div>
          </form>
        )}

        {filteredRelationships.length === 0 ? (
          <p className="relationships-empty-text">Нет связей между персонажами</p>
        ) : (
          <div className="relationships-list">
            {filteredRelationships.map(rel => (
              <div key={rel.id} className="relationship-item">
                <div className="relationship-item-content">
                  <span className="relationship-name">{rel.character1?.name}</span>
                  <span className="relationship-arrow">⟷</span>
                  <span className="relationship-name">{rel.character2?.name}</span>
                  <Link to={`/stories/${storyId}/relationships/${rel.id}/history`} className="relationship-history-link">
                    ◈ история
                  </Link>
                </div>
                <button onClick={() => handleDeleteRelationship(rel.id)} className="relationship-delete-btn">
                  ✕
                </button>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
};

export default Relationships;