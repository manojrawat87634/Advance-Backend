// src/context/DataProvider.js
import React, { createContext, useEffect, useRef, useState } from "react";
// import { useNavigate } from "react-router-dom";
import axios from "axios";

import { toast, ToastContainer } from "react-toastify";
import { API_BASE_URL } from "./config";
import { useNavigate } from "react-router-dom";


export const DataContext = createContext();

const ContextComponent = ({ children }) => {
    const navigate = useNavigate();
    const [token, setToken] = useState(null);
    const [user, setUser] = useState(null);
    const isRefreshingRef = useRef(false);
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


    const apiPost = async (endpoint, data, setButton, myFunc = async () => { }) => {
        try {
            setButton(true);
            const res = await axios.post(`${API_BASE_URL}${endpoint}`, data);
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

     const apiGet = async (endpoint, params = {}, setData, callFunc = () => { }) => {
    try {
      console.log(endpoint);
      const freshToken = await checkSession();
      if (!freshToken) return null;

      const res = await axios.get(`${API_BASE_URL}${endpoint}`, {
        headers: {
          Authorization: `Bearer ${freshToken}`,
        },
        params,
        timeout: 10000,
      });

      setData(res.data);
      callFunc();
      console.log(res.data);
      return res.data;
    } catch (error) {
      console.log(error)
      checkSession();
      const message = error?.response?.data?.error || "Something went wrong.";
      toast.error(message);
      return null;
    }
  };








  

    const checkSession = async () => {
        try {
            const refreshToken = localStorage.getItem("refreshToken");

            // 1. If no refresh token exists, clear state and return null
            if (!refreshToken) {
                setToken(null);
                setUser(null);
                return null;
            }

            // 2. If access token is valid and not expiring, return it directly
            if (token && user && !isTokenExpiringSoon(token)) {
                return token;
            }
            if (isRefreshingRef.current) return token;
            const res = await apiPost('/auth/get-access-token', { refreshToken }, ()=>{});
            const { accessToken: freshAccessToken, refreshToken: freshRefreshToken, user: userData } = res;

            setToken(freshAccessToken);
            setUser(userData);

            // Rotate Refresh Token in localStorage
            if (freshRefreshToken) {
                localStorage.setItem("refreshToken", freshRefreshToken);
            }

            return freshAccessToken;
        }
        catch (error) {
            console.error("Session refresh failed:", error?.response?.data || error.message);

            const errorMessage =
                error?.response?.data?.message ||
                error?.response?.data?.error ||
                "Session expired. Please log in again.";

            toast.error(errorMessage);
            navigate('/login');
            // logout();
            return null;
        } finally {
            isRefreshingRef.current = false;
        }
    }

    return (
        <DataContext.Provider
            value={{
                checkSession,
                apiPost,
                setUser,
                setToken,
                apiGet,
                apiAuthPost
            }}
        >
            {children}
        </DataContext.Provider>
    );
}

export default ContextComponent;