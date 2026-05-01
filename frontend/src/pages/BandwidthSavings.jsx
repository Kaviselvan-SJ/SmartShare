import React, { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import Loader from '../components/ui/Loader';
import StatCard from '../components/ui/StatCard';
import { HardDrive, Server, Percent } from 'lucide-react';
import toast from 'react-hot-toast';

export default function BandwidthSavings() {
  const [systemStats, setSystemStats] = useState(null);
  const [userStats, setUserStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [sysRes, userRes] = await Promise.all([
          axiosClient.get('/analytics/bandwidth/system'),
          axiosClient.get('/analytics/bandwidth/user')
        ]);
        setSystemStats(sysRes.data);
        setUserStats(userRes.data);
      } catch (error) {
        if (!error.handled) toast.error('Failed to load bandwidth statistics');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1024 * 1024 * 1024) return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  if (loading) return <Loader text="Calculating storage metrics..." />;
  if (!systemStats || !userStats) return null;

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Bandwidth & Storage Savings</h1>
        <p className="text-slate-500 mt-1">See how much storage deduplication and compression saves.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* User Stats Panel */}
        <div className="bg-gradient-to-br from-blue-600 to-indigo-700 rounded-2xl p-8 text-white shadow-lg">
          <div className="flex items-center space-x-3 mb-8 opacity-90">
            <HardDrive size={24} />
            <h2 className="text-xl font-semibold">Your Personal Savings</h2>
          </div>
          
          <div className="space-y-6">
            <div>
              <p className="text-blue-200 text-sm font-medium mb-1">Total Storage Saved</p>
              <p className="text-4xl font-bold">{formatSize(userStats.totalSavedBytes)}</p>
            </div>
            
            <div className="grid grid-cols-2 gap-4 border-t border-blue-500/30 pt-6">
              <div>
                <p className="text-blue-200 text-xs uppercase tracking-wider mb-1">Original Uploads</p>
                <p className="font-semibold">{formatSize(userStats.totalOriginalSize)}</p>
              </div>
              <div>
                <p className="text-blue-200 text-xs uppercase tracking-wider mb-1">Actual Storage Used</p>
                <p className="font-semibold">{formatSize(userStats.totalCompressedSize)}</p>
              </div>
            </div>
          </div>
        </div>

        {/* System Stats Panel */}
        <div className="bg-white rounded-2xl p-8 border border-gray-100 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-3 text-slate-700">
                <Server size={24} className="text-emerald-500" />
                <h2 className="text-xl font-semibold">Global System Savings</h2>
              </div>
              <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider">
                System Wide
              </span>
            </div>
            
            <div className="space-y-6">
              <div>
                <p className="text-slate-500 text-sm font-medium mb-1">Total Storage Prevented</p>
                <p className="text-4xl font-bold text-slate-800">{formatSize(systemStats.totalSavedBytes)}</p>
              </div>
            </div>
          </div>

          <div className="mt-8 bg-slate-50 rounded-xl p-4 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Percent className="text-purple-500" />
              <span className="text-sm font-medium text-slate-600">Average Compression Ratio</span>
            </div>
            <span className="text-lg font-bold text-slate-800">
              {(systemStats.averageCompressionRatio * 100).toFixed(2)}%
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
