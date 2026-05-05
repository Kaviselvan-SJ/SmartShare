import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import Loader from '../components/ui/Loader';
import TagBadge from '../components/ui/TagBadge';
import ShortLinkModal from '../components/ShortLinkModal';
import { toast } from 'react-hot-toast';
import { FileText, ArrowLeft, Download, Eye, EyeOff, Activity, Link as LinkIcon, HardDrive, Clock, Search, Trash2, Copy } from 'lucide-react';

export default function FileDetails() {
  const { fileId } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showPasswords, setShowPasswords] = useState({});
  const [linkToDelete, setLinkToDelete] = useState(null);
  const [deletingLink, setDeletingLink] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [showDeleteFileModal, setShowDeleteFileModal] = useState(false);
  const [deletingFile, setDeletingFile] = useState(false);
  const [previewData, setPreviewData] = useState(null);
  useEffect(() => {
    fetchFileDetails();
  }, [fileId]);

  const fetchFileDetails = async () => {
    try {
      const response = await axiosClient.get(`/files/${fileId}/details`);
      setDetails(response.data);
    } catch (error) {
      if (!error.handled) toast.error('Failed to load file details');
      navigate('/files');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyLink = (shortCode) => {
    const url = `${window.location.origin}/f/${shortCode}`;
    navigator.clipboard.writeText(url);
    toast.success('Link copied to clipboard!');
  };

  const handleDeleteLink = async () => {
    if (!linkToDelete) return;
    try {
      setDeletingLink(true);
      const response = await axiosClient.delete(`/shortlinks/${linkToDelete}`);
      toast.success(response.data.message || 'Link deleted successfully');
      setLinkToDelete(null);
      fetchFileDetails(); // Refresh details
    } catch (error) {
      if (!error.handled) toast.error(error.response?.data?.message || 'Failed to delete link');
    } finally {
      setDeletingLink(false);
    }
  };

  const togglePassword = (shortCode) => {
    setShowPasswords(prev => ({ ...prev, [shortCode]: !prev[shortCode] }));
  };

  const handleDownloadFile = async () => {
    try {
      const toastId = toast.loading('Downloading file...');
      const response = await axiosClient.get(`/files/${fileId}/preview`, {
        responseType: 'blob'
      });
      
      const url = URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = details?.fileName || 'download';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      setTimeout(() => URL.revokeObjectURL(url), 1000);
      toast.success('Download complete!', { id: toastId });
    } catch (error) {
      toast.dismiss();
      if (!error.handled) toast.error('Failed to download file');
    }
  };

  const handlePreviewFile = async () => {
    try {
      const toastId = toast.loading('Loading preview...');
      const response = await axiosClient.get(`/files/${fileId}/preview`, {
        responseType: 'blob'
      });
      
      const contentType = response.headers['content-type'];
      const blob = new Blob([response.data], { type: contentType });
      const url = URL.createObjectURL(blob);
      
      toast.dismiss(toastId);
      
      // Always open the modal, let the modal decide how to render the file type
      setPreviewData({ url, type: contentType, name: details?.fileName });
      
    } catch (error) {
      toast.dismiss();
      if (!error.handled) toast.error('Failed to load preview or unsupported file type');
    }
  };

  const closePreview = () => {
    if (previewData?.url) {
      URL.revokeObjectURL(previewData.url);
    }
    setPreviewData(null);
  };

  const handleDeleteFile = async () => {
    try {
      setDeletingFile(true);
      const response = await axiosClient.delete(`/files/${fileId}`);
      toast.success(response.data.message || 'File successfully deleted');
      navigate('/files');
    } catch (error) {
      if (!error.handled) toast.error(error.response?.data?.error || 'Failed to delete file');
    } finally {
      setDeletingFile(false);
      setShowDeleteFileModal(false);
    }
  };

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Never';
    return new Date(dateString).toLocaleString();
  };

  if (loading) return <Loader text="Loading file details..." />;
  if (!details) return null;

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center space-x-4">
          <button onClick={() => navigate('/files')} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
            <ArrowLeft size={20} />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
              <FileText className="text-indigo-500" /> {details.fileName}
            </h1>
            <p className="text-slate-500 font-mono text-sm mt-1">{details.fileHash}</p>
          </div>
        </div>
        <div className="flex items-center space-x-2">
          <button
            onClick={handlePreviewFile}
            className="inline-flex items-center gap-2 px-4 py-2 bg-purple-50 text-purple-600 hover:bg-purple-100 rounded-lg transition-colors font-medium text-sm border border-purple-200"
            title="Preview File"
          >
            <Eye size={16} /> Preview
          </button>
          <button
            onClick={handleDownloadFile}
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors font-medium text-sm border border-blue-200"
            title="Download File"
          >
            <Download size={16} /> Download
          </button>
          <button
            onClick={() => setShowShareModal(true)}
            className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-600 hover:bg-emerald-100 rounded-lg transition-colors font-medium text-sm border border-emerald-200"
            title="Create Shareable Link"
          >
            <LinkIcon size={16} /> Create Link
          </button>
          <button
            onClick={() => setShowDeleteFileModal(true)}
            className="inline-flex items-center gap-2 px-4 py-2 bg-red-50 text-red-600 hover:bg-red-100 rounded-lg transition-colors font-medium text-sm border border-red-200"
            title="Delete File"
          >
            <Trash2 size={16} /> Delete
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Overview Stats */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex items-center space-x-4">
          <div className="bg-blue-50 p-3 rounded-lg text-blue-600"><Download size={24} /></div>
          <div>
            <p className="text-sm text-gray-500 font-medium">Total Downloads</p>
            <p className="text-2xl font-bold text-gray-900">{details.totalDownloads}</p>
          </div>
        </div>
        
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex items-center space-x-4">
          <div className="bg-emerald-50 p-3 rounded-lg text-emerald-600"><HardDrive size={24} /></div>
          <div>
            <p className="text-sm text-gray-500 font-medium">Bandwidth Saved</p>
            <p className="text-2xl font-bold text-gray-900">{formatSize(details.totalBandwidthSaved)}</p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex items-center space-x-4">
          <div className="bg-purple-50 p-3 rounded-lg text-purple-600"><LinkIcon size={24} /></div>
          <div>
            <p className="text-sm text-gray-500 font-medium">Active Links</p>
            <p className="text-2xl font-bold text-gray-900">{details.activeLinkCount}</p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex items-center space-x-4">
          <div className="bg-orange-50 p-3 rounded-lg text-orange-600"><Clock size={24} /></div>
          <div>
            <p className="text-sm text-gray-500 font-medium">Last Accessed</p>
            <p className="text-sm font-bold text-gray-900 truncate" title={formatDate(details.lastAccessedAt)}>{formatDate(details.lastAccessedAt)}</p>
          </div>
        </div>
      </div>

      {/* Main Content - Links */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
          <h3 className="font-bold text-gray-800 flex items-center gap-2"><LinkIcon size={18} className="text-gray-500"/> Generated Short Links</h3>
        </div>
        {details.links && details.links.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 text-gray-600">
                <tr>
                  <th className="px-6 py-3 font-medium">Short Code</th>
                  <th className="px-6 py-3 font-medium">Status</th>
                  <th className="px-6 py-3 font-medium">Downloads</th>
                  <th className="px-6 py-3 font-medium">Password</th>
                  <th className="px-6 py-3 font-medium">Expires</th>
                  <th className="px-6 py-3 font-medium">Created</th>
                  <th className="px-6 py-3 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {details.links.map(link => (
                  <tr key={link.shortCode} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 font-mono text-indigo-600 font-medium">
                      {link.shortCode}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                        link.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' :
                        link.status === 'EXPIRED' ? 'bg-red-100 text-red-700' :
                        'bg-orange-100 text-orange-700'
                      }`}>
                        {link.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-gray-600">
                      {link.downloadCount} {link.downloadLimit ? `/ ${link.downloadLimit}` : ''}
                    </td>
                    <td className="px-6 py-4">
                      {link.passwordProtected ? (
                        <div className="flex items-center gap-2 text-gray-600">
                          <span className="font-mono bg-gray-100 px-2 py-1 rounded">
                            {showPasswords[link.shortCode] ? link.password : '••••••••'}
                          </span>
                          <button onClick={() => togglePassword(link.shortCode)} className="text-gray-400 hover:text-indigo-600">
                            {showPasswords[link.shortCode] ? <EyeOff size={16} /> : <Eye size={16} />}
                          </button>
                        </div>
                      ) : (
                        <span className="text-gray-400 italic">None</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-gray-500 whitespace-nowrap">
                      {formatDate(link.expiryTime)}
                    </td>
                    <td className="px-6 py-4 text-gray-500 whitespace-nowrap">
                      {formatDate(link.createdAt)}
                    </td>
                    <td className="px-6 py-4 text-right flex items-center justify-end space-x-2">
                      <button 
                        onClick={() => handleCopyLink(link.shortCode)} 
                        className="text-gray-400 hover:text-indigo-600 transition-colors p-1 flex items-center gap-1"
                        title="Copy Link"
                      >
                        <Copy size={16} />
                      </button>
                      <button 
                        onClick={() => setLinkToDelete(link.shortCode)} 
                        className="text-gray-400 hover:text-red-600 transition-colors p-1"
                        title="Delete Short Link"
                      >
                        <Trash2 size={18} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-8 text-center text-gray-500">No short links generated for this file yet.</div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
            <h3 className="font-bold text-gray-800 flex items-center gap-2"><Activity size={18} className="text-gray-500"/> Recent Download Activity</h3>
          </div>
          {details.recentActivity && details.recentActivity.length > 0 ? (
            <div className="divide-y divide-gray-100">
              {details.recentActivity.map((activity, index) => (
                <div key={index} className="px-6 py-4 flex items-center justify-between hover:bg-gray-50">
                  <div>
                    <p className="text-sm font-medium text-gray-800">Downloaded via <span className="font-mono text-indigo-600">{activity.shortCode}</span></p>
                    <p className="text-xs text-gray-500 mt-1">{activity.deviceType || 'Unknown Device'} • {activity.browser || 'Unknown Browser'}</p>
                  </div>
                  <span className="text-xs text-gray-400">{formatDate(activity.timestamp)}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center text-gray-500">No recent download activity.</div>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 className="font-bold text-gray-800 mb-4 flex items-center gap-2"><Search size={18} className="text-gray-500"/> Metadata & Tags</h3>
          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">Compression</p>
              <div className="flex justify-between items-center bg-gray-50 p-2 rounded">
                <span className="text-sm text-gray-600">Original Size</span>
                <span className="text-sm font-medium">{formatSize(details.originalSize)}</span>
              </div>
              <div className="flex justify-between items-center bg-gray-50 p-2 rounded mt-1">
                <span className="text-sm text-gray-600">Compressed Size</span>
                <span className="text-sm font-medium text-emerald-600">{formatSize(details.compressedSize)}</span>
              </div>
              <div className="flex justify-between items-center bg-gray-50 p-2 rounded mt-1">
                <span className="text-sm text-gray-600">Ratio</span>
                <span className="text-sm font-medium">{(details.compressionRatio * 100).toFixed(1)}% saved</span>
              </div>
            </div>
            
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-2">Tags</p>
              <div className="flex flex-wrap gap-2">
                {details.tags && details.tags.length > 0 ? (
                  details.tags.map(tag => <TagBadge key={tag} tag={tag} />)
                ) : (
                  <span className="text-sm text-gray-400 italic">No tags assigned</span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {linkToDelete && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-in zoom-in-95 duration-200">
            <h3 className="text-xl font-bold text-gray-900 mb-2">Delete Short Link</h3>
            <p className="text-gray-600 mb-6">
              Are you sure you want to delete the short link <span className="font-mono text-indigo-600 font-semibold">{linkToDelete}</span>? 
              <br /><br />
              <span className="text-red-600 font-medium text-sm">Warning: Deleting this link will also permanently delete all its download analytics.</span>
            </p>
            <div className="flex justify-end space-x-3">
              <button 
                onClick={() => setLinkToDelete(null)}
                className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors font-medium"
                disabled={deletingLink}
              >
                Cancel
              </button>
              <button 
                onClick={handleDeleteLink}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium flex items-center"
                disabled={deletingLink}
              >
                {deletingLink ? 'Deleting...' : 'Delete Link'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showDeleteFileModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-in zoom-in-95 duration-200">
            <h3 className="text-xl font-bold text-gray-900 mb-2">Delete File</h3>
            <p className="text-gray-600 mb-6">
              Are you sure you want to delete <span className="font-semibold">{details.fileName}</span>?
              This action cannot be undone. All associated short links and analytics will be permanently removed.
            </p>
            <div className="flex justify-end space-x-3">
              <button 
                onClick={() => setShowDeleteFileModal(false)}
                className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors font-medium"
                disabled={deletingFile}
              >
                Cancel
              </button>
              <button 
                onClick={handleDeleteFile}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium flex items-center"
                disabled={deletingFile}
              >
                {deletingFile ? 'Deleting...' : 'Delete Permanently'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showShareModal && (
        <ShortLinkModal 
          fileId={fileId}
          fileName={details.fileName}
          onClose={() => setShowShareModal(false)}
        />
      )}

      {/* File Preview Modal */}
      {previewData && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex flex-col z-50 animate-in fade-in duration-200">
          <div className="flex justify-between items-center p-4 bg-black/50 text-white">
            <h3 className="font-medium text-lg truncate flex items-center gap-2">
              <Eye size={20} className="text-purple-400" />
              {previewData.name}
            </h3>
            <button 
              onClick={closePreview}
              className="p-2 hover:bg-white/10 rounded-full transition-colors text-gray-300 hover:text-white"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
          </div>
          <div className="flex-1 w-full h-full p-4 flex justify-center items-center overflow-hidden">
            {previewData.type.startsWith('image/') ? (
              <img 
                src={previewData.url} 
                alt={previewData.name}
                className="max-w-full max-h-full object-contain rounded-lg shadow-2xl"
              />
            ) : previewData.type === 'application/octet-stream' ? (
              <div className="bg-white rounded-xl p-8 flex flex-col items-center max-w-md w-full shadow-2xl">
                <FileText size={48} className="text-gray-400 mb-4" />
                <h4 className="text-lg font-semibold text-gray-800 mb-2">No preview available</h4>
                <p className="text-gray-500 text-center mb-6">This file type cannot be previewed in the browser. Please download it to view the contents.</p>
                <button 
                  onClick={() => {
                    const link = document.createElement('a');
                    link.href = previewData.url;
                    link.download = previewData.name || 'download';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                  }}
                  className="px-6 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
                >
                  <Download size={18} /> Download File
                </button>
              </div>
            ) : (
              <iframe 
                src={previewData.url} 
                title={previewData.name}
                className="w-full max-w-5xl h-full bg-white rounded-lg shadow-2xl border-0"
              />
            )}
          </div>
        </div>
      )}
    </div>
  );
}
