import React from 'react';
import './InputField.css';

function InputField({ placeholder = '', type = 'text', name = '', required = null, onChange = null}: any) {
  return(
    <input
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