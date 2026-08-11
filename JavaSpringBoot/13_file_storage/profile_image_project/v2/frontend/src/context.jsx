// src/context/DataProvider.js
import React, { createContext, useEffect, useState } from "react";
// import { useNavigate } from "react-router-dom";
import axios from "axios";

import { toast, ToastContainer } from "react-toastify";
import { API_BASE_URL } from "../config";


export const DataContext = createContext();

const ContextComponent = ({ children }) => {
    // const navigate = useNavigate();
    const [token, setToken] = useState(null);
    const [user, setUser] = useState(null);

    const isTokenExpiringSoon = (jwtToken) => {
        if (!jwtToken) return true;
        try {
            const payload = JSON.parse(atob(jwtToken.split(".")[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            return payload.exp - currentTime < 30;
        } catch (error) {
            return true;
        }
    };


    const apiPost = async(endpoint, data,setButton, myFunc=async()=>{}) => {
        try {
            setButton(true);
            const res = await axios.post(`${API_BASE_URL}/auth/login`, data);
            return res.data;
        } catch (error) {
            console.log(error);
        }
    }


    const apiAuthPost = async (endpoint, body = {}, setButton, myFunc = async () => { }) => {
        try {
            setButton?.(true);
            const freshToken = await checkSession();
            if (!freshToken) return null;

            const res = await axios.post(`${API_BASE_URL}${endpoint}`, body, {
                headers: {
                    Authorization: `Bearer ${freshToken}`,
                    "Content-Type": "application/json",
                },
                timeout: 10000,
            });
            await myFunc();
            return res.data;
        } catch (error) {
            const data = error?.response?.data;
            const message =
                data?.error ||
                data?.message ||
                (Array.isArray(data?.errors) ? data.errors.join(", ") : null) ||
                error.message ||
            "Something went wrong.";
            toast.error(message);
            toast.error(message);
            return null;
        } finally {
            // setLoading(false);
            setButton?.(false);
        }
    };

    const checkSession = () => {
        try {

        } catch (error) {

        }
    }

     return (
    <DataContext.Provider
      value={{
        checkSession,
        apiPost,
        setUser,
        setToken
      }}
    >
      {children}
    </DataContext.Provider>
  );
}

export default ContextComponent;