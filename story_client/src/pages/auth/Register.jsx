/**
 * Страница регистрации нового пользователя
 * Создаёт аккаунт и перенаправляет на страницу входа
 */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';
import api from '../../services/api';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    verificationCode: ''
  });
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [codeSent, setCodeSent] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (error) setError('');
    if (successMessage) setSuccessMessage('');
  };

  const validateForm = () => {
    if (formData.password !== formData.confirmPassword) {
      setError('Пароли не совпадают');
      return false;
    }
    if (formData.password.length < 6) {
      setError('Пароль должен быть не менее 6 символов');
      return false;
    }
    if (!codeSent) {
      setError('Сначала получите код подтверждения на email');
      return false;
    }
    if (!formData.verificationCode) {
      setError('Введите код подтверждения');
      return false;
    }
    return true;
  };

  const handleSendCode = async () => {
    if (!formData.email) {
      setError('Введите email для получения кода');
      return;
    }
    
    setIsSendingCode(true);
    setError('');
    setSuccessMessage('');
    
    try {
      await api.post('/auth/send-verification-code', null, {
        params: { email: formData.email }
      });
      setCodeSent(true);
      setSuccessMessage('Код подтверждения отправлен на ваш email');
      // Таймер для повторной отправки (60 секунд)
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown(prev => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка отправки кода. Попробуйте позже.');
    } finally {
      setIsSendingCode(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setIsLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      // Используем API с кодом подтверждения
      const response = await api.post('/auth/verify-and-register', {
        username: formData.username,
        email: formData.email,
        password: formData.password
      }, {
        params: { code: formData.verificationCode }
      });
      
      if (response.data.token) {
        // Если сервер вернул токен, значит пользователь уже авторизован
        // Сохраняем токен и перенаправляем в дашборд
        localStorage.setItem('token', response.data.token);
        navigate('/dashboard');
      } else {
        // Иначе на страницу входа
        navigate('/login');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка регистрации. Проверьте код подтверждения.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page auth-page--with-padding">
      <Card className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Создать аккаунт</h2>
          <p className="auth-subtitle">Начни писать свои истории</p>
        </div>
        
        {error && (
          <div className="error-message auth-error">
            ✕ {error}
          </div>
        )}
        
        {successMessage && (
          <div className="success-message auth-success">
            ✓ {successMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="Имя пользователя"
            name="username"
            type="text"
            value={formData.username}
            onChange={handleChange}
            placeholder="ivan123"
            required
            autoFocus
          />

          <div className="form-field">
            <label className="form-label">Email *</label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="input-field"
                placeholder="ivan@example.com"
                required
                disabled={codeSent}
                style={{ flex: 1 }}
              />
              <Button
                type="button"
                variant="secondary"
                onClick={handleSendCode}
                isLoading={isSendingCode}
                disabled={isSendingCode || countdown > 0 || codeSent}
              >
                {countdown > 0 ? `${countdown}с` : (codeSent ? 'Код отправлен' : 'Получить код')}
              </Button>
            </div>
          </div>

          {codeSent && (
            <Input
              label="Код подтверждения"
              name="verificationCode"
              type="text"
              value={formData.verificationCode}
              onChange={handleChange}
              placeholder="Введите 6-значный код из письма"
              required
              maxLength={6}
            />
          )}

          <Input
            label="Пароль"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
          />

          <Input
            label="Подтвердите пароль"
            name="confirmPassword"
            type="password"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="••••••••"
            required
          />

          <Button
            type="submit"
            variant="primary"
            isLoading={isLoading}
            className="auth-submit-btn"
            disabled={!codeSent || !formData.verificationCode}
          >
            Зарегистрироваться
          </Button>
        </form>

        <p className="auth-footer">
          Уже есть аккаунт?{' '}
          <Link to="/login" className="auth-link">
            Войти
          </Link>
        </p>
      </Card>
    </div>
  );
};

export default Register;