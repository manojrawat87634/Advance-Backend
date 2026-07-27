import { Route, Routes } from 'react-router-dom';
import LoginPage from './pages/auth/LoginPage/LoginPage';
import "./index.css";
import Navbar from './Component/Navbar/Navbar';
import { DataContext } from './context';
import { useContext, useEffect } from 'react';
import ProtectedRoute from './Component/ProtectedRoutes';
import { ToastContainer } from 'react-toastify';
import HomePage from './pages/HomePage/HomePage';
import StudentDashboard from './pages/StudentDashboard/StudentEnrollment';
import BatchManagement from './pages/BatchManagment/BatchManagement';
import ClassManagement from './pages/ClassManagement/ClassManagement';
import FeeManagement from './pages/FeeManagement/FeeMangement';
import AdminUserProfile from './pages/HomePage/HomePage';

function App() {
  const { token, user } = useContext(DataContext);
  useEffect(() => {
  }, []);

  return (
    <>
      <ToastContainer />
      <Navbar user={user} />
      <div className={user ? `mt-20` : ''}>
        <Routes>
          <Route path='/login' Component={LoginPage} />
          <Route path='' element={
            <ProtectedRoute>
              <AdminUserProfile />
            </ProtectedRoute>
          } />
          <Route path='/enrolled-student' element={
            <ProtectedRoute>
              <AdminUserProfile />
            </ProtectedRoute>
          } />
          <Route path='/enrolled-student/:id' element={
            <ProtectedRoute>
              <AdminUserProfile />
            </ProtectedRoute>
          } />
          <Route path='/fee-management' element={
            <ProtectedRoute>
              <FeeManagement />
            </ProtectedRoute>
          } />
          <Route path='/fee-management/:id' element={
            <ProtectedRoute>
              <FeeManagement />
            </ProtectedRoute>
          } />
          <Route path='/home' element={
            <ProtectedRoute>
              <AdminUserProfile />
            </ProtectedRoute>
          } />
          <Route path='/batch-management' element={
            <ProtectedRoute>
              <BatchManagement />
            </ProtectedRoute>
          } />
          <Route path='/batch-management/:id' element={
            <ProtectedRoute>
              <BatchManagement />
            </ProtectedRoute>
          } />
          <Route path='/class-management' element={
            <ProtectedRoute>
              <ClassManagement />
            </ProtectedRoute>
          } />
          <Route path='/class-management/:id' element={
            <ProtectedRoute>
              <ClassManagement />
            </ProtectedRoute>
          } />
        </Routes>
      </div>
    </>
  );
}

export default App;
