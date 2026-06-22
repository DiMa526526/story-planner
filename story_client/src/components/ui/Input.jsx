/**
 * Поле ввода с поддержкой разных типов
 */
const Input = ({ 
  label, 
  name, 
  value, 
  onChange, 
  error, 
  type = 'text', 
  placeholder = '', 
  required = false,
  autoFocus = false,
  maxLength
}) => {
  return (
    <div className="form-field">
      {label && (
        <label className="form-label" htmlFor={name}>
          {label} {required && '*'}
        </label>
      )}
      <input
        id={name}
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        className={`input-field ${error ? 'input-field--error' : ''}`}
        placeholder={placeholder}
        required={required}
        autoFocus={autoFocus}
        maxLength={maxLength}
      />
      {error && <span className="form-error">{error}</span>}
      {maxLength && (
        <div className="form-hint">
          {value?.length || 0} / {maxLength} символов
        </div>
      )}
    </div>
  );
};

export default Input;