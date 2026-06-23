import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import { BrowserRouter } from 'react-router-dom';
import { DataProviderFuncComp } from './context.jsx';
import { GoogleOAuthProvider } from '@react-oauth/google';

createRoot(document.getElementById('root')).render(
  <GoogleOAuthProvider
   clientId="267309777878-ui9qtj1bqatrn31ivv2oj9noi8p2gj54.apps.googleusercontent.com"
  >
  <BrowserRouter>
    <DataProviderFuncComp >
      <App />
    </DataProviderFuncComp>
  </BrowserRouter>
  </GoogleOAuthProvider>
)
