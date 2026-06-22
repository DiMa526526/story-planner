/**
 * Страница восстановления пароля
 * Позволяет сделать запрос на восстановление пароля
 */
import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    setMessage('');

    try {
      // Отправляем email как query-параметр
      await api.post('/auth/send-recover-link', null, {
        params: { email }
      });
      setMessage('Ссылка для восстановления пароля отправлена на ваш email');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка отправки. Проверьте email.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Восстановление пароля</h2>
          <p className="auth-subtitle">Введите email, указанный при регистрации</p>
        </div>

        {message && <div className="success-message">{message}</div>}
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="ivan@example.com"
            required
          />
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Отправить ссылку
          </Button>
        </form>

        <p className="auth-footer">
          Вспомнили пароль?{' '}
          <Link to="/login" className="auth-link">Войти</Link>
        </p>
      </Card>
    </div>
  );
};

export default ForgotPassword;