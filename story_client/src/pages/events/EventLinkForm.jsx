/**
 * Форма создания/редактирования связи между событиями
 * Позволяет задать целевое событие и текст выбора для пользователя
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { useForm } from '../../hooks/useForm';
import Button from '../../components/ui/Button';
import Select from '../../components/ui/Select';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';

const initialValues = {
  toEventId: '',
  choiceText: '',
};

const EventLinkForm = () => {
  const { storyId, eventId, linkId } = useParams();
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const { get, post, put } = useApi();
  const { values, setValues, handleChange, isSubmitting, handleSubmit: formSubmit } = useForm(initialValues);

  useEffect(() => {
    if (storyId) {
      fetchEvents();
    }
    if (linkId) {
      fetchLink();
    }
  }, [storyId, linkId]);

  const fetchEvents = async () => {
    const data = await get(`/stories/${storyId}/events`);
    setEvents(data.filter(e => e.id !== parseInt(eventId)));
  };

  const fetchLink = async () => {
    const link = await get(`/events/links/${linkId}`);
    setValues({
      toEventId: link.toEventId,
      choiceText: link.choiceText || '',
    });
  };

  const handleSave = async (data) => {
    if (linkId) {
      await put(`/events/links/${linkId}`, data);
    } else {
      await post(`/events/${eventId}/links`, data);
    }
    navigate(`/events/${eventId}`);
  };

  const eventOptions = events.map(event => ({
    value: event.id,
    label: event.title
  }));

  return (
    <div className="event-link-form-page">
      <div className="event-link-form-header">
        <Link to={`/events/${eventId}`} className="back-link">
          ← Назад к событию
        </Link>
        <h1 className="event-link-form-title">
          {linkId ? 'Редактировать связь' : 'Новая связь событий'}
        </h1>
      </div>

      <Card className="event-link-form-card">
        <form onSubmit={(e) => formSubmit(e, handleSave)}>
          <Select
            label="Целевое событие"
            name="toEventId"
            value={values.toEventId}
            options={eventOptions}
            onChange={handleChange}
            placeholder="Выберите событие"
            required
          />

          <Input
            label="Текст выбора (для читателя)"
            name="choiceText"
            value={values.choiceText}
            onChange={handleChange}
            placeholder="Например: Пойти налево, Сражаться с драконом..."
          />
          <p className="form-hint">
            Если оставить пустым, будет показано название события
          </p>

          <div className="event-link-form-actions">
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              {isSubmitting ? 'Сохранение...' : 'Сохранить'}
            </Button>
            <Button type="button" variant="secondary" onClick={() => navigate(`/events/${eventId}`)}>
              Отмена
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default EventLinkForm;