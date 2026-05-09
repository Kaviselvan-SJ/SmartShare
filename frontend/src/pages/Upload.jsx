import React, { useState } from 'react';
import axiosClient from '../api/axiosClient';
import UploadDropzone from '../components/ui/UploadDropzone';
import { CheckCircle, AlertCircle, HardDrive, File as FileIcon, X } from 'lucide-react';
import toast from 'react-hot-toast';
import Loader from '../components/ui/Loader';

export default function Upload() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);
  
  // Conflict state
  const [conflictData, setConflictData] = useState(null);

  const performUpload = async (file, params = {}) => {
    const formData = new FormData();
    formData.append('file', file);
    
    setUploading(true);
    setUploadResult(null);
    setConflictData(null);

    try {
      let endpoint = '/files/upload';
      if (params.replace !== undefined && params.fileGroupId) {
        endpoint = `/files/${params.fileGroupId}/versions?replace=${params.replace}`;
      } else if (params.independent) {
        endpoint = '/files/upload?independent=true';
      }
      
      const response = await axiosClient.post(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      setUploadResult(response.data);
      if (response.data.duplicate) {
        toast('Duplicate file detected. Link created to existing file.', { icon: 'ℹ️' });
      } else {
        toast.success(params.replace !== undefined ? 'New version uploaded successfully!' : 'File uploaded successfully!');
      }
    } catch (error) {
      console.error('Upload failed', error);
      toast.error(error.response?.data?.message || 'Failed to upload file.');
    } finally {
      setUploading(false);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error('Please select a file first.');
      return;
    }

    setUploading(true);
    try {
      // Pre-flight check
      const checkResponse = await axiosClient.get(`/files/check-duplicate?fileName=${encodeURIComponent(selectedFile.name)}`);
      
      if (checkResponse.data.fileNameExists) {
        setConflictData(checkResponse.data);
        setUploading(false);
      } else {
        await performUpload(selectedFile);
      }
    } catch (error) {
      console.error('Pre-flight check failed', error);
      toast.error('Failed to check file status. Proceeding with upload...');
      await performUpload(selectedFile);
    }
  };

  const handlePreviewFile = async (fileGroupId) => {
    // Actually, the preview logic can be identical to FileDetails preview
    // But since the conflict data only gives us a group ID, we can fetch the current version's preview
    // We can also just use the existing preview endpoint since we don't have a direct short code
    // Assuming backend FilePreviewController takes fileId or shortCode. Wait, FilePreviewController takes `fileId`.
    // Wait, the preview in Upload Decision Modal: "clicking preview opens owner-only preview endpoint"
    // The conflict data tells us the current version ID? Let's check `existingCurrentVersion` which is the version *number*.
    // Wait, `FileGroupEntity` has `currentVersionId`. Let's assume we can fetch `/files/${conflictData.existingFileGroupId}/preview` if we build it.
    toast('Preview existing file not implemented yet in this modal.', { icon: 'ℹ️' });
  };

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  const handleConflictResolution = async (action) => {
    if (!conflictData || !selectedFile) return;

    if (action === 'replace') {
      await performUpload(selectedFile, { replace: true, fileGroupId: conflictData.existingFileGroupId });
    } else if (action === 'new_version') {
      await performUpload(selectedFile, { replace: false, fileGroupId: conflictData.existingFileGroupId });
    } else if (action === 'new_file') {
      await performUpload(selectedFile, { independent: true });
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Upload File</h1>
        <p className="text-slate-500 mt-1">Upload securely. We will compress and deduplicate it automatically.</p>
      </div>

      <div className="bg-white p-6 sm:p-8 rounded-2xl shadow-sm border border-gray-100 relative">
        <UploadDropzone 
          selectedFile={selectedFile} 
          onFileSelect={(f) => { setSelectedFile(f); setUploadResult(null); setConflictData(null); }}
          onClear={() => { setSelectedFile(null); setUploadResult(null); setConflictData(null); }}
        />

        <div className="mt-8 flex flex-col sm:flex-row justify-end">
          <button
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
            className={`w-full sm:w-auto px-6 py-2.5 rounded-full font-medium transition-all shadow-sm min-h-[44px] ${
              !selectedFile || uploading
                ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md'
            }`}
          >
            {uploading && !conflictData ? 'Uploading...' : 'Start Upload'}
          </button>
        </div>
      </div>

      {uploading && !conflictData && (
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-center">
          <Loader text="Compressing and securely uploading to MinIO..." />
        </div>
      )}

      {conflictData && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden animate-in zoom-in-95 duration-300">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-amber-50/50">
              <div className="flex items-center space-x-3 text-amber-600">
                <AlertCircle size={24} />
                <h3 className="text-xl font-bold text-gray-900">File Already Exists</h3>
              </div>
              <button onClick={() => setConflictData(null)} className="text-gray-400 hover:text-gray-600 transition-colors">
                <X size={24} />
              </button>
            </div>
            
            <div className="p-6 space-y-6">
              <p className="text-gray-600">
                You already have a file named <strong className="text-gray-900">{selectedFile?.name}</strong>. 
                What would you like to do?
              </p>
              
              <div className="flex flex-col sm:flex-row items-start sm:items-center p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-3 sm:space-y-0 sm:space-x-4">
                <div className="bg-blue-100 p-3 rounded-lg text-blue-600 shrink-0">
                  <FileIcon size={24} />
                </div>
                <div className="min-w-0 w-full">
                  <p className="font-semibold text-slate-800 truncate" title={selectedFile?.name}>{selectedFile?.name}</p>
                  <p className="text-sm text-slate-500">Current active version: <strong>v{conflictData.existingCurrentVersion}</strong></p>
                </div>
              </div>

              <div className="space-y-3 pt-2">
                <button
                  onClick={() => handleConflictResolution('replace')}
                  className="w-full text-left p-4 rounded-xl border border-blue-200 bg-blue-50 hover:bg-blue-100 transition-colors group flex justify-between items-center gap-4"
                >
                  <div className="min-w-0">
                    <h4 className="font-bold text-blue-900 truncate">Replace Existing Version</h4>
                    <p className="text-xs sm:text-sm text-blue-700/80 mt-1">Existing links will download this new file automatically.</p>
                  </div>
                  <CheckCircle className="text-blue-500 opacity-0 group-hover:opacity-100 transition-opacity shrink-0" />
                </button>

                <button
                  onClick={() => handleConflictResolution('new_version')}
                  className="w-full text-left p-4 rounded-xl border border-gray-200 hover:bg-gray-50 transition-colors group flex justify-between items-center gap-4"
                >
                  <div className="min-w-0">
                    <h4 className="font-bold text-gray-900 truncate">Add as New Version</h4>
                    <p className="text-xs sm:text-sm text-gray-500 mt-1">Keep the old version in history and set this as active.</p>
                  </div>
                  <CheckCircle className="text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity shrink-0" />
                </button>

                <button
                  onClick={() => handleConflictResolution('new_file')}
                  className="w-full text-left p-4 rounded-xl border border-gray-200 hover:bg-gray-50 transition-colors group flex justify-between items-center gap-4"
                >
                  <div className="min-w-0">
                    <h4 className="font-bold text-gray-900 truncate">Store as Independent File</h4>
                    <p className="text-xs sm:text-sm text-gray-500 mt-1">Upload separately. Does not affect existing files.</p>
                  </div>
                  <CheckCircle className="text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity shrink-0" />
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {uploadResult && !uploading && !conflictData && (
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
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-4">
              <div className="bg-white/60 p-4 rounded-xl border border-emerald-200/50 min-w-0">
                <p className="text-xs text-emerald-600 font-medium uppercase tracking-wider mb-1">File Hash / Version</p>
                <p className="text-sm font-mono text-slate-700 truncate" title={uploadResult.fileHash}>
                  {uploadResult.versionNumber ? `v${uploadResult.versionNumber} ` : ''}{uploadResult.fileHash}
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
              <div className="col-span-1 sm:col-span-2 bg-white/60 p-4 rounded-xl border border-emerald-200/50">
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
