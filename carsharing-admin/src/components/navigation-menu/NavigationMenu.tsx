import React from 'react';
import './NavigationMenu.css';
import Logo from '../../assets/logo.svg';
import Avatar from '../../assets/avatar.png';
import User from '../../assets/user.svg';
import Dashboard from '../../assets/dashboard.svg';
import Report from '../../assets/report.svg';
import Exit from '../../assets/exit.svg';
import Car from '../../assets/car.svg';
import NavigationButton from "../nav-button/NavigationButton";
import EditButton from "../edit-button/EditButton";

function NavigationMenu() {
  return(
    <nav className="nav-menu">
      <section className="logo-section">
        <img className="logo" src={Logo}/>
        <h1>AutoShare</h1>
      </section>
      <section className="nav-links">
        <NavigationButton
          icon={Dashboard}
          text="Дашборд"/>
        <NavigationButton
          icon={Car}
          text="Транспорт"/>
        <NavigationButton
          icon={User}
          text="Пользователи"/>
        <NavigationButton
          icon={Report}
          text="Отчёты"/>
      </section>
      <div className="divider"/>
      <section className="profile-section">
        <img className="profile-photo" src={Avatar}/>
        <section>
          <h3 className="profile-email">admin@gmail.com</h3>
          <EditButton
            text="Редактировать"/>
        </section>
        <img src={Exit}/>
      </section>
    </nav>
  );
}

export default NavigationMenu;