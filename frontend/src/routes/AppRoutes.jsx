import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from '../pages/Login';
import Register from '../pages/Register';
import Dashboard from '../pages/Dashboard';
import Upload from '../pages/Upload';
import MyFiles from '../pages/MyFiles';
import TagSearch from '../pages/TagSearch';
import Analytics from '../pages/Analytics';
import BandwidthSavings from '../pages/BandwidthSavings';
import AdminDashboard from '../pages/AdminDashboard';
import FileAccess from '../pages/FileAccess';
import FileDetails from '../pages/FileDetails';
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';
import AdminRouteGuard from './AdminRouteGuard';
import AppLayout from '../components/layout/AppLayout';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/f/:shortCode" element={<FileAccess />} />
      
      {/* Protected Routes inside AppLayout */}
      <Route 
        path="/dashboard" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><Dashboard /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />
      <Route 
        path="/upload" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><Upload /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />
      <Route 
        path="/files" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><MyFiles /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />
      <Route 
        path="/files/:fileId" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><FileDetails /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />
      <Route 
        path="/search" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><TagSearch /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />
      <Route 
        path="/analytics" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><Analytics /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />
      <Route 
        path="/bandwidth" 
        element={<ProtectedRoute><AdminRouteGuard><AppLayout><BandwidthSavings /></AppLayout></AdminRouteGuard></ProtectedRoute>} 
      />

      {/* Admin Route */}
      <Route 
        path="/admin" 
        element={<ProtectedRoute><AdminRoute><AppLayout><AdminDashboard /></AppLayout></AdminRoute></ProtectedRoute>} 
      />
      
      {/* Fallback route */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
