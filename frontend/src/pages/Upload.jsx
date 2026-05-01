import React, { useState } from 'react';
import axiosClient from '../api/axiosClient';
import UploadDropzone from '../components/ui/UploadDropzone';
import { CheckCircle, AlertCircle, HardDrive } from 'lucide-react';
import toast from 'react-hot-toast';
import Loader from '../components/ui/Loader';

export default function Upload() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error('Please select a file first.');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);

    setUploading(true);
    setUploadResult(null);

    try {
      const response = await axiosClient.post('/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      setUploadResult(response.data);
      if (response.data.duplicate) {
        toast('Duplicate file detected. Link created to existing file.', { icon: 'ℹ️' });
      } else {
        toast.success('File uploaded successfully!');
      }
    } catch (error) {
      console.error('Upload failed', error);
      toast.error(error.response?.data?.message || 'Failed to upload file.');
    } finally {
      setUploading(false);
    }
  };

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Upload File</h1>
        <p className="text-slate-500 mt-1">Upload securely. We will compress and deduplicate it automatically.</p>
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
        <UploadDropzone 
          onFileSelect={setSelectedFile} 
          selectedFile={selectedFile} 
          onClear={() => { setSelectedFile(null); setUploadResult(null); }}
        />

        <div className="mt-8 flex justify-end">
          <button
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
            className={`px-6 py-2.5 rounded-full font-medium transition-all shadow-sm ${
              !selectedFile || uploading
                ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md'
            }`}
          >
            {uploading ? 'Uploading...' : 'Start Upload'}
          </button>
        </div>
      </div>

      {uploading && (
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-center">
          <Loader text="Compressing and securely uploading to MinIO..." />
        </div>
      )}

      {uploadResult && !uploading && (
        <div className="bg-emerald-50 border border-emerald-100 rounded-2xl p-6 relative overflow-hidden">
          <div className="absolute top-0 right-0 p-8 opacity-10">
            <CheckCircle size={100} className="text-emerald-500" />
          </div>
          
          <div className="relative z-10 space-y-4">
            <div className="flex items-center space-x-3 text-emerald-700">
              {uploadResult.duplicate ? <AlertCircle size={24} /> : <CheckCircle size={24} />}
              <h3 className="text-lg font-semibold">
                {uploadResult.duplicate ? 'Existing File Linked' : 'Upload Complete'}
              </h3>
            </div>
            
            <div className="grid grid-cols-2 gap-4 mt-4">
              <div className="bg-white/60 p-4 rounded-xl border border-emerald-200/50">
                <p className="text-xs text-emerald-600 font-medium uppercase tracking-wider mb-1">File Hash</p>
                <p className="text-sm font-mono text-slate-700 truncate" title={uploadResult.fileHash}>
                  {uploadResult.fileHash}
                </p>
              </div>
              <div className="bg-white/60 p-4 rounded-xl border border-emerald-200/50">
                <p className="text-xs text-emerald-600 font-medium uppercase tracking-wider mb-1">Storage Saved</p>
                <div className="flex items-center space-x-2">
                  <HardDrive size={16} className="text-emerald-500" />
                  <p className="text-sm font-semibold text-slate-800">
                    {uploadResult.duplicate ? '100% (Duplicate)' : formatSize(uploadResult.originalSize - uploadResult.compressedSize)}
                  </p>
                </div>
              </div>
              <div className="col-span-2 bg-white/60 p-4 rounded-xl border border-emerald-200/50">
                <p className="text-xs text-emerald-600 font-medium uppercase tracking-wider mb-1">Compression Details</p>
                <p className="text-sm text-slate-700">
                  Compressed from <strong>{formatSize(uploadResult.originalSize)}</strong> to <strong>{formatSize(uploadResult.compressedSize)}</strong>
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
