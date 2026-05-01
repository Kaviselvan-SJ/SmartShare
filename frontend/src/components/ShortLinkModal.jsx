import React, { useState } from 'react';
import axiosClient from '../api/axiosClient';
import { X, Copy, Link, Shield, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

export default function ShortLinkModal({ fileId, fileName, onClose }) {
  const [password, setPassword] = useState('');
  const [expiryHours, setExpiryHours] = useState(24);
  const [downloadLimit, setDownloadLimit] = useState(0);
  const [loading, setLoading] = useState(false);
  const [createdLink, setCreatedLink] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const payload = {
        fileId,
        expiryTime: expiryHours > 0 ? new Date(Date.now() + expiryHours * 3600 * 1000).toISOString() : null,
        downloadLimit: downloadLimit > 0 ? downloadLimit : null,
        password: password || null
      };

      const response = await axiosClient.post('/shortlinks/create', payload);
      setCreatedLink(response.data.shortUrl);
      toast.success('Secure link generated!');
    } catch (error) {
      toast.error('Failed to create link');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    if (createdLink) {
      navigator.clipboard.writeText(createdLink);
      toast.success('Link copied to clipboard!');
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-slate-50">
          <h2 className="text-lg font-bold text-slate-800 flex items-center">
            <Link size={18} className="mr-2 text-blue-500" />
            Share File
          </h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 transition-colors p-1 rounded-full hover:bg-slate-200">
            <X size={20} />
          </button>
        </div>

        <div className="p-6">
          <p className="text-sm text-slate-500 mb-6 truncate" title={fileName}>
            Creating secure link for: <strong className="text-slate-700">{fileName}</strong>
          </p>

          {!createdLink ? (
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="flex items-center text-sm font-medium text-slate-700 mb-1">
                  <Clock size={14} className="mr-1 text-slate-400" /> Expires In (Hours)
                </label>
                <input
                  type="number"
                  min="0"
                  className="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all text-sm"
                  value={expiryHours}
                  onChange={(e) => setExpiryHours(parseInt(e.target.value))}
                />
                <p className="text-xs text-slate-400 mt-1">Set to 0 for no expiration</p>
              </div>

              <div>
                <label className="flex items-center text-sm font-medium text-slate-700 mb-1">
                  <DownloadLimitIcon /> Download Limit
                </label>
                <input
                  type="number"
                  min="0"
                  className="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all text-sm"
                  value={downloadLimit}
                  onChange={(e) => setDownloadLimit(parseInt(e.target.value))}
                />
                <p className="text-xs text-slate-400 mt-1">Set to 0 for unlimited</p>
              </div>

              <div>
                <label className="flex items-center text-sm font-medium text-slate-700 mb-1">
                  <Shield size={14} className="mr-1 text-slate-400" /> Password (Optional)
                </label>
                <input
                  type="password"
                  className="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all text-sm"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Leave empty for public link"
                />
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 text-white font-medium py-2.5 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                  {loading ? 'Generating...' : 'Generate Link'}
                </button>
              </div>
            </form>
          ) : (
            <div className="space-y-6">
              <div className="bg-emerald-50 border border-emerald-100 rounded-xl p-4 text-center">
                <p className="text-sm font-medium text-emerald-800 mb-3">Your secure link is ready!</p>
                <div className="flex items-center bg-white border border-emerald-200 rounded-lg p-1">
                  <input 
                    type="text" 
                    readOnly 
                    value={createdLink} 
                    className="flex-1 bg-transparent px-3 text-sm text-slate-600 outline-none truncate"
                  />
                  <button 
                    onClick={copyToClipboard}
                    className="bg-emerald-500 hover:bg-emerald-600 text-white p-2 rounded-md transition-colors"
                  >
                    <Copy size={16} />
                  </button>
                </div>
              </div>
              
              <button
                onClick={onClose}
                className="w-full bg-slate-100 text-slate-700 font-medium py-2.5 rounded-lg hover:bg-slate-200 transition-colors"
              >
                Close
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function DownloadLimitIcon() {
  return (
    <svg className="w-3.5 h-3.5 mr-1 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
    </svg>
  );
}
