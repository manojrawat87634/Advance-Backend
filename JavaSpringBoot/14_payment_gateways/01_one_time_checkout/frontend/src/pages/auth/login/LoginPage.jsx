import React, { useContext, useEffect } from "react";
import DynamicForm from "../../../components/ComonForm/CommonForm";
import { MdEmail, MdLock } from 'react-icons/md';
import logo from "./logo.png";
import { DataContext } from "../../../context";
import { useNavigate, useNavigation } from "react-router-dom";
import axios from "axios";
import { API_BASE_URL } from "../../../config";

const AuthLinks = () => {

  const { user } = useContext(DataContext);
  const navigate = useNavigate();
  if (user) {
    navigate('/home', { replace: true });
  }

  return (
    <div className="text-center mt-3 space-y-2">
      <p>
        <a
          href="/forgot-password"
          className="text-blue-500 hover:underline text-sm"
        >
          Forgot Password?
        </a>
      </p>
      <p className="text-sm text-gray-600">
        Don’t have an account?{" "}
        <a
          href="/signup"
          className="text-blue-500 hover:underline font-medium"
        >
          Create New Account
        </a>
      </p>
    </div>
  );
};

const LoginPage = () => {
  const navigate = useNavigate();

  const { user, checkSession, setToken, apiPost } = useContext(DataContext);

  const loginFields = [
    { name: 'email', type: 'email', label: 'Email Address', placeholder: 'Enter your email', required: true, icon: MdEmail },
    { name: 'password', type: 'password', label: 'Password', placeholder: 'Enter your password', required: true, icon: MdLock }
  ];

  const getUserInfo = async () => {
    await checkSession();
    if (user?.user_type == 'admin') {
      navigate("/home/")
    }
  }

  useEffect(() => {
    getUserInfo();
  }, []);

  const handleSubmit = async (values, { setSubmitting, resetForm }) => {
    const data = await apiPost('/auth/login', values, setSubmitting, () => { resetForm });
    const { accessToken, refreshToken } = data;
    console.log(data);
    setToken(accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    navigate("/");
  }

  return <>
    <section className="gradient-form min-h-screen bg-neutral-200 dark:bg-neutral-700 flex items-center justify-center py-6">
      <div className="w-full max-w-2xl px-4 sm:px-6">
        <DynamicForm
          gradientColors={'linear-gradient(to right, #ee7724, #d8363a, #dd3675, #b44593)'}
          fields={loginFields}
          heading="Login to Your Account"
          buttonTitle="Login"
          postUrl="/login/"
          theme="light"
          logo={logo}
          handleSubmit={handleSubmit}
          ExtraContent={AuthLinks}
        />
      </div>
    </section>
  </>
}

export default LoginPage;