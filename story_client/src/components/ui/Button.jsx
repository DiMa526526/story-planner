/**
 * Универсальная кнопка с поддержкой разных вариантов оформления
 */
const Button = ({ 
  children, 
  variant = 'primary', 
  isLoading = false, 
  onClick, 
  type = 'button',
  disabled = false,
  className = ''
}) => {
  const baseClass = `btn btn-${variant} ${className}`;
  
  return (
    <button
      type={type}
      className={baseClass}
      onClick={onClick}
      disabled={disabled || isLoading}
    >
      {isLoading ? 'Загрузка...' : children}
    </button>
  );
};

export default Button;