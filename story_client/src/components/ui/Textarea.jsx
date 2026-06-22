/**
 * Многострочное текстовое поле
 */
const Textarea = ({ 
  label, 
  name, 
  value, 
  onChange, 
  rows = 3, 
  placeholder = '', 
  required = false,
  maxLength
}) => {
  return (
    <div className="form-field">
      {label && (
        <label className="form-label" htmlFor={name}>
          {label} {required && '*'}
        </label>
      )}
      <textarea
        id={name}
        name={name}
        value={value}
        onChange={onChange}
        rows={rows}
        className="input-field"
        placeholder={placeholder}
        required={required}
        maxLength={maxLength}
      />
      {maxLength && (
        <div className="form-hint">
          {value?.length || 0} / {maxLength} символов
        </div>
      )}
    </div>
  );
};

export default Textarea;