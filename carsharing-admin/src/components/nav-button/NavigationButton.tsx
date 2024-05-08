import React from 'react';
import './NavigationButton.css';

function NavigationButton({icon, text}: any) {
  return(
    <section className="nav-button">
      <img src={icon}/>
      <p>{text}</p>
    </section>
  );
}

export default NavigationButton;