import { BrowserRouter, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/auth/login/LoginPage';

function App() {
  <>
    <BrowserRouter>
      <Routes>

        <Route path='/' Component={LoginPage}></Route>
      </Routes>
    </BrowserRouter>
  </>

}

export default App;