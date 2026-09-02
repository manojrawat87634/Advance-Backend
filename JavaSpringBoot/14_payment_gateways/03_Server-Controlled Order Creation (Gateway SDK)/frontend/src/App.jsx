import { BrowserRouter, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/auth/login/LoginPage';
import ProfilePage from './pages/profile/ProfilePage';
import ProtectedRoute from './components/ProtectedRoutes';
import Product from './pages/ecom/Product';
import UploadNotePage from './pages/upload/UploadNotePage';

function App() {
 return <>
      <Routes>
          <Route path='/login' Component={LoginPage} />
         <Route path='/' element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          } />
         <Route path='/products' element={
            <ProtectedRoute>
              <Product />
            </ProtectedRoute>
          } />
         <Route path='/upload-notes' element={
            <ProtectedRoute>
              <UploadNotePage />
            </ProtectedRoute>
          } />



      </Routes>
  </>

}

export default App;