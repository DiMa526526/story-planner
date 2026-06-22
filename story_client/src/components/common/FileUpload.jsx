/**
 * Компонент для загрузки файлов на сервер
 * Поддерживает изображения с предпросмотром
 */
import { useState } from 'react';
import api from '../../services/api';

const FileUpload = ({
  onUploadSuccess,
  onUploadError,
  accept = "image/*",
  buttonText = "Загрузить изображение",
  currentImageUrl = null,
  centered = false
}) => {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(currentImageUrl);
  const [error, setError] = useState('');

  const handleFileSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setError('');

    if (!file.type.startsWith('image/')) {
      const errMsg = 'Пожалуйста, выберите изображение';
      setError(errMsg);
      onUploadError?.(errMsg);
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      const errMsg = 'Размер файла не должен превышать 5MB';
      setError(errMsg);
      onUploadError?.(errMsg);
      return;
    }

    setUploading(true);

    const formData = new FormData();
    formData.append('image', file);

    try {
      const response = await api.post('/api/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const imageUrl = response.data.url;
      setPreview(imageUrl);
      onUploadSuccess?.(imageUrl);
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Ошибка загрузки изображения';
      setError(errMsg);
      onUploadError?.(errMsg);
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleRemove = () => {
    setPreview(null);
    onUploadSuccess?.('');
  };

  return (
    <div className={`file-upload ${centered ? 'file-upload--centered' : ''}`}>
      {error && <div className="file-upload-error">{error}</div>}

      {preview && (
        <div className="file-upload-preview">
          <img
            src={preview}
            alt="Preview"
            className="file-upload-preview-img"
            onError={() => {
              setPreview(null);
              setError('Не удалось загрузить изображение');
            }}
          />
          <button
            type="button"
            onClick={handleRemove}
            className="file-upload-remove-btn"
            aria-label="Удалить изображение"
          >
            ✕
          </button>
        </div>
      )}

      <div className={centered ? 'file-upload-btn-wrapper--centered' : 'file-upload-btn-wrapper'}>
        <label className={`file-upload-label ${uploading ? 'file-upload-label--disabled' : ''}`}>
          <input
            type="file"
            className="file-upload-input"
            accept={accept}
            onChange={handleFileSelect}
            disabled={uploading}
          />
          <span className="btn-secondary">
            {uploading ? <>⟳ Загрузка...</> : buttonText}
          </span>
        </label>
      </div>

      {!preview && !uploading && (
        <p className="file-upload-hint">
          Поддерживаются JPG, PNG, GIF. Максимальный размер: 5MB
        </p>
      )}
    </div>
  );
};

export default FileUpload;