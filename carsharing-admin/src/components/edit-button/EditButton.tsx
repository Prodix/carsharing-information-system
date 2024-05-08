import React from "react";
import './EditButton.css';

function EditButton({ text }: any) {
  return(
    <section className="edit-button">
      <p>{text}</p>
    </section>
  );
}

export default EditButton;