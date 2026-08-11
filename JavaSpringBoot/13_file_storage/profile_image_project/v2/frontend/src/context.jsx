// src/context/DataProvider.js
import React, { createContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const DataContext = createContext();

const ContextComponent = ({children}) => {
    const navigate = useNavigate();
    const [token, setToken] = useState(null);
    const [user, setUser] = useState(null);

    const checkSession = ()=>{
        
    }
}