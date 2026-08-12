import React from 'react';
import { Phone, Calendar, Clock, Edit3 } from 'lucide-react';

export const UserProfile = ({ data }) => {
  if (!data) return null;

  // Clean trailing spaces from names
  const fullName = `${data.firstName?.trim() || ''} ${data.lastName?.trim() || ''}`;

  // Format dates nicely
  const formattedJoinedDate = data.createdAt
    ? new Date(data.createdAt).toLocaleDateString('en-US', {
        month: 'short',
        year: 'numeric'
      })
    : '';

  const formattedUpdatedDate = data.updatedAt
    ? new Date(data.updatedAt).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    : '';

  return (
    <div className="max-w-md mx-auto bg-white dark:bg-slate-900 rounded-2xl shadow-xl overflow-hidden border border-slate-100 dark:border-slate-800 transition-all">
      {/* Header Banner */}
      <div className="h-28 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 relative" />

      <div className="px-6 pb-6 relative">
        {/* Avatar & Action Button */}
        <div className="flex justify-between items-end -mt-14 mb-4">
          <div className="relative group">
            <img
              src={data.profileMedia?.presignedUrl || "https://via.placeholder.com/150"}
              alt={fullName}
              className="w-24 h-24 rounded-full object-cover ring-4 ring-white dark:ring-slate-900 shadow-md bg-slate-100"
            />
            <span className="absolute bottom-1 right-1 w-4 h-4 bg-emerald-500 border-2 border-white dark:border-slate-900 rounded-full" />
          </div>

          <button className="flex items-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 dark:bg-slate-100 dark:hover:bg-white text-white dark:text-slate-900 text-sm font-medium rounded-lg transition-colors shadow-sm">
            <Edit3 size={15} />
            Edit Profile
          </button>
        </div>

        {/* User Identity */}
        <div className="mb-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            {fullName}
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400 font-mono mt-0.5">
            User ID: #{data.userId}
          </p>
        </div>

        {/* Bio */}
        <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed mb-6">
          {data.bio}
        </p>

        {/* Info List */}
        <div className="space-y-3 pt-4 border-t border-slate-100 dark:border-slate-800 text-sm text-slate-600 dark:text-slate-400">
          <div className="flex items-center gap-3">
            <Phone size={16} className="text-slate-400" />
            <span>+91 {data.phoneNumber}</span>
          </div>

          <div className="flex items-center gap-3">
            <Calendar size={16} className="text-slate-400" />
            <span>Joined {formattedJoinedDate}</span>
          </div>

          <div className="flex items-center gap-3 text-xs text-slate-400">
            <Clock size={15} />
            <span>Last updated: {formattedUpdatedDate}</span>
          </div>
        </div>
      </div>
    </div>
  );
};