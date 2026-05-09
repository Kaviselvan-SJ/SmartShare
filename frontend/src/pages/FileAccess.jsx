import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Download, Lock, AlertCircle, FileBox, Clock, ShieldOff } from 'lucide-react';
import toast, { Toaster } from 'react-hot-toast';

const BACKEND_BASE = import.meta.env.VITE_API_BASE_URL
  ? import.meta.env.VITE_API_BASE_URL.replace('/api', '')
  : 'http://localhost:8080';

// States the page can be in
const STATE = {
  IDLE: 'idle',           // waiting for user to click
  LOADING: 'loading',
  NEEDS_PASSWORD: 'needs_password',
  LIMIT_REACHED: 'limit_reached',
  EXPIRED: 'expired',
  NOT_FOUND: 'not_found',
  SUCCESS: 'success',
};

export default function FileAccess() {
  const { shortCode } = useParams();
  const [password, setPassword] = useState('');
  const [pageState, setPageState] = useState(STATE.IDLE);
  const [errorMsg, setErrorMsg] = useState(null);

  // Auto-attempt download on first load (works for public links)
  useEffect(() => {
    attemptDownload('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shortCode]);

  const attemptDownload = async (pwd) => {
    setPageState(STATE.LOADING);
    setErrorMsg(null);

    try {
      const headers = pwd ? { 'X-Download-Password': pwd } : {};

      const response = await axios.get(`${BACKEND_BASE}/f/${shortCode}`, {
        responseType: 'blob',
        headers,
      });

      // Extract filename from Content-Disposition header
      const contentDisposition = response.headers['content-disposition'];
      let fileName = 'download';
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="?([^"]+)"?/);
        if (match) fileName = match[1];
      }

      const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = blobUrl;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(blobUrl);

      setPageState(STATE.SUCCESS);
      toast.success('Download started!');
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        // Password required or wrong — show password form
        setPageState(STATE.NEEDS_PASSWORD);
        if (pwd) {
          // User already tried a password — it was wrong
          setErrorMsg('Incorrect password. Please try again.');
          toast.error('Incorrect password');
        }
      } else if (status === 404) {
        // Parse the blob error body to get the actual message
        const text = await blobToText(err.response?.data);
        const msg = parseErrorText(text);

        if (msg?.toLowerCase().includes('limit')) {
          setPageState(STATE.LIMIT_REACHED);
        } else if (msg?.toLowerCase().includes('expir')) {
          setPageState(STATE.EXPIRED);
        } else {
          setPageState(STATE.NOT_FOUND);
        }
      } else {
        setPageState(STATE.IDLE);
        setErrorMsg('Something went wrong. Please try again later.');
        toast.error('Download failed');
      }
    }
  };

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    if (!password.trim()) {
      setErrorMsg('Please enter the password.');
      return;
    }
    attemptDownload(password.trim());
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <Toaster position="top-right" />
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden text-center">

        {/* ── Header ── */}
        <div className="p-8 pb-6">
          <div className="mx-auto w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mb-6">
            <FileBox size={32} />
          </div>
          <h1 className="text-2xl font-bold text-slate-800 mb-2">Secure File Download</h1>
          <p className="text-slate-500 text-sm">You've been invited to download a file securely via SmartShare.</p>
        </div>

        {/* ── States ── */}

        {/* LOADING */}
        {pageState === STATE.LOADING && (
          <div className="px-8 pb-8">
            <div className="flex items-center justify-center space-x-3 text-blue-600 py-6">
              <svg className="animate-spin h-6 w-6" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              <span className="font-medium">Preparing your download…</span>
            </div>
          </div>
        )}

        {/* SUCCESS */}
        {pageState === STATE.SUCCESS && (
          <div className="px-8 pb-8">
            <div className="bg-emerald-50 border border-emerald-100 rounded-xl p-5 text-center">
              <p className="text-emerald-700 font-semibold text-lg mb-1">✓ Download started!</p>
              <p className="text-emerald-600 text-sm">Check your downloads folder.</p>
            </div>
            <button
              onClick={() => attemptDownload(password)}
              className="mt-4 w-full bg-slate-100 text-slate-700 py-2.5 rounded-xl hover:bg-slate-200 transition-colors text-sm font-medium"
            >
              Download Again
            </button>
          </div>
        )}

        {/* NEEDS PASSWORD */}
        {pageState === STATE.NEEDS_PASSWORD && (
          <div className="px-8 pb-8">
            <div className="bg-amber-50 border border-amber-100 rounded-xl p-4 flex items-center space-x-3 text-left mb-6">
              <Lock size={20} className="text-amber-500 shrink-0" />
              <p className="text-amber-700 text-sm font-medium">This link is password protected.</p>
            </div>

            {errorMsg && (
              <div className="bg-red-50 text-red-600 p-3 rounded-xl flex items-center space-x-2 text-sm mb-4">
                <AlertCircle size={16} className="shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handlePasswordSubmit} className="space-y-4 text-left">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                  <input
                    type="password"
                    autoFocus
                    className="w-full pl-9 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    placeholder="Enter link password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
              </div>
              <button
                type="submit"
                className="w-full bg-blue-600 text-white font-semibold py-3 rounded-xl hover:bg-blue-700 transition-all flex items-center justify-center space-x-2"
              >
                <Download size={18} />
                <span>Download File</span>
              </button>
            </form>
          </div>
        )}

        {/* LIMIT REACHED */}
        {pageState === STATE.LIMIT_REACHED && (
          <div className="px-8 pb-8">
            <div className="bg-orange-50 border border-orange-200 rounded-xl p-6 text-center">
              <ShieldOff size={36} className="text-orange-400 mx-auto mb-3" />
              <p className="text-orange-700 font-bold text-lg mb-1">Download Limit Reached</p>
              <p className="text-orange-600 text-sm">This link has reached its maximum number of downloads and is no longer available.</p>
            </div>
          </div>
        )}

        {/* EXPIRED */}
        {pageState === STATE.EXPIRED && (
          <div className="px-8 pb-8">
            <div className="bg-slate-50 border border-slate-200 rounded-xl p-6 text-center">
              <Clock size={36} className="text-slate-400 mx-auto mb-3" />
              <p className="text-slate-700 font-bold text-lg mb-1">Link Expired</p>
              <p className="text-slate-500 text-sm">This download link has expired. Please ask the file owner to generate a new one.</p>
            </div>
          </div>
        )}

        {/* NOT FOUND */}
        {pageState === STATE.NOT_FOUND && (
          <div className="px-8 pb-8">
            <div className="bg-red-50 border border-red-100 rounded-xl p-6 text-center">
              <AlertCircle size={36} className="text-red-400 mx-auto mb-3" />
              <p className="text-red-700 font-bold text-lg mb-1">Link Not Found</p>
              <p className="text-red-500 text-sm">This link is invalid or has been deleted.</p>
            </div>
          </div>
        )}

        {/* IDLE (initial state — shouldn't show long) */}
        {pageState === STATE.IDLE && (
          <div className="px-8 pb-8">
            {errorMsg && (
              <div className="bg-red-50 text-red-600 p-3 rounded-xl flex items-center space-x-2 text-sm mb-4">
                <AlertCircle size={16} className="shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}
            <button
              onClick={() => attemptDownload('')}
              className="w-full bg-blue-600 text-white font-semibold py-3.5 rounded-xl hover:bg-blue-700 transition-all flex items-center justify-center space-x-2 shadow-sm"
            >
              <Download size={20} />
              <span>Download File</span>
            </button>
          </div>
        )}

        <p className="pb-6 text-xs text-slate-400">Powered by <strong>SmartShare</strong></p>
      </div>
    </div>
  );
}

// ── Helpers ────────────────────────────────────────────────────────────────
async function blobToText(blob) {
  if (!blob) return '';
  try {
    return await blob.text();
  } catch {
    return '';
  }
}

function parseErrorText(text) {
  try {
    const json = JSON.parse(text);
    return json.error || json.message || '';
  } catch {
    return text || '';
  }
}
