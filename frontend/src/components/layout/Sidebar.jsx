import React from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  UploadCloud,
  Files,
  Tags,
  BarChart3,
  Activity,
  LogOut,
  HardDrive,
  Shield,
  X
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Sidebar({ isOpen, onClose }) {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const adminEmailsString = import.meta.env.VITE_ADMIN_EMAILS || '';
  const adminEmails = adminEmailsString.split(',').map(e => e.trim().toLowerCase());
  const isAdmin = currentUser && currentUser.email && adminEmails.includes(currentUser.email.toLowerCase());

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  };

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Upload File', path: '/upload', icon: UploadCloud },
    { name: 'My Files', path: '/files', icon: Files },
    { name: 'Tag Search', path: '/search', icon: Tags },
    { name: 'Analytics', path: '/analytics', icon: BarChart3 },
    { name: 'Bandwidth Savings', path: '/bandwidth', icon: HardDrive },
  ];

  if (isAdmin) {
    navItems.push({ name: 'Admin Control', path: '/admin', icon: Shield });
  }

  return (
  <>
    {/* Mobile overlay backdrop */}
    {isOpen && (
      <div
        className="fixed inset-0 bg-black/50 z-40 md:hidden transition-opacity"
        onClick={onClose}
      />
    )}

    {/* Sidebar */}
    <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 text-white min-h-screen flex flex-col shadow-xl transform transition-transform duration-300 ease-in-out md:relative md:translate-x-0 ${isOpen ? 'translate-x-0' : '-translate-x-full'}`}>
      <div className="p-6 flex items-center justify-between border-b border-slate-800">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
            <UploadCloud size={20} className="text-white" />
          </div>
          <span className="text-xl font-bold tracking-wider">SmartShare</span>
        </div>
        <button
          className="md:hidden text-gray-400 hover:text-white p-1"
          onClick={onClose}
        >
          <X size={20} className="lucide lucide-x" />
        </button>
      </div>

      <div className="flex-1 py-6 flex flex-col space-y-1 px-3">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;
          return (
            <NavLink
              key={item.name}
              to={item.path}
              className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors ${isActive
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`}
              onClick={() => {
                if (window.innerWidth < 768) {
                  onClose();
                }
              }}
            >
              <Icon size={18} />
              <span className="font-medium text-sm">{item.name}</span>
            </NavLink>
          );
        })}
      </div>

      <div className="p-4 border-t border-slate-800">
        <button
          onClick={handleLogout}
          className="flex items-center space-x-3 w-full px-4 py-3 text-slate-300 hover:bg-red-500/10 hover:text-red-400 rounded-lg transition-colors"
        >
          <LogOut size={18} />
          <span className="font-medium text-sm">Logout</span>
        </button>
      </div>
    </div>
  </>
);
}
