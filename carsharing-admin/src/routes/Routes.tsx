import React from "react";
import { createBrowserRouter, Outlet, RouterProvider } from "react-router-dom";
import NavigationMenu from "../components/navigation-menu/NavigationMenu";
import MainContent from "../components/main-content/MainContent";
import NavigationButton from "../components/nav-button/NavigationButton";
import Dashboard from "../assets/Dashboard";
import Car from "../assets/Car";
import User from "../assets/User";
import Report from "../assets/Report";


const router = createBrowserRouter([
  {
    path: '/',
    element: <div></div>
  },
  {
    element: <>
      <NavigationMenu>
        <NavigationButton
          route="/dashboard"
          icon={<Dashboard/>}
          text="Дашборд"/>
        <NavigationButton
          route="/transport"
          icon={<Car/>}
          text="Транспорт"/>
        <NavigationButton
          route="/users"
          icon={<User/>}
          text="Пользователи"/>
        <NavigationButton
          route="/reports"
          icon={<Report/>}
          text="Отчёты"/>
      </NavigationMenu>
      <Outlet/>
    </>,
    children: [
      {
        path: '/',
        element: <MainContent />
      },
      {
        path: '/dashboard',
        element: <MainContent header="Дашборд" />
      },
      {
        path: '/transport',
        element: <MainContent header="Транспорт" />
      },
      {
        path: '/users',
        element: <MainContent header="Пользователи" />
      },
      {
        path: '/reports',
        element: <MainContent header="Отчёты" />
      }
    ]
  },
]);

function Router() {
  return(
    <RouterProvider router={router}/>
  );
}

export default Router;

