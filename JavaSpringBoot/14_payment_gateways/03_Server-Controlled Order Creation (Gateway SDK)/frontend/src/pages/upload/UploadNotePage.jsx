import React, { useContext, useState } from 'react';
import { UploadCloud, FileText, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
import { DataContext } from '../../context';
import { useNavigate } from 'react-router-dom';

export const UploadNotePage = () => {
  const { apiAuthPost } = useContext(DataContext);
  const navigate = useNavigate();

  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Handle file selection
  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setError('');
      setFile(selectedFile);
    }
  };

  // Submit file to /upload-notes-asset


  const handleSubmit = async (e) => {
  e.preventDefault();
  if (!file) return;

  const formData = new FormData();

  // 1. Part "file" -> Multipart File
  formData.append('file', file);

  // 2. Part "data" -> Wrapped in a Blob with explicit application/json MIME type
  const noteData = {
    title: file.name,
    description: 'Uploaded note file',
    priceInSubunits: 0,
    currency: 'INR',
    isPublished: true,
  };

  const jsonBlob = new Blob([JSON.stringify(noteData)], {
    type: 'application/json',
  });

  formData.append('data', jsonBlob);

  // Send request using your DataContext helper
  await apiAuthPost('/api/v1/notes/upload-notes-asset', formData, setLoading, async () => {
    navigate('/products');
  });
};


  return (
    <div className="max-w-xl mx-auto my-8 p-6 bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-100 dark:border-slate-800 transition-all">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <FileText className="text-indigo-500" /> Upload Note File
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Select your PDF or Word document to upload.
        </p>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 rounded-lg flex items-center gap-2 text-sm text-red-600 dark:text-red-400">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Dropzone */}
        <div className="border-2 border-dashed border-slate-200 dark:border-slate-700 rounded-xl p-6 text-center hover:border-indigo-500 dark:hover:border-indigo-500 transition-colors cursor-pointer bg-slate-50/50 dark:bg-slate-800/30">
          <input
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={handleFileChange}
            className="hidden"
            id="pdf-upload-input"
          />
          <label htmlFor="pdf-upload-input" className="cursor-pointer block">
            {file ? (
              <div className="flex items-center justify-center gap-2 text-emerald-600 dark:text-emerald-400 font-medium">
                <CheckCircle size={20} />
                <span className="truncate max-w-xs">{file.name}</span>
              </div>
            ) : (
              <div className="flex flex-col items-center gap-2">
                <UploadCloud size={36} className="text-indigo-500" />
                <span className="text-sm font-medium text-slate-700 dark:text-slate-200">
                  Click to choose a file
                </span>
                <span className="text-xs text-slate-400">Supported formats: PDF, DOC, DOCX</span>
              </div>
            )}
          </label>
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading || !file}
          className="w-full mt-4 py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white font-medium text-sm rounded-lg transition-colors shadow-sm flex items-center justify-center gap-2"
        >
          {loading ? (
            <>
              <Loader2 size={16} className="animate-spin" /> Uploading Note...
            </>
          ) : (
            'Upload Note'
          )}
        </button>
      </form>
    </div>
  );
};

export default UploadNotePage;