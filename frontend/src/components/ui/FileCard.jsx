import React from 'react';
import TagBadge from './TagBadge';
import { Download, Link, FileText, Image, Video, Music, Archive, File, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function FileCard({ file, onDownload, onShare, onDelete }) {
  const navigate = useNavigate();

  const getFileIcon = (fileName) => {
    const ext = fileName.split('.').pop().toLowerCase();
    switch (ext) {
      case 'pdf': case 'txt': case 'doc': case 'docx': return <FileText size={24} className="text-blue-500" />;
      case 'png': case 'jpg': case 'jpeg': case 'gif': return <Image size={24} className="text-emerald-500" />;
      case 'mp4': case 'mkv': case 'avi': return <Video size={24} className="text-purple-500" />;
      case 'mp3': case 'wav': return <Music size={24} className="text-orange-500" />;
      case 'zip': case 'rar': case 'tar': return <Archive size={24} className="text-gray-500" />;
      default: return <File size={24} className="text-indigo-500" />;
    }
  };

  const formatSize = (bytes) => {
    if (!bytes) return 'Unknown';
    if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return bytes + ' B';
  };

  const handleCardClick = (e) => {
    // Only navigate if the click wasn't on an action button
    if (e.target.closest('button')) return;
    navigate(`/files/${file.fileId}`);
  };

  return (
    <div 
      onClick={handleCardClick}
      className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-all group flex flex-col h-full cursor-pointer hover:border-indigo-200 hover:-translate-y-1"
    >
      <div className="flex items-start justify-between mb-4">
        <div className="bg-gray-50 p-3 rounded-lg">
          {getFileIcon(file.fileName)}
        </div>
        <div className="flex space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
          {onDownload && (
            <button onClick={(e) => { e.stopPropagation(); onDownload(file); }} className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Download">
              <Download size={16} />
            </button>
          )}
          {onShare && (
            <button onClick={(e) => { e.stopPropagation(); onShare(file); }} className="p-2 text-gray-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors" title="Share">
              <Link size={16} />
            </button>
          )}
          {onDelete && (
            <button onClick={(e) => { e.stopPropagation(); onDelete(file); }} className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Delete">
              <Trash2 size={16} />
            </button>
          )}
        </div>
      </div>
      
      <div className="flex-1">
        <h3 className="font-semibold text-gray-800 text-sm mb-1 truncate" title={file.fileName}>{file.fileName}</h3>
        <p className="text-xs text-gray-400 mb-3 font-mono truncate" title={file.fileHash}>
          {file.fileHash ? file.fileHash.substring(0, 16) + '...' : 'Unknown hash'}
        </p>
      </div>

      <div className="mb-4">
        {file.compressedSize && file.originalSize && file.compressedSize < file.originalSize && (
           <p className="text-xs font-medium text-emerald-600 mb-2">
             Saved {formatSize(file.originalSize - file.compressedSize)}
           </p>
        )}
        <div className="flex flex-wrap gap-1.5">
          {file.tags && file.tags.slice(0, 3).map(tag => (
            <TagBadge key={tag} tag={tag} />
          ))}
          {file.tags && file.tags.length > 3 && (
            <span className="text-xs text-gray-400 font-medium px-2 py-0.5 bg-gray-50 rounded-full border border-gray-100">+{file.tags.length - 3}</span>
          )}
        </div>
      </div>

      <div className="flex items-center justify-between text-xs text-gray-400 pt-3 border-t border-gray-100">
        <span>{file.createdAt ? new Date(file.createdAt).toLocaleDateString() : 'Today'}</span>
        <span>{formatSize(file.compressedSize || file.originalSize)}</span>
      </div>
    </div>
  );
}
