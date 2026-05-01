import React from 'react';

export default function TagBadge({ tag, onClick }) {
  return (
    <span 
      onClick={onClick}
      className={`inline-block px-2.5 py-1 text-xs font-medium rounded-md border 
        ${onClick 
          ? 'cursor-pointer hover:bg-blue-100 hover:border-blue-200 transition-colors bg-blue-50 text-blue-700 border-blue-100' 
          : 'bg-slate-100 text-slate-700 border-slate-200'}`}
    >
      #{tag}
    </span>
  );
}
