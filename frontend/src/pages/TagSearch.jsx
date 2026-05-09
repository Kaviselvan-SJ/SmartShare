import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import FileCard from '../components/ui/FileCard';
import Loader from '../components/ui/Loader';
import { Search, FileText, Tag } from 'lucide-react';
import toast from 'react-hot-toast';
import ShortLinkModal from '../components/ShortLinkModal';

export default function TagSearch() {
  const [searchParams, setSearchParams] = useSearchParams();
  // Support both ?q= (unified) and legacy ?tags= param
  const initialQuery = searchParams.get('q') || searchParams.get('tags') || '';

  const [query, setQuery] = useState(initialQuery);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [selectedFileForLink, setSelectedFileForLink] = useState(null);
  const [fileToDelete, setFileToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (initialQuery) {
      performSearch(initialQuery);
    }
  }, [initialQuery]);

  const performSearch = async (searchQuery) => {
    if (!searchQuery.trim()) return;

    setLoading(true);
    setHasSearched(true);

    try {
      const response = await axiosClient.get(
        `/tags/search/unified?q=${encodeURIComponent(searchQuery.trim())}`
      );
      setResults(response.data);
    } catch (error) {
      toast.error('Search failed');
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmed = query.trim();
    if (trimmed) {
      setSearchParams({ q: trimmed });
      performSearch(trimmed);
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
      setDeleting(true);
      const response = await axiosClient.delete(`/files/${fileToDelete.fileId}`);
      toast.success(response.data.message || 'File successfully deleted');
      setFileToDelete(null);
      setResults(prev => prev.filter(f => f.fileId !== fileToDelete.fileId));
    } catch (error) {
      if (!error.handled) toast.error(error.response?.data?.error || 'Failed to delete file');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Search Files</h1>
        <p className="text-sm sm:text-base text-slate-500 mt-1">
          Search by <span className="font-medium text-blue-600">file name</span> or{' '}
          <span className="font-medium text-purple-600">tag</span> — results match either.
        </p>
      </div>

      {/* Search bar */}
      <div className="bg-white p-4 sm:p-6 rounded-2xl shadow-sm border border-gray-100">
        <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 sm:gap-4">
          <div className="flex-1 relative w-full">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              id="search-input"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="e.g. report.pdf  or  invoice  or  image..."
              className="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all text-slate-700"
              autoFocus
            />
          </div>
          <button
            type="submit"
            id="search-submit"
            disabled={loading || !query.trim()}
            className="w-full sm:w-auto px-6 py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 min-h-[48px]"
          >
            Search
          </button>
        </form>

        {/* Legend */}
        <div className="flex flex-wrap items-center gap-2 sm:gap-4 mt-4 sm:ml-1">
          <span className="flex items-center gap-1.5 text-xs text-slate-500">
            <FileText size={13} className="text-blue-500" />
            Matches file name
          </span>
          <span className="flex items-center gap-1.5 text-xs text-slate-500">
            <Tag size={13} className="text-purple-500" />
            Matches tag
          </span>
        </div>
      </div>

      {loading && <Loader text="Searching files..." />}

      {!loading && hasSearched && results.length === 0 && (
        <div className="bg-white p-12 rounded-2xl border border-dashed border-gray-300 text-center shadow-sm">
          <Search size={40} className="mx-auto text-gray-300 mb-3" />
          <p className="text-lg text-slate-600 font-medium">No files found</p>
          <p className="text-sm text-slate-400 mt-1">
            No file name or tag matched <span className="font-semibold">"{query}"</span>
          </p>
        </div>
      )}

      {!loading && results.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
            Found {results.length} Result{results.length > 1 ? 's' : ''} for &ldquo;{initialQuery}&rdquo;
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {results.map(file => (
              <FileCard
                key={file.fileId}
                file={file}
                onDownload={handleDownload}
                onShare={(f) => setSelectedFileForLink(f)}
                onDelete={(f) => setFileToDelete(f)}
              />
            ))}
          </div>
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
              Are you sure you want to delete{' '}
              <span className="font-semibold">{fileToDelete.fileName}</span>? This action
              cannot be undone. All associated short links and analytics will be permanently
              removed.
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setFileToDelete(null)}
                className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors font-medium"
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteConfirm}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium flex items-center"
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Delete Permanently'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
