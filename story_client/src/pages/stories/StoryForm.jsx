/**
 * Форма создания/редактирования истории
 * Поддерживает загрузку обложки и выбор жанра
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { useForm } from '../../hooks/useForm';
import { GENRES, VALIDATION_LIMITS } from '../../utils/constants';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import Textarea from '../../components/ui/Textarea';
import Card from '../../components/ui/Card';
import FileUpload from '../../components/common/FileUpload';

const initialValues = {
  title: '',
  shortDescription: '',
  fullDescription: '',
  genre: '',
  coverUrl: '',
};

const StoryForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { loading, get, post, put } = useApi();
  const { values, setValues, handleChange, isSubmitting, handleSubmit: formSubmit } = useForm(initialValues);
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    if (id) {
      fetchStory();
    }
  }, [id]);

  const fetchStory = async () => {
    const story = await get(`/stories/${id}`);
    setValues({
      title: story.title,
      shortDescription: story.shortDescription || '',
      fullDescription: story.fullDescription || '',
      genre: story.genre || '',
      coverUrl: story.coverUrl || '',
    });
  };

  const handleCoverUpload = (url) => {
    setValues(prev => ({ ...prev, coverUrl: url }));
  };

  // Простая клиентская валидация перед отправкой
  const validateLocal = (data) => {
    const errors = {};
    if (data.title && data.title.length > VALIDATION_LIMITS.STORY.TITLE_MAX) {
      errors.title = `Название не может превышать ${VALIDATION_LIMITS.STORY.TITLE_MAX} символов`;
    }
    if (data.shortDescription && data.shortDescription.length > VALIDATION_LIMITS.STORY.SHORT_DESCRIPTION_MAX) {
      errors.shortDescription = `Краткое описание не может превышать ${VALIDATION_LIMITS.STORY.SHORT_DESCRIPTION_MAX} символов`;
    }
    if (data.fullDescription && data.fullDescription.length > VALIDATION_LIMITS.STORY.FULL_DESCRIPTION_MAX) {
      errors.fullDescription = `Полное описание не может превышать ${VALIDATION_LIMITS.STORY.FULL_DESCRIPTION_MAX} символов`;
    }
    if (data.coverUrl && data.coverUrl.length > VALIDATION_LIMITS.STORY.COVER_URL_MAX) {
      errors.coverUrl = `URL обложки слишком длинный`;
    }
    return errors;
  };

  const handleSave = async (data) => {
    setFieldErrors({});
    const localErrors = validateLocal(data);
    if (Object.keys(localErrors).length > 0) {
      setFieldErrors(localErrors);
      return;
    }

    try {
      if (id) {
        await put(`/stories/${id}`, data);
      } else {
        await post('/stories', data);
      }
      navigate('/dashboard');
    } catch (err) {
      // Обработка ошибок валидации с сервера
      if (err.response?.status === 400 && err.response?.data?.message) {
        const serverMessage = err.response.data.message;
        // Парсим сообщение вида "Ошибка валидации: title: ... , shortDescription: ..."
        if (serverMessage.startsWith('Ошибка валидации:')) {
          const parts = serverMessage.substring('Ошибка валидации:'.length).split(',');
          const newErrors = {};
          parts.forEach(part => {
            const colonIndex = part.indexOf(':');
            if (colonIndex !== -1) {
              const field = part.substring(0, colonIndex).trim();
              const errorMsg = part.substring(colonIndex + 1).trim();
              newErrors[field] = errorMsg;
            }
          });
          setFieldErrors(newErrors);
        } else {
          // Общая ошибка — показываем в виде alert или глобальной ошибки
          alert(serverMessage);
        }
      } else {
        console.error(err);
        alert('Произошла ошибка при сохранении. Попробуйте позже.');
      }
    }
  };

  return (
    <div className="story-form-page">
      <h1 className="story-form-title">
        {id ? 'Редактировать историю' : 'Новая история'}
      </h1>

      <Card className="story-form">
        <form onSubmit={(e) => formSubmit(e, handleSave)}>
          <Input
            label="Название"
            name="title"
            value={values.title}
            onChange={handleChange}
            placeholder="Название вашей истории"
            required
            maxLength={VALIDATION_LIMITS.STORY.TITLE_MAX}
            error={fieldErrors.title}
          />

          <Textarea
            label="Краткое описание"
            name="shortDescription"
            value={values.shortDescription}
            onChange={handleChange}
            rows={2}
            placeholder="Краткий анонс истории (отображается в списке)"
            maxLength={VALIDATION_LIMITS.STORY.SHORT_DESCRIPTION_MAX}
          />
          {fieldErrors.shortDescription && <span className="form-error">{fieldErrors.shortDescription}</span>}

          <Textarea
            label="Полное описание"
            name="fullDescription"
            value={values.fullDescription}
            onChange={handleChange}
            rows={5}
            placeholder="Подробное описание сюжета, мира, предыстории..."
            maxLength={VALIDATION_LIMITS.STORY.FULL_DESCRIPTION_MAX}
          />
          {fieldErrors.fullDescription && <span className="form-error">{fieldErrors.fullDescription}</span>}

          <Select
            label="Жанр"
            name="genre"
            value={values.genre}
            options={GENRES}
            onChange={handleChange}
            placeholder="Выберите жанр"
          />

          <div className="story-form-field">
            <label className="form-label">Обложка истории</label>
            <FileUpload
              onUploadSuccess={handleCoverUpload}
              currentImageUrl={values.coverUrl}
              buttonText="Загрузить обложку"
            />
            {fieldErrors.coverUrl && <span className="form-error">{fieldErrors.coverUrl}</span>}
          </div>

          <div className="story-form-actions">
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              {isSubmitting ? 'Сохранение...' : 'Сохранить'}
            </Button>
            <Button type="button" variant="secondary" onClick={() => navigate('/dashboard')}>
              Отмена
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default StoryForm;