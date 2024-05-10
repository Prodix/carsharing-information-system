import React from 'react';
import './NavigationButton.css';
import { useLocation, useNavigate } from "react-router-dom";

function NavigationButton({ routes, icon, text }: any) {

  const navigate = useNavigate();
  const location = useLocation()

  return(
    <section className={ routes.includes(location.pathname) ? "nav-button selected" : "nav-button" } onClick={ () => navigate(routes[0]) }>
      {icon}
      <p>{text}</p>
    </section>
  );
}

export default NavigationButton;