import { BrowserRouter, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/auth/login/LoginPage';

function App() {
 return <>
      <Routes>
        <Route path='' Component={LoginPage}></Route>
      </Routes>
  </>

}

export default App;