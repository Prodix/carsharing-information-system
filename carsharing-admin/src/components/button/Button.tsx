import React from "react";
import './Button.css';

function Button({ text, type = "submit", onClick = null }: any) {
  return(
    <button onClick={onClick} className="button" type={type}>{text}</button>
  );
}

export default Button;