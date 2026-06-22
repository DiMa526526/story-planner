/**
 * Страница истории изменений отношений между персонажами
 * Отображает хронологию изменений типа отношений по событиям
 */
import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { useForm } from '../../hooks/useForm';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import Select from '../../components/ui/Select';
import Input from '../../components/ui/Input';

const initialValues = {
  eventId: '',
  relationshipType: '',
  color: '#6366f1'
};

const RelationshipHistory = () => {
  const { relationshipId, storyId } = useParams();
  const [relationship, setRelationship] = useState(null);
  const [history, setHistory] = useState([]);
  const [events, setEvents] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');
  const { loading, get, post, del } = useApi();
  const { values, setValues, handleChange, isSubmitting, handleSubmit: formSubmit, resetForm } = useForm(initialValues);

  useEffect(() => {
    fetchData();
  }, [relationshipId, storyId]);

  const fetchData = async () => {
    setError('');
    try {
      const [relData, historyData, eventsData] = await Promise.all([
        get(`/stories/${storyId}/relationships/${relationshipId}`),
        get(`/relationships/${relationshipId}/history`),
        get(`/stories/${storyId}/events`)
      ]);
      setRelationship(relData);
      // Сортируем историю по eventId (порядок событий)
      const sortedHistory = [...historyData].sort((a, b) => a.eventId - b.eventId);
      setHistory(sortedHistory);
      setEvents(eventsData);
    } catch (err) {
      console.error('Ошибка загрузки данных:', err);
      setError('Не удалось загрузить данные. Попробуйте обновить страницу.');
    }
  };

  const handleAddHistory = async (data) => {
    setError('');
    try {
      await post(`/relationships/${relationshipId}/history`, data);
      setShowForm(false);
      resetForm();
      await fetchData();
    } catch (err) {
      console.error('Ошибка добавления записи:', err);
      setError(err.response?.data?.message || 'Ошибка добавления записи');
    }
  };

  const handleDeleteHistory = async (id) => {
    if (!window.confirm('Удалить запись?')) return;
    setError('');
    try {
      await del(`/history/${id}`);
      await fetchData();
    } catch (err) {
      console.error('Ошибка удаления записи:', err);
      setError(err.response?.data?.message || 'Ошибка удаления записи');
    }
  };

  const eventOptions = events.map(event => ({
    value: event.id,
    label: event.title
  }));

  if (loading) return <Spinner />;
  if (!relationship) return null;

  return (
    <div className="history-page">
      <div className="history-header">
        <Link to={`/stories/${storyId}/relationships`} className="back-link">
          ← Назад к отношениям
        </Link>
        <h1 className="history-title">
          История отношений: {relationship.character1?.name} ⟷ {relationship.character2?.name}
        </h1>
      </div>

      <div className="history-grid">
        <div className="history-main">
          <Card>
            <div className="history-card-header">
              <h2 className="sidebar-title">
                <span className="icon icon-md">◈</span> Хронология изменений
              </h2>
              <Button variant="primary" onClick={() => setShowForm(!showForm)}>
                ✛ Добавить запись
              </Button>
            </div>

            {error && (
              <div className="error-message" style={{ marginBottom: '1rem' }}>
                ⚠️ {error}
              </div>
            )}

            {showForm && (
              <form onSubmit={(e) => formSubmit(e, handleAddHistory)} className="history-form">
                <h3 className="history-form-title">Новая запись</h3>
                <div className="history-form-fields">
                  <Select
                    name="eventId"
                    value={values.eventId}
                    options={eventOptions}
                    onChange={handleChange}
                    placeholder="Выберите событие"
                    required
                  />
                  <Input
                    name="relationshipType"
                    value={values.relationshipType}
                    onChange={handleChange}
                    placeholder="Тип отношений (например: друзья, враги, союзники)"
                    required
                  />
                  <div className="history-form-color">
                    <span>Цвет:</span>
                    <input
                      type="color"
                      name="color"
                      value={values.color}
                      onChange={handleChange}
                      className="history-color-input"
                    />
                  </div>
                  <div className="history-form-actions">
                    <Button type="submit" variant="primary" isLoading={isSubmitting}>
                      Сохранить
                    </Button>
                    <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>
                      Отмена
                    </Button>
                  </div>
                </div>
              </form>
            )}

            {history.length === 0 ? (
              <p className="history-empty-text">Нет записей об изменениях отношений</p>
            ) : (
              <div className="timeline">
                {history.map((item) => (
                  <div key={item.id} className="timeline-item">
                    <div className="timeline-dot" style={{ backgroundColor: item.color }}></div>
                    <div className="timeline-content">
                      <div className="timeline-content-header">
                        <div>
                          <Link to={`/stories/${storyId}/events/${item.eventId}`} className="timeline-link">
                            {item.eventTitle}
                          </Link>
                          <p className="timeline-type">
                            Тип отношений: <span className="timeline-type-value">{item.relationshipType}</span>
                          </p>
                        </div>
                        <button onClick={() => handleDeleteHistory(item.id)} className="timeline-delete-btn">
                          Удалить
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        <div className="history-sidebar">
          <Card>
            <h2 className="sidebar-title">
              <span className="icon icon-md">ℹ</span> О связях
            </h2>
            <p className="history-info-text">
              Здесь можно отслеживать, как менялись отношения между персонажами
              в процессе развития сюжета. Каждая запись привязана к конкретному событию.
            </p>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default RelationshipHistory;