/**
 * Страница входа в систему
 * Позволяет пользователю авторизоваться по email/username и паролю
 */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    const result = await login(email, password);
    
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
    
    setIsLoading(false);
  };

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Добро пожаловать!</h2>
          <p className="auth-subtitle">Войдите в свой аккаунт</p>
        </div>
        
        {error && (
          <div className="error-message auth-error">
            ✕ {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="Email или имя пользователя"
            name="email"
            type="text"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="example@mail.com"
            required
            autoFocus
          />

          <Input
            label="Пароль"
            name="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
          />

          <Button
            type="submit"
            variant="primary"
            isLoading={isLoading}
            className="auth-submit-btn"
          >
            Войти
          </Button>
        </form>

        <p className="auth-footer">
          Нет аккаунта?{' '}
          <Link to="/register" className="auth-link">
            Зарегистрироваться
          </Link>
        </p>

        <div className="text-center mb-2">
          <Link to="/forgot-password" className="text-sm text-purple-600 hover:underline">
            Забыли пароль?
          </Link>
        </div>
      </Card>
    </div>
  );
};

export default Login;