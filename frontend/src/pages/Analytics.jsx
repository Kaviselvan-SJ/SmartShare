import React, { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import Loader from '../components/ui/Loader';
import AnalyticsChart from '../components/ui/AnalyticsChart';
import { FileStack } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Analytics() {
  const [topFiles, setTopFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const filesRes = await axiosClient.get('/analytics/dashboard/top-files');
        setTopFiles(filesRes.data);
      } catch (error) {
        toast.error('Failed to load analytics data');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <Loader text="Crunching numbers..." />;

  const chartData = topFiles.map(f => ({
    name: f.fileName.length > 15 ? f.fileName.substring(0, 15) + '...' : f.fileName,
    value: f.downloadCount
  }));

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Your Analytics</h1>
        <p className="text-slate-500 mt-1">Deep dive into your file usage and trends.</p>
      </div>

      <div className="grid grid-cols-1 gap-8">
        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm space-y-6">
          <div className="flex items-center space-x-2">
            <FileStack className="text-blue-500" />
            <h2 className="text-lg font-semibold text-slate-800">Top Downloaded Files</h2>
          </div>
          <AnalyticsChart data={chartData} color="#3b82f6" />
        </div>
      </div>
    </div>
  );
}
