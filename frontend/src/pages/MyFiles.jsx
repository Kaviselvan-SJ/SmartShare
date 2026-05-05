import React, { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import FileCard from '../components/ui/FileCard';
import ShortLinkModal from '../components/ShortLinkModal';
import Loader from '../components/ui/Loader';
import toast from 'react-hot-toast';

export default function MyFiles() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedFileForLink, setSelectedFileForLink] = useState(null);
  const [fileToDelete, setFileToDelete] = useState(null);

  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    try {
      const response = await axiosClient.get('/files/my-files');
      setFiles(response.data);
    } catch (error) {
      if (!error.handled) toast.error('Failed to load files');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (file) => {
    try {
      const toastId = toast.loading('Downloading file...');
      const response = await axiosClient.get(`/files/${file.fileId}/preview`, {
        responseType: 'blob'
      });
      
      const url = URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = file.fileName || 'download';
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

  const handleDeleteConfirm = async () => {
    if (!fileToDelete) return;
    
    try {
      setLoading(true);
      const response = await axiosClient.delete(`/files/${fileToDelete.fileId}`);
      toast.success(response.data.message || 'File successfully deleted');
      setFileToDelete(null);
      fetchFiles(); // Refresh the list
    } catch (error) {
      if (!error.handled) toast.error(error.response?.data?.error || 'Failed to delete file');
      setLoading(false);
    }
  };

  if (loading && files.length === 0) return <Loader text="Loading your files..." />;

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">My Files</h1>
          <p className="text-slate-500 mt-1">Manage your uploaded files and create shareable links.</p>
        </div>
      </div>

      {files.length === 0 ? (
        <div className="bg-white p-12 rounded-2xl border border-dashed border-gray-300 text-center shadow-sm">
          <p className="text-lg text-slate-600 mb-2">No files uploaded yet.</p>
          <button 
            onClick={() => window.location.href='/upload'}
            className="text-blue-600 hover:underline font-medium"
          >
            Upload your first file
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {files.map(file => (
            <FileCard 
              key={file.fileId} 
              file={file} 
              onDownload={handleDownload}
              onShare={(f) => setSelectedFileForLink(f)}
              onDelete={(f) => setFileToDelete(f)}
            />
          ))}
        </div>
      )}

      {selectedFileForLink && (
        <ShortLinkModal 
          fileId={selectedFileForLink.fileId}
          fileName={selectedFileForLink.fileName}
          onClose={() => setSelectedFileForLink(null)}
        />
      )}

      {fileToDelete && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-in zoom-in-95 duration-200">
            <h3 className="text-xl font-bold text-gray-900 mb-2">Delete File</h3>
            <p className="text-gray-600 mb-6">
              Are you sure you want to delete <span className="font-semibold">{fileToDelete.fileName}</span>? 
              This action cannot be undone. All associated short links and analytics will be permanently removed.
            </p>
            <div className="flex justify-end space-x-3">
              <button 
                onClick={() => setFileToDelete(null)}
                className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors font-medium"
                disabled={loading}
              >
                Cancel
              </button>
              <button 
                onClick={handleDeleteConfirm}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium flex items-center"
                disabled={loading}
              >
                {loading ? 'Deleting...' : 'Delete Permanently'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
