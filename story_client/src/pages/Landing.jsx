/**
 * Главная страница
 * Презентует функционал приложения и предлагает регистрацию
 */
import { Link } from 'react-router-dom';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';

const Landing = () => {
  const features = [
    { icon: '◆', title: 'Управление историями', text: 'Создавай истории с описанием, жанром и обложкой' },
    { icon: '✦', title: 'Персонажи и отношения', text: 'Добавляй персонажей и отслеживай их связи' },
    { icon: '⌘', title: 'Нелинейные сюжеты', text: 'Строй граф событий с ветвлениями' }
  ];

  return (
    <div className="landing-page">
      <section className="landing-hero">
        <h1 className="landing-title">
          Пиши истории. <br />
          <span className="landing-title-accent">Структурируй сюжеты.</span>
        </h1>
        <p className="landing-description">
          Инструмент для писателей, помогающий создавать нелинейные сюжеты,
          управлять персонажами и отслеживать развитие отношений.
        </p>
        <div className="landing-buttons">
          <Link to="/register">
            <Button variant="primary" className="landing-btn">
              Начать бесплатно
            </Button>
          </Link>
          <Link to="/login">
            <Button variant="secondary" className="landing-btn">
              Войти
            </Button>
          </Link>
        </div>
      </section>

      <section className="landing-features">
        {features.map((feature, index) => (
          <Card key={index} className="landing-feature">
            <div className="landing-feature-icon">
              <span className="icon icon-4xl">{feature.icon}</span>
            </div>
            <h3 className="landing-feature-title">{feature.title}</h3>
            <p className="landing-feature-text">{feature.text}</p>
          </Card>
        ))}
      </section>
    </div>
  );
};

export default Landing;