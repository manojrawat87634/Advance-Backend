import React, { useContext, useState } from 'react';
import axios from 'axios';
import { toast } from 'react-hot-toast';
import { DataContext } from '../../context';

const ProfileImageUploader = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { checkSession, apiPost } = useContext(DataContext);

  const handleUpdateProfileImage = async (file) => {
    if (!file) {
      toast.error("Please select an image file first.");
      return;
    }

    try {
      setIsSubmitting(true);

      // Verify active session and retrieve token
      const freshToken = await checkSession();
      if (!freshToken) {
        toast.error("Session expired. Please log in again.");
        return;
      }

      // STEP 1: Ask Main Backend for Presigned URL (Auth Proxy)
      const presignResponse = await apiPost("/presign-profile-image", {
        fileName: file.name,
        mimeType: file.type,
        fileSize: file.size,
      });

      const { mediaId, uploadUrl } = presignResponse.data;

      if (!mediaId || !uploadUrl) {
        throw new Error("Invalid presigned URL response.");
      }

      // STEP 2: Upload file bytes directly to MinIO
      await axios.put(uploadUrl, file, {
        headers: {
          "Content-Type": file.type,
        },
      });

      // STEP 3: Tell Main Backend to confirm media & update user profile
      await apiPost("/update-profile-image", { mediaId: mediaId });

      toast.success("Profile image updated successfully!");
      setSelectedFile(null);

    } catch (error) {
      console.error("Profile Image Update Failed:", error);
      const message = error?.response?.data?.message || error.message || "Failed to update profile image.";
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  return (
    <div className="profile-image-uploader">
      <input type="file" accept="image/*" onChange={handleFileChange} />
      <button 
        onClick={() => handleUpdateProfileImage(selectedFile)} 
        disabled={!selectedFile || isSubmitting}
      >
        {isSubmitting ? "Uploading..." : "Update Profile Image"}
      </button>
    </div>
  );
};

export default ProfileImageUploader;