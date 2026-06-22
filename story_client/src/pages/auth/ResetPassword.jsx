/**
 * Страница изменения пароля
 * Позволяет пользователю изменить пароль
 */
import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';


const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { post } = useApi();

  useEffect(() => {
    if (!token) {
      setError('Недействительная ссылка сброса пароля');
    }
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }
    if (password.length < 6) {
      setError('Пароль должен быть не менее 6 символов');
      return;
    }

    setIsLoading(true);
    setError('');
    setMessage('');

    try {
      await post('/auth/recover-password', { token, newPassword: password });
      setMessage('Пароль успешно изменён. Теперь вы можете войти.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка сброса пароля. Возможно, ссылка устарела.');
    } finally {
      setIsLoading(false);
    }
  };

  if (!token && error) {
    return (
      <div className="auth-page">
        <Card className="auth-card">
          <div className="error-message">{error}</div>
          <Link to="/forgot-password" className="auth-link">Запросить новую ссылку</Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Новый пароль</h2>
          <p className="auth-subtitle">Придумайте надёжный пароль</p>
        </div>

        {message && <div className="success-message">{message}</div>}
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="Новый пароль"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
          />
          <Input
            label="Подтвердите пароль"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="••••••••"
            required
          />
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Установить пароль
          </Button>
        </form>

        <p className="auth-footer">
          <Link to="/login" className="auth-link">Вернуться ко входу</Link>
        </p>
      </Card>
    </div>
  );
};

export default ResetPassword;