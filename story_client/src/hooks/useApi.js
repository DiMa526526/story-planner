/**
 * Хук для выполнения API запросов с состоянием загрузки и ошибки
 */
import { useState, useCallback } from 'react';
import api from '../services/api';

export const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const request = useCallback(async (requestFn, onSuccess) => {
    setLoading(true);
    setError(null);
    try {
      const response = await requestFn();
      onSuccess?.(response.data);
      return response.data;
    } catch (err) {
      const message = err.response?.data?.message || 'Произошла ошибка';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const get = useCallback((url, onSuccess) => 
    request(() => api.get(url), onSuccess), [request]);

  const post = useCallback((url, data, onSuccess) => 
    request(() => api.post(url, data), onSuccess), [request]);

  const put = useCallback((url, data, onSuccess) => 
    request(() => api.put(url, data), onSuccess), [request]);

  const del = useCallback((url, onSuccess) => 
    request(() => api.delete(url), onSuccess), [request]);

  return { loading, error, get, post, put, del };
};