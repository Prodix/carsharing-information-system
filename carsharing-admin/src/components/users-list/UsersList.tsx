import React, {useEffect, useState} from "react";
import './UsersList.css';
import Popup from "../popup/Popup";
import Button from "../button/Button";
import InputField from "../input-field/InputField";

function UsersList() {

  const [users, setUsers] = useState<any>([]);
  const [selectedUser, setSelectedUser] = useState<any>(null);
  const [content, setContent] = useState<any>(null);
  const [isShowed, setIsShowed] = useState<boolean>(false);

  useEffect(() => {
    const token = localStorage.getItem("token");
    fetch(process.env.REACT_APP_API_IP + '/api/admin/users/get', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).then(resolve => resolve.json())
      .then(result => setUsers(result.map((user:any) => {
        let email = user?.email ? user?.email?.split('@')[0] : ""
        if (result.indexOf(user) === 0) {
          return (
            <tr>
              <th>Фото</th>
              <th>Почта</th>
              <th>Баланс</th>
              <th>Верифицирован</th>
              <th>Подтверждена почта</th>
              <th>Рейтинг</th>
            </tr>
          )
        }
        return(
          <tr onClick={(event) => {
            setSelectedUser(user);
            setIsShowed(true);
          }}>
            <td>
              <img alt="Фото лица" src={ user.selfieId ? process.env.REACT_APP_API_IP + `/api/account/get/selfie?id=${user.selfieId}` : "https://static.vecteezy.com/system/resources/previews/009/292/244/original/default-avatar-icon-of-social-media-user-vector.jpg"}/>
            </td>
            <td>{user?.email?.length >= 30 ? email.substring(0, 5) + "..." + email.substring(email.length-5, email.length) + user?.email.substring(email.length) : user?.email}</td>
            <td>{user.balance.toFixed(2)}</td>
            <td>{user.isVerified ? "Да" : "Нет"}</td>
            <td>{user.isEmailVerified ? "Да" : "Нет"}</td>
            <td>{user.rating}</td>
          </tr>
        );
      })));
  }, []);

  let email = selectedUser?.email ? selectedUser?.email?.split('@')[0] : ""

  return(
    <section className="user-table">
      <table>
        {users}
      </table>
      <Popup
        isShowed={isShowed}
        onOuterClick={() => {
          setSelectedUser(null);
          setIsShowed(false);
        }}>
        <section>
          <section className="info">
            <img alt="Фото лица" src={selectedUser?.selfieId ? process.env.REACT_APP_API_IP + `/api/account/get/selfie?id=${selectedUser?.selfieId}` : "https://static.vecteezy.com/system/resources/previews/009/292/244/original/default-avatar-icon-of-social-media-user-vector.jpg"}/>
            <section>
              <h2>Информация</h2>
              <table>
                <tr>
                  <td>Почта:</td>
                  <td>{selectedUser?.email?.length >= 30 ? email.substring(0, 5) + "..." + email.substring(email.length-5, email.length) + selectedUser?.email.substring(email.length) : selectedUser?.email}</td>
                </tr>
                <tr>
                  <td>Паспорт:</td>
                  <td>{selectedUser?.passportId === null ? <span style={{ color: "#CC6666" }}>Нет</span> : <a download href={process.env.REACT_APP_API_IP + `/api/account/get/passport?id=${selectedUser?.passportId}`}>Да</a>}</td>
                </tr>
                <tr>
                  <td>Баланс:</td>
                  <td>{selectedUser?.balance.toFixed(2)}</td>
                </tr>
                <tr>
                  <td>Вод. уд.:</td>
                  <td>{selectedUser?.driverLicenseId === null ? <span style={{ color: "#CC6666" }}>Нет</span> : <a download href={process.env.REACT_APP_API_IP + `/api/account/get/driver_license?id=${selectedUser?.driverLicenseId}`}>Да</a>}</td>
                </tr>
                <tr>
                  <td>Верифицирован:</td>
                  <td>{selectedUser?.isVerified ? <span style={{color: "#7ccc66"}}>Да</span> :
                    <span style={{color: "#CC6666"}}>Нет</span>}</td>
                </tr>
                <tr>
                  <td>Подтверждёна почта:</td>
                  <td>{selectedUser?.isEmailVerified ? <span style={{color: "#7ccc66"}}>Да</span> :
                    <span style={{color: "#CC6666"}}>Нет</span>}</td>
                </tr>
                <tr>
                  <td>Рейтинг:</td>
                  <td>{selectedUser?.rating}</td>
                </tr>
              </table>
            </section>
          </section>
          <section className="buttons">
            <Button text="Верификация" type="button" onClick={() => {
              setContent(
                <>
                  <Button text="Верифицировать" type="button"/>
                  <Button text="Снять верификацию" type="button"/>
                </>
              );
            }}/>
            <Button text="Заблокировать" type="button"/>
            <Button text="Удалить" type="button"/>
            <Button text="История активности" type="button"/>
            <Button text="Выписать штраф" type="button" onClick={() => {
              setContent(
                <div className="penalty">
                  <InputField placeholder="Сумма" type="text" name="price" required={true}/>
                  <InputField placeholder="Штраф рейтинга" type="text" name="rating_penalty" required={true}/>
                  <InputField placeholder="Описание" type="text" name="description" required={true}/>
                  <Button text="Подтвердить" type="button"/>
                </div>
              );
            }}/>
          </section>
          {content &&
              <section className="action">
                {content}
              </section>
          }
        </section>
      </Popup>
    </section>
  );
}

export default UsersList;