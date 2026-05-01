import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

export default function Dashboard() {
  const [error, setError] = useState('');
  const [apiResponse, setApiResponse] = useState(null);
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    setError('');
    try {
      await logout();
      navigate('/login');
    } catch {
      setError('Failed to log out');
    }
  }

  async function testApi() {
    try {
      const response = await axiosClient.get('/test/authenticated');
      setApiResponse(response.data);
    } catch (err) {
      setApiResponse({ error: err.message });
    }
  }

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <button 
          onClick={handleLogout}
          className="rounded bg-red-500 px-4 py-2 text-white hover:bg-red-700"
        >
          Log Out
        </button>
      </div>

      <div className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Profile Details</h2>
        {error && <div className="mb-4 text-red-500">{error}</div>}
        <p><strong>Email:</strong> {currentUser.email}</p>
        <p><strong>UID:</strong> {currentUser.uid}</p>
      </div>

      <div className="rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Backend Connection Test</h2>
        <button 
          onClick={testApi}
          className="mb-4 rounded bg-green-500 px-4 py-2 text-white hover:bg-green-700"
        >
          Test Protected Route
        </button>
        {apiResponse && (
          <pre className="rounded bg-gray-100 p-4 overflow-x-auto">
            {JSON.stringify(apiResponse, null, 2)}
          </pre>
        )}
      </div>
    </div>
  );
}
