import React from "react";
import {createBrowserRouter, LoaderFunctionArgs, Outlet, redirect, RouterProvider} from "react-router-dom";
import NavigationMenu from "../components/navigation-menu/NavigationMenu";
import MainContent from "../components/main-content/MainContent";
import NavigationButton from "../components/nav-button/NavigationButton";
import Dashboard from "../assets/Dashboard";
import Car from "../assets/Car";
import User from "../assets/User";
import Report from "../assets/Report";
import LoginPage, {authProvider} from "../components/login-page/LoginPage";


const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage/>,
    action: loginAction,
    loader: loginLoader
  },
  {
    loader: protectedLoader,
    element: <>
      <NavigationMenu>
        <NavigationButton
          routes={["/dashboard", "/"]}
          icon={<Dashboard/>}
          text="Дашборд"/>
        <NavigationButton
          routes={["/transport"]}
          icon={<Car/>}
          text="Транспорт"/>
        <NavigationButton
          routes={["/users"]}
          icon={<User/>}
          text="Пользователи"/>
        <NavigationButton
          routes={["/reports"]}
          icon={<Report/>}
          text="Отчёты"/>
      </NavigationMenu>
      <Outlet/>
    </>,
    children: [
      {
        path: '/',
        element: <MainContent header="Дашборд" />
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
    ],
  },
  {
    path: '/logout',
    action: async () => {
      await authProvider.signout();
      return redirect('/login');
    }
  }
]);

async function loginAction({ request }: LoaderFunctionArgs) {
  let formData = await request.formData();
  let email = formData.get("email") as string | null;
  let password = formData.get("password") as string | null;

  if (!email) {
    return {
      error: "Вы должны ввести логин",
    };
  }

  if (!password) {
    return {
      error: "Вы должны ввести пароль",
    };
  }

  try {
    await authProvider.signin(email, password);
  } catch (error) {
    if (error instanceof Error) {
      return {
        error: error.message,
      };
    } else {
      return {
        error: "Неизвестная ошибка",
      };
    }
  }

  return redirect("/");
}

async function loginLoader() {
  if (localStorage.getItem("token"))
    return redirect("/");
  return null;
}

async function protectedLoader() {
  let token = localStorage.getItem("token");
  if (token) {
    await fetch(process.env.REACT_APP_API_IP + "/api/account/signin", {
      method: "POST",
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).then(async response => {
      const result = await response.json()
      if (result.status_code !== 200) {
        throw new Error(result.message)
      }
      authProvider.isAuthenticated = true;
      authProvider.token = result.token;
      localStorage.setItem("token", result.token);
      token = result.token
    });
  } else {
    return redirect('/login');
  }
  return token;
}

function Router() {
  return(
    <RouterProvider router={router}/>
  );
}

export default Router;

