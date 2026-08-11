import React, { useContext, useEffect } from "react";
import DynamicForm from "../../../components/ComonForm/CommonForm";
import { MdEmail, MdLock } from 'react-icons/md';
import logo from "./logo.png";
import { DataContext } from "../../../context";
// import { useNavigate, useNavigation } from "react-router-dom";
import axios from "axios";

const AuthLinks = () => {

  const {user} = useContext(DataContext);
//   const navigate = useNavigate();
  if (user){
    // navigate('/home', {replace : true});
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
    return <>Login Page</>
}

export default LoginPage;