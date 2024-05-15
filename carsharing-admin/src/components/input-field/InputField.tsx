import React from 'react';
import './InputField.css';

function InputField({ initialValue = null, placeholder = '', type = 'text', name = '', required = null, onChange = null}: any) {
  return(
    <input
      defaultValue={initialValue}
      className="input-field"
      placeholder={placeholder}
      type={type}
      name={name}
      required={required}
      onChange={onChange}
    />
  );
}

export default InputField;