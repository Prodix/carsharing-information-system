import React from 'react';
import './NavigationButton.css';
import {useLocation, useNavigate } from "react-router-dom";

function NavigationButton({ route, icon, text }: any) {

  const navigate = useNavigate();
  const location = useLocation()

  return(
    <section className={ location.pathname === route ? "nav-button selected" : "nav-button" } onClick={ () => navigate(route) }>
      {icon}
      <p>{text}</p>
    </section>
  );
}

export default NavigationButton;