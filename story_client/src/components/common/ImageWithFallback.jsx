/**
 * Компонент изображения с fallback-иконкой
 * Автоматически показывает иконку при ошибке загрузки
 */
import { useState } from 'react';

const ImageWithFallback = ({ src, alt, className, fallbackIcon = "📷" }) => {
  const [hasError, setHasError] = useState(false);

  if (!src || hasError) {
    return <div className={`image-fallback ${className || ''}`}>{fallbackIcon}</div>;
  }

  return (
    <img
      src={src}
      alt={alt}
      className={className}
      onError={() => setHasError(true)}
    />
  );
};

export default ImageWithFallback;