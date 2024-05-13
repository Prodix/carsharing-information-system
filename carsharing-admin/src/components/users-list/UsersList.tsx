import React, {useEffect, useState} from "react";
import './UsersList.css';
import Popup from "../popup/Popup";
import Button from "../button/Button";
import InputField from "../input-field/InputField";

function UsersList() {

  const [users, setUsers] = useState<any>([]);
  const [initialUsers, setInitialUsers] = useState<any>([]);
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
      .then(result => {
        setUsers(result);
        setInitialUsers(result);
      });
  }, []);

  let email = selectedUser?.email ? selectedUser?.email?.split('@')[0] : ""

  return(
    <>
      <InputField name="search" type="text" placeholder="Поиск" onChange={() => {
        const search_text = (document.querySelector('input[name="search"]') as HTMLInputElement).value
        if (search_text.length > 0) {
          setUsers(initialUsers.filter((x:any) => JSON.stringify(x).includes(search_text)));
        } else {
          setUsers(initialUsers);
        }
      }}/>
      <section className="user-table">
        <table>
          <tr>
            <th>Фото</th>
            <th>Почта</th>
            <th>Баланс</th>
            <th>Верифицирован</th>
            <th>Подтверждена почта</th>
            <th>Рейтинг</th>
          </tr>
          { users.map((user:any) => {
              let email = user?.email ? user?.email?.split('@')[0] : "";
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
            }) }
        </table>
        <Popup
          isShowed={isShowed}
          onOuterClick={() => {
            setSelectedUser(null);
            setIsShowed(false);
            setContent(null);
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
                    <td>Подтверждена почта:</td>
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
                    <Button text="Верифицировать" type="button" onClick={async () => {
                      const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/users/verify?id=${selectedUser.id}`, {
                        method: "POST",
                        headers: {
                          "Authorization": `Bearer ${localStorage.getItem("token")}`
                        }
                      });
                      const body = await response.json();

                      if (body.status_code !== 200) {
                        alert(body.message);
                      } else {
                        setIsShowed(false);
                        selectedUser.isVerified = true;
                        setSelectedUser(null);
                      }
                    }}/>
                    <Button text="Снять верификацию" type="button" onClick={async () => {
                      const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/users/unverify?id=${selectedUser.id}`, {
                        method: "POST",
                        headers: {
                          "Authorization": `Bearer ${localStorage.getItem("token")}`
                        }
                      });
                      const body = await response.json();

                      if (body.status_code !== 200) {
                        alert(body.message);
                      } else {
                        setIsShowed(false);
                        selectedUser.isVerified = false;
                        setSelectedUser(null);
                      }
                    }}/>
                  </>
                );
              }}/>
              <Button text="Удалить" type="button" onClick={async () => {
                const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/users/delete?id=${selectedUser.id}`, {
                  method: "POST",
                  headers: {
                    "Authorization": `Bearer ${localStorage.getItem("token")}`
                  }
                });
                const body = await response.json();

                if (body.status_code !== 200) {
                  alert(body.message);
                } else {
                  setIsShowed(false);
                  users.splice(users.indexOf(selectedUser), 1)
                  setUsers(users);
                  setSelectedUser(null);
                }
              }}/>
              <Button text="История аренды" type="button" onClick={async () => {
                const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/users/get_history?id=${selectedUser.id}`, {
                  headers: {
                    "Authorization": `Bearer ${localStorage.getItem("token")}`
                  }
                });
                const content = await response.json();
                setContent(
                  <ul>
                    {content.map((rentHistory: any) => {
                      return(
                        <li>{rentHistory.transport.carName}({rentHistory.transport.carNumber}), Дата: {rentHistory.date}, Время аренды: {rentHistory.rentTime.substring(0,8)}, Цена: {rentHistory.price.toFixed(2)}</li>
                      )
                    })}
                  </ul>
                )
              }}/>
              <Button text="Выписать штраф" type="button" onClick={async () => {
                const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/users/get_history?id=${selectedUser.id}`, {
                  headers: {
                    "Authorization": `Bearer ${localStorage.getItem("token")}`
                  }
                });
                const content = await response.json();
                setContent(
                  <div className="penalty">
                    <InputField placeholder="Сумма" type="text" name="price" required={true}/>
                    <InputField placeholder="Штраф рейтинга" type="text" name="rating_penalty" required={true}/>
                    <InputField placeholder="Описание" type="text" name="description" required={true}/>
                    <select className="custom-select">
                      {content.map((rentHistory: any) => {
                        return(
                          <option value={rentHistory.id}>{rentHistory.transport.carName} ({rentHistory.date})</option>
                        )
                      })}
                    </select>
                    <Button text="Подтвердить" type="button" onClick={async () => {
                      const price = (document.querySelector('input[name="price"]') as HTMLInputElement).value;
                      const ratingPenalty = (document.querySelector('input[name="rating_penalty"]') as HTMLInputElement).value;
                      const description = (document.querySelector('input[name="description"]') as HTMLInputElement).value;
                      const select = (document.querySelector(".custom-select") as HTMLInputElement).value;
                      if (/^\d+\.?\d+$/gi.test(price) && /^\d+$/gi.test(ratingPenalty) && description.length > 0 && select.length > 0) {
                        const response = await fetch(process.env.REACT_APP_API_IP + '/api/admin/users/give_penalty', {
                          method: "POST",
                          headers: {
                            "Authorization": `Bearer ${localStorage.getItem("token")}`
                          },
                          body: JSON.stringify({
                            Description: description,
                            Price: price,
                            UserId: selectedUser.id,
                            RatingPenalty: ratingPenalty,
                            RelatedRent: select
                          })
                        });
                        setIsShowed(false)
                        setSelectedUser(null)
                        setContent(null)
                        alert((await response.json()).message);
                      } else {
                        alert("Неверный формат введённых данных");
                      }
                    }}/>
                  </div>
                );
              }}/>
              <Button text="Оправить сообщение" type="button" onClick={async () => {
                setContent(
                  <div className="penalty">
                    <InputField name="message" type="text" placeholder="Сообщение"/>
                    <Button text="Отправить" type="button" onClick={async () => {
                      const message = (document.querySelector('input[name="message"]') as HTMLInputElement).value;
                      if (message.length > 0) {
                        const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/users/send_message?id=${selectedUser.id}`, {
                          method: "POST",
                          headers: {
                            "Authorization": `Bearer ${localStorage.getItem("token")}`
                          },
                          body: message
                        });
                        setIsShowed(false)
                        setSelectedUser(null)
                        setContent(null)
                        alert((await response.json()).message);
                      } else {
                        alert("Вы ввели пустое сообщение");
                      }
                    }}/>
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
    </>
  );
}

export default UsersList;