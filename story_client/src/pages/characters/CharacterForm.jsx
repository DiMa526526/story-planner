/**
 * Форма создания/редактирования персонажа
 * Включает загрузку портрета и отображение графа связей
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { useForm } from '../../hooks/useForm';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Textarea from '../../components/ui/Textarea';
import Card from '../../components/ui/Card';
import FileUpload from '../../components/common/FileUpload';
import CharacterRelationGraph from '../../components/graphs/CharacterRelationGraph';

const initialValues = {
  name: '',
  description: '',
  imageUrl: '',
};

const CharacterForm = () => {
  const { storyId, characterId } = useParams();
  const navigate = useNavigate();
  const [currentStoryId, setCurrentStoryId] = useState(storyId);
  const { loading: apiLoading, get, post, put, del } = useApi();
  const { values, setValues, handleChange, isSubmitting, handleSubmit: formSubmit } = useForm(initialValues);

  useEffect(() => {
    if (characterId) {
      fetchCharacter();
    }
  }, [characterId]);

  const fetchCharacter = async () => {
    const char = await get(`/characters/${characterId}`);
    setCurrentStoryId(char.storyId);
    setValues({
      name: char.name,
      description: char.description || '',
      imageUrl: char.imageUrl || '',
    });
  };

  const handleImageUpload = (url) => {
    setValues(prev => ({ ...prev, imageUrl: url }));
  };

  const handleSave = async (data) => {
    if (characterId) {
      await put(`/characters/${characterId}`, data);
    } else {
      await post(`/stories/${storyId}/characters`, data);
    }
    navigate(`/stories/${currentStoryId || storyId}/characters`);
  };

  const handleDelete = async () => {
    if (window.confirm(`Удалить персонажа "${values.name}"?`)) {
      await del(`/characters/${characterId}`);
      navigate(`/stories/${currentStoryId || storyId}/characters`);
    }
  };

  const storyIdForBackLink = currentStoryId || storyId;

  return (
    <div className="character-form">
      <div className="character-form-header">
        <Link to={`/stories/${storyIdForBackLink}/characters`} className="back-link">
          ← Назад к персонажам
        </Link>
        <h1 className="character-form-title">
          {characterId ? 'Редактировать персонажа' : 'Новый персонаж'}
        </h1>
      </div>

      <div className="character-form-content">
        <Card className="character-form-card">
          <form onSubmit={(e) => formSubmit(e, handleSave)} className="character-form-inner">
            <div className="avatar-section">
              <div className="avatar-container">
                {values.imageUrl ? (
                  <img src={values.imageUrl} alt={values.name} className="avatar-image" />
                ) : (
                  <span className="avatar-placeholder">👤</span>
                )}
              </div>
              <div className="avatar-upload">
                <FileUpload
                  onUploadSuccess={handleImageUpload}
                  buttonText="Загрузить портрет"
                  currentImageUrl={values.imageUrl}
                  centered={true}
                />
              </div>
            </div>

            <div className="form-fields">
              <Input
                label="Имя персонажа"
                name="name"
                value={values.name}
                onChange={handleChange}
                placeholder="Гарри Поттер"
                required
              />

              <Textarea
                label="Описание"
                name="description"
                value={values.description}
                onChange={handleChange}
                rows={5}
                placeholder="Характер, внешность, биография..."
              />

              <div className="form-actions">
                <Button type="submit" variant="primary" isLoading={isSubmitting}>
                  {isSubmitting ? 'Сохранение...' : 'Сохранить'}
                </Button>
                {characterId && (
                  <Button type="button" variant="danger" onClick={handleDelete}>
                    Удалить персонажа
                  </Button>
                )}
              </div>
            </div>
          </form>
        </Card>

        {storyIdForBackLink && characterId && (
          <Card className="character-relations-card">
            <h2 className="relations-title">
              <span className="icon icon-md">♡</span> Связи с другими персонажами
            </h2>
            <CharacterRelationGraph
              characterId={characterId}
              storyId={storyIdForBackLink}
            />
            <div style={{ marginTop: '1rem', textAlign: 'center' }}>
              <Link to={`/stories/${storyIdForBackLink}/relationships`} className="back-link">
                Управление всеми связями →
              </Link>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default CharacterForm;