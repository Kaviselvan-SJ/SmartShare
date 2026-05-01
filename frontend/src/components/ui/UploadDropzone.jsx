import React, { useCallback, useState } from 'react';
import { UploadCloud, File as FileIcon, X } from 'lucide-react';

export default function UploadDropzone({ onFileSelect, selectedFile, onClear }) {
  const [isDragActive, setIsDragActive] = useState(false);

  const handleDrag = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setIsDragActive(true);
    } else if (e.type === 'dragleave') {
      setIsDragActive(false);
    }
  }, []);

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      onFileSelect(e.dataTransfer.files[0]);
    }
  }, [onFileSelect]);

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      onFileSelect(e.target.files[0]);
    }
  };

  if (selectedFile) {
    return (
      <div className="bg-blue-50 border-2 border-blue-200 border-dashed rounded-2xl p-8 text-center transition-all">
        <div className="flex flex-col items-center justify-center space-y-4">
          <div className="bg-white p-4 rounded-full shadow-sm">
            <FileIcon size={32} className="text-blue-500" />
          </div>
          <div>
            <p className="text-lg font-semibold text-slate-800">{selectedFile.name}</p>
            <p className="text-sm text-slate-500">{(selectedFile.size / 1024 / 1024).toFixed(2)} MB</p>
          </div>
          <button
            type="button"
            onClick={onClear}
            className="flex items-center space-x-2 text-sm text-red-500 hover:text-red-700 bg-white px-4 py-2 rounded-full shadow-sm hover:shadow transition-all"
          >
            <X size={16} />
            <span>Remove File</span>
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      className={`border-2 border-dashed rounded-2xl p-12 text-center cursor-pointer transition-all ${
        isDragActive 
          ? 'border-blue-500 bg-blue-50 scale-[1.02]' 
          : 'border-slate-300 bg-slate-50 hover:bg-slate-100'
      }`}
      onDragEnter={handleDrag}
      onDragLeave={handleDrag}
      onDragOver={handleDrag}
      onDrop={handleDrop}
      onClick={() => document.getElementById('file-upload').click()}
    >
      <input
        id="file-upload"
        type="file"
        className="hidden"
        onChange={handleChange}
      />
      <div className="flex flex-col items-center justify-center space-y-4 pointer-events-none">
        <div className={`p-4 rounded-full ${isDragActive ? 'bg-blue-100 text-blue-600' : 'bg-white text-slate-400 shadow-sm'}`}>
          <UploadCloud size={40} />
        </div>
        <div>
          <p className="text-lg font-semibold text-slate-700">
            {isDragActive ? "Drop the file here" : "Drag & drop your file here"}
          </p>
          <p className="text-sm text-slate-500 mt-1">or click to browse from your computer</p>
        </div>
      </div>
    </div>
  );
}
