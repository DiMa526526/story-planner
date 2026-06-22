/**
 * Выпадающий список
 */
const Select = ({ 
  label, 
  name, 
  value, 
  options, 
  onChange, 
  placeholder = '', 
  required = false,
  className = ''
}) => {
  return (
    <div className="form-field">
      {label && (
        <label className="form-label" htmlFor={name}>
          {label} {required && '*'}
        </label>
      )}
      <select
        id={name}
        name={name}
        value={value}
        onChange={onChange}
        className={`input-field ${className}`}
        required={required}
      >
        <option value="">{placeholder}</option>
        {options.map(option => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
};

export default Select;