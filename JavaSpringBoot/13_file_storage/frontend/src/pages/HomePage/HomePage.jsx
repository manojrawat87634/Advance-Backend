import React, { useState, useRef, useContext } from 'react';
import axios from 'axios';
import { toast } from 'react-hot-toast';
import { DataContext } from '../../context'; // Adjust import according to your context path

const InstagramAvatarUploader = ({ currentAvatarUrl, onUploadSuccess }) => {
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(currentAvatarUrl || '');
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  const { apiPost } = useContext(DataContext); // Custom helper or instance of Axios with auth header

  const handleFileChange = (e) => {
    const selected = e.target.files?.[0];
    if (!selected) return;

    if (!selected.type.startsWith('image/')) {
      toast.error('Please select an image file');
      return;
    }

    setFile(selected);
    setPreviewUrl(URL.createObjectURL(selected));
  };

  const handleUpload = async () => {
    if (!file) return;

    try {
      setUploading(true);
      toast.loading('Uploading profile picture...', { id: 'avatar-toast' });

      // =========================================================================
      // STEP 1: Get presigned upload URL & mediaId from Main Backend
      // =========================================================================
      const presignRes = await apiPost('/user-profile/presign-avatar', {
        fileName: file.name,
        mimeType: file.type,
        fileSize: file.size,
      });

      const { mediaId, uploadUrl } = presignRes.data || {};

      if (!mediaId || !uploadUrl) {
        throw new Error('Failed to obtain presigned upload details');
      }

      // =========================================================================
      // STEP 2: Send raw image binary directly to MinIO (Bypasses Main Backend)
      // =========================================================================
      await axios.put(uploadUrl, file, {
        headers: { 'Content-Type': file.type },
      });

      // =========================================================================
      // STEP 3: Send mediaId to Main Backend to confirm and link to User Profile
      // =========================================================================
      const updateRes = await apiPost('/user-profile/update-image', {
        mediaId: mediaId,
      });

      toast.success('Profile picture updated successfully!', { id: 'avatar-toast' });

      if (onUploadSuccess) {
        onUploadSuccess(mediaId, updateRes.data);
      }

      setFile(null);
    } catch (error) {
      console.error('Upload error:', error);
      const msg = error?.response?.data?.message || error.message || 'Failed to update avatar';
      toast.error(msg, { id: 'avatar-toast' });
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="flex flex-col items-center gap-3 p-4">
      {/* Circular Avatar Display */}
      <div 
        onClick={() => fileInputRef.current?.click()}
        className="group relative h-28 w-28 cursor-pointer overflow-hidden rounded-full border-2 border-gray-300 bg-gray-100 shadow-sm"
      >
        {previewUrl ? (
          <img 
            src={previewUrl} 
            alt="Profile Avatar" 
            className="h-full w-full object-cover" 
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center bg-gray-200 text-gray-500">
            <svg className="h-10 w-10" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
            </svg>
          </div>
        )}

        {/* Camera Hover Overlay */}
        <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 transition-opacity duration-200 group-hover:opacity-100">
          <svg className="h-7 w-7 text-white" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 9a3 3 0 100 6 3 3 0 000-6zm0-2a5 5 0 110 10 5 5 0 010-10z" />
            <path d="M4 5h3l2-2h6l2 2h3a2 2 0 012 2v11a2 2 0 01-2 2H4a2 2 0 01-2-2V7a2 2 0 012-2z" />
          </svg>
        </div>
      </div>

      {/* Hidden File Input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileChange}
        className="hidden"
      />

      {/* Control Buttons */}
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          disabled={uploading}
          className="text-sm font-semibold text-sky-500 hover:text-sky-600 disabled:opacity-50"
        >
          Change profile photo
        </button>

        {file && (
          <button
            type="button"
            onClick={handleUpload}
            disabled={uploading}
            className="rounded-lg bg-sky-500 px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-sky-600 disabled:opacity-50"
          >
            {uploading ? 'Uploading...' : 'Save Photo'}
          </button>
        )}
      </div>
    </div>
  );
};

export default InstagramAvatarUploader;