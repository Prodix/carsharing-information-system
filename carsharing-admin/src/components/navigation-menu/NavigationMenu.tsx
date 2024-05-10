import React from 'react';
import './NavigationMenu.css';
import Logo from '../../assets/Logo';
import Avatar from '../../assets/avatar.png';
import Exit from '../../assets/Exit';
import EditButton from "../edit-button/EditButton";
import { useFetcher } from "react-router-dom";

function NavigationMenu({ children }: any) {
  const fetcher = useFetcher();
  return(
    <nav className="nav-menu">
      <section className="logo-section">
        <Logo />
        <h1>AutoShare</h1>
      </section>
      <section className="nav-links">
        {children}
      </section>
      <div className="divider"/>
      <section className="profile-section">
        <img alt="Аватарка" className="profile-photo" src={Avatar}/>
        <section>
          <h3 className="profile-email">admin@gmail.com</h3>
          <EditButton
            text="Редактировать"/>
        </section>
        <fetcher.Form method="post" action="/logout">
          <button type="submit">
            <Exit />
          </button>
        </fetcher.Form>
      </section>
    </nav>
  );
}

export default NavigationMenu;