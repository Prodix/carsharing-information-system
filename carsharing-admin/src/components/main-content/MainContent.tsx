import React from "react";
import './MainContent.css';

function MainContent({ header = 'Страница не существует' }: any) {
  return(
    <section className="main-content">
      <h1>{header}</h1>
    </section>
  );
}

export default MainContent;