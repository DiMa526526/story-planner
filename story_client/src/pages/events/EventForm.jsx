/**
 * Форма создания/редактирования события
 * Позволяет выбрать участвующих персонажей и связать с предыдущим событием.
 * При редактировании также отображает и позволяет управлять исходящими связями.
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { useForm } from '../../hooks/useForm';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Textarea from '../../components/ui/Textarea';
import Select from '../../components/ui/Select';
import Card from '../../components/ui/Card';

const initialValues = {
  title: '',
  content: '',
  characterIds: [],
  previousEventId: '',
  choiceText: '',
};

const EventForm = () => {
  const { storyId, eventId } = useParams();
  const navigate = useNavigate();
  const [characters, setCharacters] = useState([]);
  const [events, setEvents] = useState([]);
  const [showLinkForm, setShowLinkForm] = useState(false);
  const [currentStoryId, setCurrentStoryId] = useState(storyId);
  const [outgoingLinks, setOutgoingLinks] = useState([]);
  const { get, post, put, del } = useApi();
  const { values, setValues, handleChange, isSubmitting, handleSubmit: formSubmit } = useForm(initialValues);

  useEffect(() => {
    const loadData = async () => {
      try {
        if (eventId) {
          // Режим редактирования
          const eventData = await get(`/events/${eventId}`);
          setValues({
            title: eventData.title,
            content: eventData.content || '',
            characterIds: eventData.characters?.map(c => c.id) || [],
            previousEventId: '',
            choiceText: '',
          });

          const loadedStoryId = eventData.storyId;
          if (!loadedStoryId) {
            console.error('У события отсутствует storyId');
            return;
          }
          setCurrentStoryId(loadedStoryId);

          const [chars, evts, links] = await Promise.all([
            get(`/stories/${loadedStoryId}/characters`),
            get(`/stories/${loadedStoryId}/events`),
            get(`/events/${eventId}/links/outgoing`).catch(() => []),
          ]);
          setCharacters(chars);
          setEvents(evts.filter(e => e.id !== parseInt(eventId)));
          setOutgoingLinks(links);
        } else if (storyId) {
          // Режим создания
          setCurrentStoryId(storyId);
          const [chars, evts] = await Promise.all([
            get(`/stories/${storyId}/characters`),
            get(`/stories/${storyId}/events`),
          ]);
          setCharacters(chars);
          setEvents(evts);
        }
      } catch (err) {
        console.error('Ошибка загрузки данных формы события:', err);
      }
    };

    loadData();
  }, [eventId, storyId]);

  const handleCharacterToggle = (characterId) => {
    setValues(prev => ({
      ...prev,
      characterIds: prev.characterIds.includes(characterId)
        ? prev.characterIds.filter(id => id !== characterId)
        : [...prev.characterIds, characterId],
    }));
  };

  const handlePreviousEventChange = (e) => {
    handleChange(e);
    setShowLinkForm(e.target.value !== '');
  };

  const handleDeleteLink = async (linkId) => {
    if (window.confirm('Удалить связь?')) {
      try {
        await del(`/events/links/${linkId}`);
        // Обновить список исходящих связей
        const updatedLinks = await get(`/events/${eventId}/links/outgoing`).catch(() => []);
        setOutgoingLinks(updatedLinks);
      } catch (err) {
        console.error('Ошибка удаления связи:', err);
        alert('Не удалось удалить связь');
      }
    }
  };

  const handleSave = async (data) => {
    let createdEventId = eventId;

    if (eventId) {
      // Редактирование события
      await put(`/events/${eventId}`, {
        title: data.title,
        content: data.content,
        characterIds: data.characterIds,
      });
      navigate(`/stories/${currentStoryId}/events`);
    } else {
      // Создание нового события
      const response = await post(`/stories/${storyId}/events`, {
        title: data.title,
        content: data.content,
        characterIds: data.characterIds,
      });
      createdEventId = response.id;

      if (data.previousEventId && data.previousEventId !== '') {
        await post(`/events/${data.previousEventId}/links`, {
          toEventId: createdEventId,
          choiceText: data.choiceText || `Перейти к событию "${data.title}"`,
        });
      }
      if (data.previousEventId && data.previousEventId !== '') {
        navigate(`/stories/${storyId}/events/${createdEventId}`);
      } else {
        navigate(`/stories/${storyId}/events`);
      }
    }
  };

  const eventOptions = events.map(event => ({
    value: event.id,
    label: event.title,
  }));

  const backStoryId = currentStoryId || storyId;

  return (
    <div className="event-form-page">
      <div className="event-form-header">
        <Link to={`/stories/${backStoryId}/events`} className="back-link">
          ← Назад к событиям
        </Link>
        <h1 className="event-form-title">
          {eventId ? 'Редактировать событие' : 'Новое событие'}
        </h1>
      </div>

      <Card className="event-form-card">
        <form onSubmit={(e) => formSubmit(e, handleSave)}>
          <Input
            label="Название события"
            name="title"
            value={values.title}
            onChange={handleChange}
            placeholder="Совет в Ривенделле"
            required
          />

          {!eventId && events.length > 0 && (
            <>
              <Select
                label="Предшествующее событие"
                name="previousEventId"
                value={values.previousEventId}
                options={eventOptions}
                onChange={handlePreviousEventChange}
                placeholder="— Без связи (начало сюжета) —"
              />
              <p className="form-hint">
                Выберите событие, после которого наступает это событие
              </p>
            </>
          )}

          {!eventId && showLinkForm && values.previousEventId && (
            <div className="event-form-field event-form-animated">
              <Input
                label="Текст выбора для пользователя"
                name="choiceText"
                value={values.choiceText}
                onChange={handleChange}
                placeholder="Например: Отправиться в Ривенделл, Принять совет..."
              />
              <p className="form-hint">
                Что увидит пользователь, чтобы перейти к этому событию
              </p>
            </div>
          )}

          <Textarea
            label="Описание события"
            name="content"
            value={values.content}
            onChange={handleChange}
            rows={8}
            placeholder="Что происходит в этом событии? Описание, диалоги, действия..."
          />

          <div className="event-form-field">
            <label className="form-label">Участвующие персонажи</label>
            {characters.length === 0 ? (
              <div className="empty-characters-message">
                Нет персонажей. Сначала создайте персонажей.
              </div>
            ) : (
              <div className="characters-buttons">
                {characters.map(char => (
                  <button
                    key={char.id}
                    type="button"
                    onClick={() => handleCharacterToggle(char.id)}
                    className={`character-btn ${
                      values.characterIds.includes(char.id)
                        ? 'character-btn--active'
                        : 'character-btn--inactive'
                    }`}
                  >
                    {char.name}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Блок управления исходящими связями (только в режиме редактирования) */}
          {eventId && (
            <div className="event-form-field">
              <label className="form-label">Связи с другими событиями</label>
              <div style={{ marginBottom: '1rem' }}>
                <Link
                  to={`/stories/${backStoryId}/events/${eventId}/links/new`}
                  className="add-link"
                >
                  ✛ Добавить связь
                </Link>
              </div>
              {outgoingLinks.length === 0 ? (
                <p className="sidebar-empty-text">Нет исходящих связей</p>
              ) : (
                <div className="links-list">
                  {outgoingLinks.map(link => (
                    <div key={link.id} className="link-item-with-delete">
                      <div>
                        <div className="link-item-title">→ {link.choiceText || link.toEventTitle}</div>
                      </div>
                      <div className="link-actions" style={{ display: 'flex', gap: '0.75rem' }}>
                        <button
                          onClick={() => handleDeleteLink(link.id)}
                          className="delete-link-btn"
                          style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }}
                        >
                          ✕
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          <div className="event-form-actions">
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              {isSubmitting ? 'Сохранение...' : 'Сохранить'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate(`/stories/${backStoryId}/events`)}
            >
              Отмена
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default EventForm;