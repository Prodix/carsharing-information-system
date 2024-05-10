import React from "react";
import './Button.css';

function Button({ text, type = "submit" }: any) {
  return(
    <button className="button" type={type}>{text}</button>
  );
}

export default Button;