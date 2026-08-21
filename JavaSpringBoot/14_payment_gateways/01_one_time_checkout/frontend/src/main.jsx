import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import ContextComponent from './context.jsx';
import { BrowserRouter } from 'react-router-dom';
// import ContextComponent from './context.jsx';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
  <ContextComponent >
      <App />
  </ContextComponent>
    </BrowserRouter>
)