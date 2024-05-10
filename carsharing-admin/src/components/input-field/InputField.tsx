import React from 'react';
import './InputField.css';

function InputField({ placeholder = '', type = 'text', name = '', required = null }: any) {
  return(
    <input
      className="input-field"
      placeholder={placeholder}
      type={type}
      name={name}
      required={required}/>
  );
}

export default InputField;