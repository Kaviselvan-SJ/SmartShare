import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Download, Lock, AlertCircle, FileBox } from 'lucide-react';
import toast, { Toaster } from 'react-hot-toast';

export default function FileAccess() {
  const { shortCode } = useParams();
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleDownload = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const baseURL = import.meta.env.VITE_API_BASE_URL ? import.meta.env.VITE_API_BASE_URL.replace('/api', '') : 'http://localhost:8080';
      const url = `${baseURL}/f/${shortCode}`;

      const response = await axios.get(url, { 
        responseType: 'blob',
        headers: password ? { 'X-Download-Password': password } : {}
      });
      
      const contentDisposition = response.headers['content-disposition'];
      let fileName = 'downloaded_file';
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
        if (fileNameMatch && fileNameMatch.length === 2) {
          fileName = fileNameMatch[1];
        }
      }

      const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = blobUrl;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(blobUrl);

      toast.success('Download started!');
    } catch (err) {
      if (err.response && err.response.status === 401) {
        setError('Password required or incorrect password.');
      } else if (err.response && err.response.status === 404) {
        setError('Link expired, invalid, or download limit reached.');
      } else if (err.response && err.response.status === 403) {
        setError('Access denied.');
      } else {
        setError('Failed to download file. Please try again later.');
      }
      toast.error('Download failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <Toaster position="top-right" />
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden text-center p-8">
        <div className="mx-auto w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mb-6">
          <FileBox size={32} />
        </div>
        
        <h1 className="text-2xl font-bold text-slate-800 mb-2">Secure File Download</h1>
        <p className="text-slate-500 text-sm mb-8">
          You've been invited to download a file securely via SmartShare.
        </p>

        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-xl flex items-start space-x-3 text-left mb-6 text-sm">
            <AlertCircle size={20} className="shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleDownload} className="space-y-4 text-left">
          {error?.includes('Password') && (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Password Required
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input
                  type="password"
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter link password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white font-semibold py-3.5 rounded-xl hover:bg-blue-700 transition-all disabled:opacity-50 flex items-center justify-center space-x-2 shadow-sm hover:shadow"
          >
            <Download size={20} />
            <span>{loading ? 'Downloading...' : 'Download File'}</span>
          </button>
        </form>
        
        <p className="mt-8 text-xs text-slate-400">
          Powered by <strong>SmartShare</strong>
        </p>
      </div>
    </div>
  );
}
