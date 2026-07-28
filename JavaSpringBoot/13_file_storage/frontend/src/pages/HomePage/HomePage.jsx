import React, { useContext, useState } from 'react';
import axios from 'axios';
import { toast } from 'react-hot-toast'; // Ensure toast is imported
import { DataContext } from '../../context';

const MEDIA_SERVICE_URL = "http://localhost:8080"; // Image Service Port 8080

const ProfileImageUploader = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {checkSession, apiPost} = useContext(DataContext);

  const handleUpdateProfileImage = async (file, setButton, onSuccess = async () => {}) => {
    if (!file) {
      toast.error("Please select an image file first.");
      return;
    }

    try {
      setButton?.(true);

      // Verify active session and retrieve token
      const freshToken = await checkSession();
      if (!freshToken) {
        toast.error("Session expired. Please log in again.");
        return null;
      }

      // -----------------------------------------------------------------
      // Step 1: Request Presigned S3 URL from Image Service (Port 8080)
      // -----------------------------------------------------------------
      const presignResponse = await axios.post(
        `${MEDIA_SERVICE_URL}/api/v1/media/presign-upload`,
        {
          fileName: file.name,
          mimeType: file.type,
          fileSize: file.size,
        },
        {
          headers: {
            Authorization: `Bearer ${freshToken}`,
            "Content-Type": "application/json",
          },
          timeout: 10000,
        }
      );

      const { mediaId, uploadUrl } = presignResponse.data;

      if (!mediaId || !uploadUrl) {
        throw new Error("Invalid presigned URL response from Media Service.");
      }

      // -----------------------------------------------------------------
      // Step 2: Direct Binary Upload to S3 Bucket
      // -----------------------------------------------------------------
      await axios.put(uploadUrl, file, {
        headers: {
          "Content-Type": file.type,
        },
      });

      // -----------------------------------------------------------------
      // Step 3: Confirm Upload & Update Profile in Main Backend (Port 8081)
      // -----------------------------------------------------------------
      // apiPost automatically sends requests to http://localhost:8081
      const updateResponse = await apiPost(
        "/user-profile/update-profile-image",
        { mediaId: mediaId },
        setButton,
        onSuccess
      );

      return updateResponse;

    } catch (error) {
      console.error("Profile Image Update Failed:", error);
      const data = error?.response?.data;
      const message =
        data?.error ||
        data?.message ||
        (Array.isArray(data?.errors) ? data.errors.join(", ") : null) ||
        error.message ||
        "Failed to update profile image.";

      toast.error(message);
      return null;
    } finally {
      setButton?.(false);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleSubmit = async () => {
    await handleUpdateProfileImage(
      selectedFile,
      setIsSubmitting,
      async () => {
        toast.success("Profile image updated successfully!");
        setSelectedFile(null); // Reset input state after success
      }
    );
  };

  return (
    <div className="profile-image-uploader">
      <input type="file" accept="image/*" onChange={handleFileChange} />
      <button 
        onClick={handleSubmit} 
        disabled={!selectedFile || isSubmitting}
      >
        {isSubmitting ? "Uploading..." : "Update Profile Image"}
      </button>
    </div>
  );
};

export default ProfileImageUploader;