import React, { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import StatCard from '../components/ui/StatCard';
import ActivityTable from '../components/ui/ActivityTable';
import AnalyticsChart from '../components/ui/AnalyticsChart';
import Loader from '../components/ui/Loader';
import { Files, Download, HardDrive, Percent, Tag } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Dashboard() {
  const [overview, setOverview] = useState(null);
  const [recentActivity, setRecentActivity] = useState([]);
  const [popularTags, setPopularTags] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [overviewRes, activityRes, tagsRes] = await Promise.all([
          axiosClient.get('/analytics/dashboard/overview'),
          axiosClient.get('/analytics/dashboard/recent-activity'),
          axiosClient.get('/analytics/dashboard/popular-tags')
        ]);
        
        setOverview(overviewRes.data);
        setRecentActivity(activityRes.data);
        
        // Transform tags for Recharts
        const formattedTags = tagsRes.data.map(t => ({
          name: t.tag,
          value: t.usageCount
        }));
        setPopularTags(formattedTags);
      } catch (error) {
        if (!error.handled) toast.error('Failed to load dashboard data');
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1024 * 1024 * 1024) return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  if (loading) return <Loader text="Loading dashboard metrics..." />;
  if (!overview) return null;

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Welcome Back</h1>
        <p className="text-slate-500 mt-1">Here is what's happening with your SmartShare workspace today.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard 
          title="Total Files" 
          value={overview.totalFiles} 
          icon={Files} 
          color="blue" 
        />
        <StatCard 
          title="Total Downloads" 
          value={overview.totalDownloads} 
          icon={Download} 
          color="indigo" 
        />
        <StatCard 
          title="Bandwidth Saved" 
          value={formatSize(overview.totalBandwidthSaved)} 
          icon={HardDrive} 
          color="emerald" 
          subtitle="System-wide compression savings"
        />
        <StatCard 
          title="Avg. Compression" 
          value={(overview.averageCompressionRatio * 100).toFixed(1) + '%'} 
          icon={Percent} 
          color="purple" 
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Recent Download Activity</h2>
          </div>
          <ActivityTable activities={recentActivity} />
        </div>
        
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <Tag size={20} className="text-slate-700" />
            <h2 className="text-lg font-semibold text-slate-800">Trending Tags</h2>
          </div>
          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
            <AnalyticsChart data={popularTags} xKey="name" yKey="value" color="#8b5cf6" />
          </div>
        </div>
      </div>
    </div>
  );
}
