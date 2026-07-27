import React, { useState } from "react";
import {
  User,
  Mail,
  Shield,
  Key,
  Smartphone,
  Globe,
  Clock,
  CheckCircle,
  AlertCircle,
  LogOut,
  Save,
  Lock,
  History,
  Activity
} from "lucide-react";

export default function AdminUserProfile() {
  const [activeTab, setActiveTab] = useState("profile");

  // Sample User Data - replace with state/props from your auth context
  const [userProfile, setUserProfile] = useState({
    name: "Alex Morgan",
    email: "admin.alex@example.com",
    role: "Super Admin",
    status: "Active",
    isEmailVerified: true,
    createdDate: "Jan 15, 2024",
    lastLogin: "Just now",
  });

  // Active sessions mock data
  const [sessions, setSessions] = useState([
    {
      id: "sess_101",
      deviceName: "Chrome on Windows 11",
      ipAddress: "192.168.1.45",
      location: "New York, USA",
      lastActive: "Active now",
      isCurrent: true,
    },
    {
      id: "sess_102",
      deviceName: "Safari on macOS",
      ipAddress: "172.56.21.12",
      location: "London, UK",
      lastActive: "2 hours ago",
      isCurrent: false,
    },
    {
      id: "sess_103",
      deviceName: "App on iPhone 15 Pro",
      ipAddress: "105.22.41.89",
      location: "Tokyo, Japan",
      lastActive: "3 days ago",
      isCurrent: false,
    },
  ]);

  const handleRevokeSession = (sessionId) => {
    setSessions(sessions.filter((s) => s.id !== sessionId));
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 p-6 md:p-10 font-sans">
      <div className="max-w-6xl mx-auto space-y-8">
        
        {/* Header Hero Banner */}
        <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-indigo-900 via-slate-800 to-indigo-950 p-6 md:p-8 border border-slate-800 shadow-2xl">
          <div className="absolute top-0 right-0 -mt-12 -mr-12 w-64 h-64 rounded-full bg-indigo-500/10 blur-3xl pointer-events-none"></div>
          
          <div className="flex flex-col md:flex-row items-center md:items-start gap-6 relative z-10">
            {/* Avatar with Glow */}
            <div className="relative group">
              <div className="w-28 h-28 rounded-2xl bg-gradient-to-tr from-indigo-500 to-purple-500 p-1 shadow-lg shadow-indigo-500/20">
                <div className="w-full h-full bg-slate-900 rounded-[14px] flex items-center justify-center text-3xl font-bold text-indigo-400">
                  {userProfile.name.split(" ").map((n) => n[0]).join("")}
                </div>
              </div>
              <span className="absolute bottom-1 right-1 w-4 h-4 bg-emerald-500 border-2 border-slate-900 rounded-full"></span>
            </div>

            {/* Profile Brief Info */}
            <div className="flex-1 text-center md:text-left space-y-2">
              <div className="flex flex-col md:flex-row md:items-center gap-3">
                <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
                  {userProfile.name}
                </h1>
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 w-fit mx-auto md:mx-0">
                  <Shield className="w-3.5 h-3.5 mr-1" />
                  {userProfile.role}
                </span>
              </div>
              
              <p className="text-slate-400 text-sm flex items-center justify-center md:justify-start gap-2">
                <Mail className="w-4 h-4 text-slate-500" />
                {userProfile.email}
                {userProfile.isEmailVerified && (
                  <span className="text-emerald-400 text-xs flex items-center gap-1 font-medium bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                    <CheckCircle className="w-3 h-3" /> Verified
                  </span>
                )}
              </p>

              <div className="flex flex-wrap items-center justify-center md:justify-start gap-6 pt-2 text-xs text-slate-400">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-4 h-4 text-slate-500" /> Member since {userProfile.createdDate}
                </span>
                <span className="flex items-center gap-1.5">
                  <Activity className="w-4 h-4 text-emerald-400" /> Last Active: {userProfile.lastLogin}
                </span>
              </div>
            </div>
          </div>

          {/* Navigation Tabs */}
          <div className="flex gap-2 mt-8 border-b border-slate-800/80 pt-2 overflow-x-auto">
            {[
              { id: "profile", label: "General Settings", icon: User },
              { id: "security", label: "Security & Passwords", icon: Key },
              { id: "sessions", label: "Active Sessions", icon: Smartphone },
            ].map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-all whitespace-nowrap ${
                    isActive
                      ? "border-indigo-500 text-indigo-400 bg-indigo-500/5 rounded-t-lg"
                      : "border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Tab Content Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Work Area */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* GENERAL PROFILE SETTINGS */}
            {activeTab === "profile" && (
              <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl p-6 border border-slate-800 shadow-xl space-y-6">
                <div>
                  <h2 className="text-lg font-semibold text-white">Personal Information</h2>
                  <p className="text-xs text-slate-400">Update your account details and profile information.</p>
                </div>

                <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-medium text-slate-300 mb-1.5">Full Name</label>
                      <input
                        type="text"
                        defaultValue={userProfile.name}
                        className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-medium text-slate-300 mb-1.5">Email Address</label>
                      <input
                        type="email"
                        defaultValue={userProfile.email}
                        className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-slate-300 mb-1.5">Role / Designation</label>
                    <input
                      type="text"
                      disabled
                      defaultValue={userProfile.role}
                      className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-500 cursor-not-allowed"
                    />
                    <span className="text-[11px] text-slate-500 mt-1 block">System roles can only be changed by standard administration.</span>
                  </div>

                  <div className="pt-4 flex justify-end">
                    <button className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium px-5 py-2.5 rounded-xl transition shadow-lg shadow-indigo-600/20">
                      <Save className="w-4 h-4" /> Save Changes
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* SECURITY SETTINGS */}
            {activeTab === "security" && (
              <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl p-6 border border-slate-800 shadow-xl space-y-6">
                <div>
                  <h2 className="text-lg font-semibold text-white">Change Password</h2>
                  <p className="text-xs text-slate-400">Ensure your administrative account uses a strong, unique password.</p>
                </div>

                <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
                  <div>
                    <label className="block text-xs font-medium text-slate-300 mb-1.5">Current Password</label>
                    <input
                      type="password"
                      placeholder="••••••••••••"
                      className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-medium text-slate-300 mb-1.5">New Password</label>
                      <input
                        type="password"
                        placeholder="••••••••••••"
                        className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-medium text-slate-300 mb-1.5">Confirm New Password</label>
                      <input
                        type="password"
                        placeholder="••••••••••••"
                        className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                      />
                    </div>
                  </div>

                  <div className="pt-4 flex justify-end">
                    <button className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium px-5 py-2.5 rounded-xl transition shadow-lg shadow-indigo-600/20">
                      <Lock className="w-4 h-4" /> Update Password
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* SESSIONS MANAGEMENT */}
            {activeTab === "sessions" && (
              <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl p-6 border border-slate-800 shadow-xl space-y-6">
                <div>
                  <h2 className="text-lg font-semibold text-white">Active Sessions</h2>
                  <p className="text-xs text-slate-400">Manage and revoke active sessions connected to this account.</p>
                </div>

                <div className="space-y-3">
                  {sessions.map((sess) => (
                    <div
                      key={sess.id}
                      className="flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-xl bg-slate-900/60 border border-slate-800 gap-4"
                    >
                      <div className="flex items-start gap-3.5">
                        <div className="p-2.5 rounded-xl bg-slate-800 text-indigo-400 border border-slate-700/50 mt-0.5 sm:mt-0">
                          <Smartphone className="w-5 h-5" />
                        </div>
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="text-sm font-semibold text-white">{sess.deviceName}</span>
                            {sess.isCurrent && (
                              <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                                Current Device
                              </span>
                            )}
                          </div>
                          <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-400">
                            <span className="flex items-center gap-1">
                              <Globe className="w-3 h-3 text-slate-500" /> {sess.ipAddress} ({sess.location})
                            </span>
                            <span className="flex items-center gap-1">
                              <History className="w-3 h-3 text-slate-500" /> {sess.lastActive}
                            </span>
                          </div>
                        </div>
                      </div>

                      {!sess.isCurrent && (
                        <button
                          onClick={() => handleRevokeSession(sess.id)}
                          className="flex items-center gap-1.5 text-xs font-medium text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 px-3 py-2 rounded-lg border border-rose-500/20 transition self-end sm:self-center"
                        >
                          <LogOut className="w-3.5 h-3.5" /> Revoke
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Sidebar Stats & Security Summary */}
          <div className="space-y-6">
            {/* Account Status Card */}
            <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl p-6 border border-slate-800 shadow-xl space-y-4">
              <h3 className="text-sm font-semibold text-slate-200">Account Overview</h3>
              <div className="space-y-3">
                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-400">Account Status</span>
                  <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    Active
                  </span>
                </div>
                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-400">Two-Factor Auth</span>
                  <span className="text-indigo-400 font-medium">Enabled</span>
                </div>
                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-400">Active Sessions</span>
                  <span className="text-slate-200 font-bold">{sessions.length}</span>
                </div>
              </div>
            </div>

            {/* Quick Warning / Recommendation */}
            <div className="p-4 rounded-2xl bg-amber-500/5 border border-amber-500/20 text-amber-300/90 space-y-2">
              <div className="flex items-center gap-2 text-xs font-bold text-amber-400">
                <AlertCircle className="w-4 h-4" /> Security Tip
              </div>
              <p className="text-xs leading-relaxed text-slate-400">
                Rotate your access tokens regularly and ensure all inactive sessions are revoked.
              </p>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}