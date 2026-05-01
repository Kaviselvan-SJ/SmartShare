import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  };

  if (!currentUser) return null;

  return (
    <nav className="bg-blue-600 p-4 text-white flex justify-between items-center shadow-md">
      <div className="flex items-center space-x-6">
        <h1 className="text-xl font-bold">SmartShare</h1>
        <Link to="/dashboard" className="hover:text-blue-200 transition">Dashboard</Link>
        <Link to="/upload" className="hover:text-blue-200 transition">Upload File</Link>
      </div>
      <div className="flex items-center space-x-4">
        <span className="text-sm bg-blue-700 px-3 py-1 rounded-full">{currentUser.email}</span>
        <button 
          onClick={handleLogout}
          className="bg-red-500 hover:bg-red-600 px-4 py-1 rounded transition"
        >
          Logout
        </button>
      </div>
    </nav>
  );
}
