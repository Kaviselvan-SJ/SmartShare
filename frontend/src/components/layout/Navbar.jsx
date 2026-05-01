import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Search, Plus } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';

export default function Navbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    if (currentUser) {
      axiosClient.get('/user/profile')
        .then(res => setProfile(res.data))
        .catch(err => console.error('Failed to load profile', err));
    }
  }, [currentUser]);

  return (
    <header className="bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6 sticky top-0 z-10 shadow-sm">
      <div className="flex items-center bg-gray-100 rounded-full px-4 py-2 w-96 border border-transparent focus-within:border-blue-500 focus-within:bg-white transition-colors">
        <Search size={18} className="text-gray-400" />
        <input 
          type="text" 
          placeholder="Search by tags..." 
          className="bg-transparent border-none outline-none ml-3 w-full text-sm text-gray-700"
          onKeyDown={(e) => {
            if (e.key === 'Enter' && e.target.value) {
              navigate(`/search?tags=${e.target.value}`);
            }
          }}
        />
      </div>

      <div className="flex items-center space-x-6">
        <button 
          onClick={() => navigate('/upload')}
          className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-full text-sm font-medium transition-colors shadow-sm hover:shadow"
        >
          <Plus size={16} />
          <span>Upload</span>
        </button>

        <button className="relative text-gray-500 hover:text-gray-700 transition-colors">
          <Bell size={20} />
          <span className="absolute top-0 right-0 -mt-1 -mr-1 flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-3 w-3 bg-blue-500"></span>
          </span>
        </button>

        <div className="relative border-l pl-6 border-gray-200">
          <button 
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center space-x-3 focus:outline-none"
          >
            <div className="flex flex-col text-right">
              <span className="text-sm font-semibold text-gray-700">
                {profile?.displayName || currentUser?.email?.split('@')[0]}
              </span>
              <span className="text-xs text-gray-500">{profile?.jobProfile || 'User'}</span>
            </div>
            
            {(profile?.profileImageUrl || currentUser?.photoURL) ? (
              <img 
                src={profile?.profileImageUrl || currentUser?.photoURL} 
                alt="Avatar" 
                className="h-9 w-9 rounded-full object-cover shadow-inner border border-gray-200" 
              />
            ) : (
              <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 flex items-center justify-center text-white font-bold text-sm shadow-inner">
                {(profile?.displayName || currentUser?.displayName)?.charAt(0).toUpperCase() || currentUser?.email?.charAt(0).toUpperCase()}
              </div>
            )}
          </button>

          {dropdownOpen && (
            <div className="absolute right-0 mt-3 w-48 bg-white rounded-xl shadow-lg py-1 border border-gray-100 z-50">
              <button
                onClick={() => {
                  setDropdownOpen(false);
                  navigate('/settings');
                }}
                className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-slate-50 transition-colors"
              >
                Account Settings
              </button>
              <button
                onClick={async () => {
                  setDropdownOpen(false);
                  await logout();
                  navigate('/login');
                }}
                className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
