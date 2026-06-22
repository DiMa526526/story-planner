/**
 * Основной макет приложения
 * Содержит навигацию, основное содержимое и футер
 */
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Button from '../ui/Button';
import ThemeToggle from './ThemeToggle';

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <nav className="nav">
        <div className="container">
          <div className="nav-inner">
            <Link to="/" className="logo">
              Story Planner
            </Link>

            <div className="nav-actions">
              {user ? (
                <>
                  <Link to="/dashboard" className="nav-link">
                    Мои истории
                  </Link>
                  <button onClick={handleLogout} className="nav-link nav-link--danger">
                    Выйти
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" className="nav-link">
                    Вход
                  </Link>
                  <Link to="/register">
                    <Button variant="primary">Регистрация</Button>
                  </Link>
                </>
              )}
              <ThemeToggle />
            </div>
          </div>
        </div>
      </nav>

      <main className="main-content">
        {children}
      </main>

      <footer className="footer">
        <div className="container">
          <p className="footer-text">
            © 2026 Story Planner — инструмент для писателей и сценаристов
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;