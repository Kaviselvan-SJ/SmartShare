import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Search, Plus } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();

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

        <div className="flex items-center space-x-3 border-l pl-6 border-gray-200">
          <div className="flex flex-col text-right">
            <span className="text-sm font-semibold text-gray-700">{currentUser?.email?.split('@')[0]}</span>
            <span className="text-xs text-gray-500">User</span>
          </div>
          <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 flex items-center justify-center text-white font-bold text-sm shadow-inner">
            {currentUser?.email?.charAt(0).toUpperCase()}
          </div>
        </div>
      </div>
    </header>
  );
}
