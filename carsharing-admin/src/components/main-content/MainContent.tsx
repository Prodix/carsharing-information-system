import React from "react";
import './MainContent.css';
import UsersList from "../users-list/UsersList";

function MainContent({ header = 'Страница не существует' }: any) {

  const content = header === "Пользователи"
    ? <UsersList/>
    : null

  return(
    <section className="main-content">
      <h1>{header}</h1>
      {content}
    </section>
  );
}

export default MainContent;