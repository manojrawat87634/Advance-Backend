import { BrowserRouter, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/auth/login/LoginPage';
import ProfilePage from './pages/profile/ProfilePage';

function App() {
 return <>
      <Routes>
          <Route path='/login' Component={LoginPage} />
         <Route path='' element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          } />



      </Routes>
  </>

}

export default App;