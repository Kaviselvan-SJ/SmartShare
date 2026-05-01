import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminRouteGuard({ children }) {
  const { currentUser } = useAuth();
  
  const adminEmailsString = import.meta.env.VITE_ADMIN_EMAILS || '';
  const adminEmails = adminEmailsString.split(',').map(e => e.trim().toLowerCase());
  
  const isAdmin = currentUser && currentUser.email && adminEmails.includes(currentUser.email.toLowerCase());

  if (isAdmin) {
    return <Navigate to="/admin" replace />;
  }

  return children;
}
