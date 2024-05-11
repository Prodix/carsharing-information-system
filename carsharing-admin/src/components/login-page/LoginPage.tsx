import React from "react";
import './LoginPage.css';
import InputField from "../input-field/InputField";
import Button from "../button/Button";
import {Form, useActionData} from "react-router-dom";

function LoginPage() {
  let actionData = useActionData() as { error: string } | undefined;
  return(
    <>
      <Form className="login-form"
            method="post"
            replace>
        <h1>AutoShare Dashboard<span>.</span></h1>
        <section>
          <InputField
            placeholder="Почта"
            type="text"
            name="email"/>
          <InputField
            placeholder="Пароль"
            type="password"
            name="password"/>
          <p>{actionData && actionData.error ? (actionData.error) : null}</p>
          <Button text="Войти"/>
        </section>
      </Form>
    </>
  );
}

interface AuthProvider {
  isAuthenticated: boolean;
  token: null | string;
  signin(email: string, password: string): Promise<void>;
  signout(): Promise<void>;
}

export const authProvider: AuthProvider = {
  isAuthenticated: false,
  token: null,
  async signin(email: string, password: string) {
    const formData = new FormData();
    formData.set('email', email);
    formData.set('password', password);
    await fetch(process.env.REACT_APP_API_IP + "/api/admin/signin", {
      method: "POST",
      body: formData
    }).then(async response => {
        const result = await response.json()
        if (result.status_code !== 200) {
          throw new Error(result.message)
        }
        authProvider.isAuthenticated = true;
        authProvider.token = result.token;
        localStorage.setItem("token", result.token);
    });
  },
  async signout() {
    authProvider.isAuthenticated = false;
    authProvider.token = null;
    localStorage.removeItem("token");
  },
};

export default LoginPage;
