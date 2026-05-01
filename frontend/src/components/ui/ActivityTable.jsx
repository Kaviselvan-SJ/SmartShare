import React from 'react';
import { Monitor, Smartphone, Globe, Clock } from 'lucide-react';

export default function ActivityTable({ activities }) {
  if (!activities || activities.length === 0) {
    return (
      <div className="bg-white p-8 rounded-2xl border border-gray-100 text-center shadow-sm">
        <p className="text-gray-500">No recent activity detected.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-gray-600">
          <thead className="bg-slate-50 text-slate-500 text-xs uppercase border-b border-gray-100">
            <tr>
              <th className="px-6 py-4 font-medium">Time</th>
              <th className="px-6 py-4 font-medium">Short Code</th>
              <th className="px-6 py-4 font-medium">Device</th>
              <th className="px-6 py-4 font-medium">Browser</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {activities.map((activity, index) => (
              <tr key={index} className="hover:bg-slate-50/50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center space-x-2">
                    <Clock size={14} className="text-gray-400" />
                    <span>{new Date(activity.timestamp).toLocaleString()}</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className="font-mono bg-slate-100 text-slate-800 px-2 py-1 rounded text-xs">
                    {activity.shortCode}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center space-x-2">
                    {activity.deviceType?.toLowerCase().includes('mobile') ? (
                      <Smartphone size={16} className="text-indigo-500" />
                    ) : (
                      <Monitor size={16} className="text-blue-500" />
                    )}
                    <span>{activity.deviceType || 'Desktop'}</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center space-x-2">
                    <Globe size={16} className="text-emerald-500" />
                    <span>{activity.browser || 'Unknown'}</span>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
