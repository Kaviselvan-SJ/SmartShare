import React from 'react';
import { Loader2 } from 'lucide-react';

export default function Loader({ text = "Loading..." }) {
  return (
    <div className="flex flex-col items-center justify-center p-12 w-full">
      <Loader2 size={32} className="text-blue-500 animate-spin mb-4" />
      <p className="text-sm text-gray-500 font-medium">{text}</p>
    </div>
  );
}
