import React from "react";
import './MainContent.css';
import UsersList from "../users-list/UsersList";
import TransportList from "../transport-list/TransportList";

function MainContent({ header = 'Страница не существует' }: any) {

  const content = header === "Пользователи"
    ? <UsersList/>
    : header === "Транспорт"
      ? <TransportList/>
      : null;

  return(
    <section className="main-content">
      <h1>{header}</h1>
      {content}
    </section>
  );
}

export default MainContent;