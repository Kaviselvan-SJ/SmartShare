import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import Loader from '../components/ui/Loader';
import StatCard from '../components/ui/StatCard';
import { Shield, Users, Database, ArrowUpCircle, ArrowDownCircle, Server, Activity, LogOut } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';

export default function AdminDashboard() {
  const [overview, setOverview] = useState(null);
  const [usageTrends, setUsageTrends] = useState(null);
  const [popularTags, setPopularTags] = useState([]);
  const [storageStats, setStorageStats] = useState(null);
  const [recentActivity, setRecentActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const { logout, currentUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        const [overviewRes, trendsRes, tagsRes, storageRes, activityRes] = await Promise.all([
          axiosClient.get('/admin/overview'),
          axiosClient.get('/admin/usage-trends'),
          axiosClient.get('/admin/popular-tags'),
          axiosClient.get('/admin/storage-stats'),
          axiosClient.get('/admin/recent-activity')
        ]);
        
        setOverview(overviewRes.data);
        
        // Process usage trends for charting
        const trendData = [];
        
        // Safely extract unique dates across all three maps
        const allDates = new Set([
            ...Object.keys(trendsRes.data.uploadsPerDay || {}),
            ...Object.keys(trendsRes.data.downloadsPerDay || {}),
            ...Object.keys(trendsRes.data.newUsersPerDay || {})
        ]);
        
        Array.from(allDates).sort().forEach(date => {
            trendData.push({
                date,
                uploads: trendsRes.data.uploadsPerDay[date] || 0,
                downloads: trendsRes.data.downloadsPerDay[date] || 0,
                newUsers: trendsRes.data.newUsersPerDay[date] || 0
            });
        });

        setUsageTrends(trendData);
        setPopularTags(tagsRes.data.map(t => ({ name: t.tag, value: t.usageCount })));
        setStorageStats(storageRes.data);
        setRecentActivity(activityRes.data);
        
      } catch (error) {
        console.error("Admin fetch error", error);
        toast.error('Failed to load admin dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchAdminData();
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      toast.error('Failed to log out');
    }
  };

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1024 * 1024 * 1024) return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  if (loading) return <div className="min-h-screen bg-slate-50"><Loader text="Loading secure admin telemetry..." /></div>;
  if (!overview) return null;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6 sticky top-0 z-10 shadow-sm">
        <div className="flex items-center space-x-3">
          <Shield size={24} className="text-red-500" />
          <span className="text-xl font-bold tracking-wider text-slate-800">SmartShare <span className="text-red-500">Admin</span></span>
        </div>
        <div className="flex items-center space-x-6">
          <div className="flex items-center space-x-3 border-r pr-6 border-gray-200">
            <div className="flex flex-col text-right">
              <span className="text-sm font-semibold text-gray-700">{currentUser?.email?.split('@')[0]}</span>
              <span className="text-xs text-red-500 font-medium">Administrator</span>
            </div>
            <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-red-500 to-orange-500 flex items-center justify-center text-white font-bold text-sm shadow-inner">
              A
            </div>
          </div>
          <button 
            onClick={handleLogout}
            className="flex items-center space-x-2 text-slate-500 hover:text-red-600 transition-colors font-medium text-sm"
          >
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto p-8 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-slate-800">Admin Control Center</h1>
            <p className="text-slate-500 mt-1">System observability and privacy-safe analytics.</p>
          </div>
        </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Total Users" value={overview.totalUsers} icon={Users} color="indigo" subtitle={`${overview.activeUsersLast24Hours} active in 24h`} />
        <StatCard title="System Files" value={overview.totalFiles} icon={Database} color="blue" />
        <StatCard title="Total Uploads" value={overview.totalUploads} icon={ArrowUpCircle} color="emerald" />
        <StatCard title="Total Downloads" value={overview.totalDownloads} icon={ArrowDownCircle} color="orange" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-800 mb-6">7-Day Usage Trends</h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={usageTrends} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="date" tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                <RechartsTooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                <Line type="monotone" dataKey="uploads" stroke="#10b981" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} name="Uploads" />
                <Line type="monotone" dataKey="downloads" stroke="#f97316" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} name="Downloads" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm space-y-6">
          <div className="flex items-center space-x-2">
            <Server className="text-purple-500" />
            <h2 className="text-lg font-semibold text-slate-800">Storage Utilization</h2>
          </div>
          
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-slate-500 text-sm">Original Ingress</span>
              <span className="font-semibold text-slate-800">{formatSize(storageStats.totalOriginalStorage)}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500 text-sm">Compressed Storage</span>
              <span className="font-semibold text-blue-600">{formatSize(storageStats.totalCompressedStorage)}</span>
            </div>
            <div className="w-full bg-slate-100 rounded-full h-2.5 mt-2 overflow-hidden">
              <div 
                className="bg-blue-500 h-2.5 rounded-full" 
                style={{ width: `${(storageStats.totalCompressedStorage / Math.max(storageStats.totalOriginalStorage, 1)) * 100}%` }}
              ></div>
            </div>
            <div className="flex justify-between items-center pt-4 border-t border-slate-100">
              <span className="text-slate-500 text-sm">Total Saved</span>
              <span className="font-bold text-emerald-600">{formatSize(storageStats.totalSavedStorage)}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
        <div className="flex items-center space-x-2 mb-6">
          <Activity className="text-blue-500" />
          <h2 className="text-lg font-semibold text-slate-800">Recent System Activity (Privacy Safe)</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500 border-b border-gray-100">
              <tr>
                <th className="px-4 py-3 font-medium">Timestamp</th>
                <th className="px-4 py-3 font-medium">Event Type</th>
                <th className="px-4 py-3 font-medium">Device Type</th>
                <th className="px-4 py-3 font-medium">Short Code</th>
                <th className="px-4 py-3 font-medium">Masked File Hash</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {recentActivity.map((act, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50">
                  <td className="px-4 py-3 text-slate-500">{new Date(act.timestamp).toLocaleString()}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${act.eventType === 'UPLOAD' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700'}`}>
                      {act.eventType}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{act.deviceType}</td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-500">{act.shortCode}</td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">{act.fileHashMasked}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      </main>
    </div>
  );
}
