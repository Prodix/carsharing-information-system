import React, {useEffect, useState} from "react";
import './TransportList.css';
import InputField from "../input-field/InputField";
import Popup from "../popup/Popup";
import Button from "../button/Button";

function TransportList() {

  const [transport, setTransport] = useState<any>([]);
  const [initialTransport, setInitialTransport] = useState<any>([]);
  const [selectedCar, setSelectedCar] = useState<any>(null);
  const [content, setContent] = useState<any>(null);
  const [isShowed, setIsShowed] = useState<boolean>(false);
  const [damage, setDamage] = useState<any>([]);

  useEffect(() => {
    const token = localStorage.getItem("token");
    fetch(process.env.REACT_APP_API_IP + '/api/transport/get', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).then(resolve => resolve.json())
      .then(result => {
        setTransport(result);
        setInitialTransport(result);
      });
  }, []);

  return(
    <>
      <InputField name="search" type="text" placeholder="Поиск" onChange={() => {
        const search_text = (document.querySelector('input[name="search"]') as HTMLInputElement).value
        if (search_text.length > 0) {
          setTransport(initialTransport.filter((x: any) => JSON.stringify(x).includes(search_text)));
        } else {
          setTransport(initialTransport);
        }
      }}/>
      <a target="_blank" rel="noreferrer" href={process.env.REACT_APP_API_IP + '/api/admin/transport/get/document'} style={{
        textDecoration: "none",
        color: "black",
        width: "fit-content",
        cursor: "pointer",
        padding: 10,
        borderRadius: 16,
        backgroundColor: "#6699cc"
      }}>Выгрузка</a>
      <Button text="Добавить" type="button" onClick={() => {
        transport.push({
          CarImagePath: "new_car.png",
          CarName: "Новый транспорт",
          CarNumber: "",
          Functions: [],
          GasConsumption: 0,
          GasLevel: 0,
          Id: -Math.round(Math.random() * 10000),
          InsuranceType: "ОСАГО",
          IsDoorOpened: false,
          IsReserved: false,
          Latitude: 0.0,
          Longitude: 0.0,
          Rates: [],
          TankCapacity: 0,
          TransportType: "BASE",
          IsNew: true
        });
        setTransport([...transport]);
      }}/>
      <section className="transport-table">
        <table>
          <tbody>
          <tr>
            <th>Название</th>
            <th>Категория</th>
            <th>Номер</th>
            <th>Забронировано</th>
            <th>Уровень топлива</th>
            <th>Страховка</th>
          </tr>
          {transport.map((car: any) => {
            return (
              <tr onClick={() => {
                setSelectedCar(JSON.parse(JSON.stringify(car)));
                setIsShowed(true);
              }}>
                <td>{car.CarName}</td>
                <td>{car.TransportType === "BASE" ? 'Базовый' : car.TransportType === "COMFORT" ? 'Комфорт' : 'Бизнес'}</td>
                <td>{car.CarNumber}</td>
                <td>{car.IsReserved ? "Да" : "Нет"}</td>
                <td>{car.GasLevel} л</td>
                <td>{car.InsuranceType}</td>
              </tr>
            );
          })}
          </tbody>
        </table>
        <Popup
          isShowed={isShowed}
          onOuterClick={() => {
            setSelectedCar(null);
            setIsShowed(false);
            setContent("");
          }}>
          <section>
            <section className="car-info">
              <img alt="Фото"
                   src={selectedCar?.CarImagePath ? process.env.REACT_APP_API_IP + `/api/transport/get/image?name=${selectedCar?.CarImagePath}` : "https://static.vecteezy.com/system/resources/previews/009/292/244/original/default-avatar-icon-of-social-media-user-vector.jpg"}/>
              <section>
                <h2>Информация</h2>
                <table>
                  <tbody>
                  <tr>
                    <td>Название:</td>
                    <td><InputField initialValue={selectedCar?.CarName ?? ""} onChange={(event: any) => {
                      selectedCar.CarName = event.target.value;
                    }}/></td>
                  </tr>
                  <tr>
                    <td>Категория:</td>
                    <td>
                      <select onChange={(event: any) => {
                        selectedCar.TransportType = event.target.value;
                      }}>
                        <option value="BASE" selected={selectedCar?.TransportType === "BASE"}>Базовый</option>
                        <option value="COMFORT" selected={selectedCar?.TransportType === "COMFORT"}>Комфорт</option>
                        <option value="BUSINESS" selected={selectedCar?.TransportType === "BUSINESS"}>Бизнес</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <td>Номер:</td>
                    <td><InputField initialValue={selectedCar?.CarNumber} onChange={(event: any) => {
                      selectedCar.CarNumber = event.target.value;
                    }}/></td>
                  </tr>
                  {selectedCar?.IsNew === true ? null :
                    <tr>
                      <td>Забронирован:</td>
                      <td>{selectedCar?.IsReserved.toString() === "true" ? "Да" : "Нет"}</td>
                    </tr>
                  }
                  <tr>
                    <td>Уровень топлива:</td>
                    <td>{selectedCar?.IsNew === true ?
                      <InputField type="text" initialValue={selectedCar?.GasLevel} onChange={(event: any) => {
                        selectedCar.GasLevel = event.target.value;
                      }}/> : selectedCar?.GasLevel + ' л'}</td>
                  </tr>
                  <tr>
                    <td>Страховка:</td>
                    <td>
                      <select onChange={(event: any) => {
                        selectedCar.InsuranceType = event.target.value;
                      }}>
                        <option value="ОСАГО" selected={selectedCar?.InsuranceType === "ОСАГО"}>ОСАГО</option>
                        <option value="КАСКО" selected={selectedCar?.InsuranceType === "КАСКО"}>КАСКО</option>
                      </select>
                    </td>
                  </tr>
                  {selectedCar?.IsNew === true
                    ? <>
                      <tr>
                        <td>
                          Потребление топлива
                        </td>
                        <td>
                          <InputField type="text" initialValue={selectedCar?.GasConsumption} onChange={(event: any) => {
                            selectedCar.GasConsumption = event.target.value;
                          }}/>
                        </td>
                      </tr>
                      <tr>
                        <td>
                          Широта
                        </td>
                        <td>
                          <InputField type="text" initialValue={selectedCar?.Latitude} onChange={(event: any) => {
                            selectedCar.Latitude = event.target.value;
                          }}/>
                        </td>
                      </tr>
                      <tr>
                        <td>
                          Долгота
                        </td>
                        <td>
                          <InputField type="text" initialValue={selectedCar?.Longitude} onChange={(event: any) => {
                            selectedCar.Longitude = event.target.value;
                          }}/>
                        </td>
                      </tr>
                      <tr>
                        <td>
                          Объём бака
                        </td>
                        <td>
                          <InputField type="text" initialValue={selectedCar?.TankCapacity} onChange={(event: any) => {
                            selectedCar.TankCapacity = event.target.value;
                          }}/>
                        </td>
                      </tr>
                    </>
                    : <tr>
                      <td>Позиция на карте:</td>
                      <td><a target="_blank"
                             rel="noreferrer"
                             href={`https://yandex.ru/maps/?ll=${selectedCar?.Longitude},${selectedCar?.Latitude}&z=17&pt=${selectedCar?.Longitude},${selectedCar?.Latitude},pm2rdm`}>Клик</a>
                      </td>
                    </tr>
                  }
                  {selectedCar?.IsNew === true ?
                    null
                    : <tr>
                      <td>
                        <Button onClick={() => {
                          (document.querySelector("input[name='input-image']") as HTMLInputElement).click();
                        }} text="Сменить изображение" type="button"/>
                      </td>
                      <td>
                        <input accept="image/png" type="file" name="input-image" style={{display: "none"}}
                               onChange={async (event: any) => {
                                 if (event.target.value) {
                                   const formData = new FormData();
                                   formData.set("file", event.target.files[0]);
                                   const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/transport/image/add?id=${selectedCar?.Id}`, {
                                     method: "POST",
                                     headers: {
                                       "Authorization": `Bearer ${localStorage.getItem("token")}`
                                     },
                                     body: formData
                                   });

                                   const body = await response.json();

                                   if (body.status_code !== 200) {
                                     alert(body.message);
                                   } else {
                                     alert("Фото изменено");
                                     setIsShowed(false);
                                     fetch(process.env.REACT_APP_API_IP + '/api/transport/get', {
                                       headers: {
                                         'Authorization': `Bearer ${localStorage.getItem("token")}`
                                       }
                                     }).then(resolve => resolve.json())
                                       .then(result => {
                                         setTransport(result);
                                         setInitialTransport(result);
                                       });
                                     setSelectedCar(null);
                                     event.target.files = null;
                                   }
                                 }
                               }}/>
                      </td>
                    </tr>
                  }
                  </tbody>
                </table>
              </section>
            </section>
            <section className="buttons">
              <Button text="Сохранить" type="button" onClick={async () => {
                if (selectedCar?.CarNumber.length === 0 || selectedCar?.CarName.length === 0) {
                  alert("Вы не ввели название автомобиля или его номер");
                  return;
                }

                if (selectedCar?.IsNew === true) {
                  if (selectedCar?.GasConsumption.length === 0 ||
                    selectedCar?.GasLevel.length === 0 ||
                    selectedCar?.Latitude.length === 0 ||
                    selectedCar?.Longitude.length === 0 ||
                    selectedCar?.TankCapacity.length === 0) {
                    alert("Вы не ввели одно из полей");
                    return;
                  }

                  if (!/^\d+$/gm.test(selectedCar?.GasConsumption) ||
                    !/^\d+$/gm.test(selectedCar?.GasLevel) ||
                    !/^\d+$/gm.test(selectedCar?.TankCapacity)) {
                    alert("Потребление и уровень топлива, а также объём бака должны быть целыми числами");
                    return;
                  }

                  if (selectedCar?.GasConsumption === 0 ||
                    selectedCar?.GasLevel === 0 ||
                    selectedCar?.TankCapacity === 0) {
                    alert("Потребление и уровень топлива, а также объём бака не могут равняться нулю");
                    return;
                  }

                  if (!/^\d+\.?\d*$/gm.test(selectedCar?.Latitude) ||
                    !/^\d+\.?\d*$/gm.test(selectedCar?.Longitude)) {
                    alert("Широта и долгота должны быть дробными числами");
                    return;
                  }

                  for (let i of selectedCar.Rates) {
                    i.Id = 0;
                  }

                  for (let i of selectedCar.Functions) {
                    i.Id = 0;
                  }
                }

                if (!/^[А-Я]{1}[0-9]{3}[А-Я]{2}[0-9]{2}$/gm.test(selectedCar.CarNumber)) {
                  alert("Вы не ввели некорректный номер автомобиля");
                  return;
                }

                for (let i of selectedCar?.Rates) {
                  if (i.RateName.length === 0) {
                    alert("Вы не ввели имя тарифа");
                    return;
                  }
                  if (!/^\d+\.?\d*$/gm.test(i.OnRoadPrice) || !/^\d+\.?\d*$/gm.test(i.ParkingPrice)) {
                    alert("Вы ввели некорректное значение цены тарифа");
                    return;
                  }
                }

                const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/transport/save?id=${selectedCar?.Id}`, {
                  method: "POST",
                  headers: {
                    "Authorization": `Bearer ${localStorage.getItem("token")}`
                  },
                  body: JSON.stringify(selectedCar)
                });
                const body = await response.json();

                if (body.status_code !== 200) {
                  alert(body.message);
                } else {
                  alert("Транспорт успешно сохранён");
                  setIsShowed(false);
                  const token = localStorage.getItem("token");
                  fetch(process.env.REACT_APP_API_IP + '/api/transport/get', {
                    headers: {
                      'Authorization': `Bearer ${token}`
                    }
                  }).then(resolve => resolve.json())
                    .then(result => {
                      setTransport(result);
                      setInitialTransport(result);
                    });
                  setSelectedCar(null);
                  setContent("");
                }
              }}/>
              {selectedCar?.IsNew === true ? null :
                <Button text="Удалить" type="button" onClick={async () => {
                  const response = await fetch(process.env.REACT_APP_API_IP + `/api/admin/transport/delete?id=${selectedCar?.Id}`, {
                    method: "POST",
                    headers: {
                      "Authorization": `Bearer ${localStorage.getItem("token")}`
                    }
                  });
                  const body = await response.json();

                  if (body.status_code !== 200) {
                    alert(body.message);
                  } else {
                    alert("Транспорт удалён");
                    setIsShowed(false);
                    const token = localStorage.getItem("token");
                    fetch(process.env.REACT_APP_API_IP + '/api/transport/get', {
                      headers: {
                        'Authorization': `Bearer ${token}`
                      }
                    }).then(resolve => resolve.json())
                      .then(result => {
                        setTransport(result);
                        setInitialTransport(result);
                      });
                    setSelectedCar(null);
                  }
                }}/>
              }
              <Button text="Тарифы" type="button" onClick={async () => {
                setContent(
                  "rate"
                )
              }}/>
              <Button text="Функции" type="button" onClick={async () => {
                setContent(
                  "function"
                );
              }}/>
              {selectedCar?.IsNew === true ? null :
                <Button text="Повреждения" type="button" onClick={async () => {
                  setContent(
                    "damage"
                  );
                  let names = await (await fetch(process.env.REACT_APP_API_IP + `/api/transport/get/damage?id=${selectedCar?.Id}`, {
                    headers: {
                      "Authorization": `Bearer ${localStorage.getItem("token")}`
                    }
                  })).json();
                  setDamage(names);
                }}/>
              }
            </section>
            {content &&
                <section className="action">
                    <>
                        <ul>
                          {content === "rate" ? selectedCar?.Rates.map((rate: any) => {
                            return (
                              <li key={rate.Id}>
                                <InputField name="rateName" placeholder="Название" initialValue={rate.RateName} onChange={(event: any) => {
                                  rate.RateName = event.target.value;
                                }}/>
                                <InputField name="rateOnRoad" placeholder="Цена езды" initialValue={parseFloat(rate.OnRoadPrice) === 0 ? "" : parseFloat(rate.OnRoadPrice).toFixed(2)}
                                            onChange={(event: any) => {
                                              rate.OnRoadPrice = event.target.value;
                                            }}/>
                                <InputField name="rateOnPark" placeholder="Цена парковки" initialValue={parseFloat(rate.ParkingPrice) === 0 ? "" : parseFloat(rate.ParkingPrice).toFixed(2)}
                                            onChange={(event: any) => {
                                              rate.ParkingPrice = event.target.value;
                                            }}/>
                                <Button text="Удалить" type="button" onClick={() => {
                                  const updatedRates = selectedCar.Rates.filter((r: any) => r !== rate);
                                  setSelectedCar({...selectedCar, Rates: updatedRates});
                                }}/>
                              </li>
                            )
                          }) : content === "function"
                            ? <>
                              <label>
                                <input type="checkbox" name="function_checkbox1" value="CHILD_CHAIR"
                                       defaultChecked={selectedCar?.Functions.findIndex((x: any) => x.FunctionData === "CHILD_CHAIR") !== -1}
                                       onChange={(event: any) => {
                                         if (event.target.checked) {
                                           selectedCar.Functions.push({
                                             Id: -Math.round(Math.random() * 100000),
                                             FunctionData: event.target.value
                                           });
                                         } else {
                                           const index = selectedCar.Functions.findIndex((x: any) => x.FunctionData === event.target.value);
                                           selectedCar.Functions.splice(index, 1);
                                         }
                                       }}/>
                                Детское кресло
                              </label>
                              <label>
                                <input type="checkbox" name="function_checkbox2" value="TRANSPONDER"
                                       defaultChecked={selectedCar?.Functions.findIndex((x: any) => x.FunctionData === "TRANSPONDER") !== -1}
                                       onChange={(event: any) => {
                                         if (event.target.checked) {
                                           selectedCar.Functions.push({
                                             Id: -Math.round(Math.random() * 100000),
                                             FunctionData: event.target.value
                                           });
                                         } else {
                                           const index = selectedCar.Functions.findIndex((x: any) => x.FunctionData === event.target.value);
                                           selectedCar.Functions.splice(index, 1);
                                         }
                                       }}/>
                                Транспондер
                              </label>
                            </>
                            : content === "damage"
                              ? damage.map((name: any) => {
                                return (
                                  <li key={name}>
                                    <img style={{width: 210, borderRadius: 16, height: 100}} alt="Повреждение"
                                         src={process.env.REACT_APP_API_IP + `/api/transport/get/damage/image?name=${name}`}/>
                                    <Button text="Удалить" type="button" onClick={async () => {

                                      const json = await (await fetch(process.env.REACT_APP_API_IP + `/api/admin/damage/delete?name=${name}`, {
                                        method: "POST",
                                        headers: {
                                          "Authorization": `Bearer ${localStorage.getItem("token")}`
                                        }
                                      })).json();

                                      if (json.status_code !== 200) {
                                        alert(json.message);
                                      }

                                      const updatedImages = damage.filter((r: any) => r !== name);

                                      setDamage([...updatedImages]);
                                    }}/>
                                  </li>
                                )
                              })
                              : null}
                        </ul>
                      {content === "rate" ?
                        <Button text="Добавить" type="button" onClick={() => {
                          const newRate = {
                            Id: -Math.round(Math.random() * 100000),
                            RateName: "",
                            OnRoadPrice: 0,
                            ParkingPrice: 0,
                          };
                          selectedCar.Rates.push(newRate);
                          setSelectedCar({...selectedCar, Rates: selectedCar.Rates});
                        }}/>
                        : null
                      }
                    </>
                </section>
            }
          </section>
        </Popup>
      </section>
    </>
  );
}

export default TransportList;