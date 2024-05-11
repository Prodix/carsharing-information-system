import React from "react";
import './Popup.css';

function Popup({ children, isShowed, onOuterClick }: any) {
  return(
    <section style={{ display: (isShowed === true) ? 'initial' : 'none' }} className="popup">
      <section onClick={onOuterClick}></section>
      {children}
    </section>
  );
}

export default Popup;