/**
 * Глобальные константы приложения
 */

export const GENRES = [
  { value: 'fantasy', label: 'Фэнтези' },
  { value: 'sci_fi', label: 'Научная фантастика' },
  { value: 'horror', label: 'Ужасы' },
  { value: 'detective', label: 'Детектив' },
  { value: 'thriller', label: 'Триллер' },
  { value: 'drama', label: 'Драма' },
  { value: 'romance', label: 'Романтика' },
  { value: 'adventure', label: 'Приключения' },
  { value: 'action', label: 'Боевик' },
  { value: 'mystic', label: 'Мистика' },
  { value: 'historical', label: 'Исторический' },
  { value: 'crime', label: 'Криминал' },
  { value: 'comedy', label: 'Комедия' }
];

export const GENRE_LABELS = GENRES.reduce((acc, { value, label }) => {
  acc[value] = label;
  return acc;
}, {});

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register'
  },
  STORIES: '/stories',
  CHARACTERS: '/characters',
  EVENTS: '/events',
  RELATIONSHIPS: '/relationships'
};

// Лимиты валидации для полей форм
export const VALIDATION_LIMITS = {
  STORY: {
    TITLE_MAX: 255,
    SHORT_DESCRIPTION_MAX: 500,
    FULL_DESCRIPTION_MAX: 10000,
    COVER_URL_MAX: 2000
  }
};