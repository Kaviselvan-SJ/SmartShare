import React, { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import Loader from '../components/ui/Loader';
import toast from 'react-hot-toast';
import { User, Briefcase, MapPin, AlignLeft, Image, Link2, GitBranch, Globe, Award, Type, Clock, Mail, ShieldAlert } from 'lucide-react';

export default function Settings() {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    displayName: '',
    jobProfile: '',
    organization: '',
    location: '',
    bio: '',
    profileImageUrl: '',
    linkedinUrl: '',
    githubUrl: '',
    portfolioUrl: '',
    experienceLevel: '',
    preferredLanguage: '',
    timezone: '',
    emailNotificationsEnabled: false,
    defaultLinkExpiryDays: 7
  });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await axiosClient.get('/user/profile');
        setFormData(prev => ({ ...prev, ...res.data }));
      } catch (err) {
        toast.error('Failed to load profile data');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await axiosClient.put('/user/profile', formData);
      toast.success('Profile updated successfully! Refresh to see changes globally.');
    } catch (err) {
      toast.error(err.response?.data || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Loader text="Loading your settings..." />;

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Account Settings</h1>
        <p className="text-slate-500 mt-1">Manage your public profile, links, and system preferences.</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-8">
        
        {/* Basic Information */}
        <div className="bg-white p-6 md:p-8 rounded-2xl border border-gray-100 shadow-sm space-y-6">
          <h2 className="text-lg font-semibold text-slate-800 flex items-center"><User className="mr-2 text-blue-500" size={20}/> Basic Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email (Read Only)</label>
              <input type="email" value={currentUser?.email || ''} disabled className="w-full bg-slate-100 text-slate-500 rounded-lg px-4 py-2 border border-slate-200" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Display Name</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <User className="h-4 w-4 text-slate-400" />
                </div>
                <input 
                  type="text" name="displayName" value={formData.displayName || ''} onChange={handleChange} maxLength="50"
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="John Doe" 
                />
              </div>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Profile Image URL</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Image className="h-4 w-4 text-slate-400" />
                </div>
                <input 
                  type="url" name="profileImageUrl" value={formData.profileImageUrl || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="https://example.com/my-avatar.png" 
                />
              </div>
            </div>
          </div>
        </div>

        {/* Professional Details */}
        <div className="bg-white p-6 md:p-8 rounded-2xl border border-gray-100 shadow-sm space-y-6">
          <h2 className="text-lg font-semibold text-slate-800 flex items-center"><Briefcase className="mr-2 text-indigo-500" size={20}/> Professional Details</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Job Profile</label>
              <select 
                name="jobProfile" value={formData.jobProfile || ''} onChange={handleChange}
                className="w-full border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
              >
                <option value="">Select an option</option>
                <option value="Student">Student</option>
                <option value="Software Engineer">Software Engineer (SDE)</option>
                <option value="Teacher">Teacher / Educator</option>
                <option value="Manager">Manager</option>
                <option value="Business Professional">Business Professional</option>
                <option value="Normal Person">Normal Person</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Experience Level</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Award className="h-4 w-4 text-slate-400" />
                </div>
                <select 
                  name="experienceLevel" value={formData.experienceLevel || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
                >
                  <option value="">Select Level</option>
                  <option value="Entry">Entry Level</option>
                  <option value="Intermediate">Intermediate</option>
                  <option value="Senior">Senior</option>
                  <option value="Expert">Expert / Lead</option>
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Organization / School</label>
              <input 
                type="text" name="organization" value={formData.organization || ''} onChange={handleChange}
                className="w-full border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                placeholder="Google, MIT, etc." 
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Location</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MapPin className="h-4 w-4 text-slate-400" />
                </div>
                <input 
                  type="text" name="location" value={formData.location || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="San Francisco, CA" 
                />
              </div>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Bio</label>
              <div className="relative">
                <div className="absolute top-3 left-3 pointer-events-none">
                  <AlignLeft className="h-4 w-4 text-slate-400" />
                </div>
                <textarea 
                  name="bio" value={formData.bio || ''} onChange={handleChange} maxLength="500" rows="3"
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="Tell us a little bit about yourself..." 
                ></textarea>
              </div>
            </div>
          </div>
        </div>

        {/* Links & Socials */}
        <div className="bg-white p-6 md:p-8 rounded-2xl border border-gray-100 shadow-sm space-y-6">
          <h2 className="text-lg font-semibold text-slate-800 flex items-center"><Globe className="mr-2 text-emerald-500" size={20}/> Links & Profiles</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">LinkedIn Profile</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Link2 className="h-4 w-4 text-slate-400" />
                </div>
                <input 
                  type="url" name="linkedinUrl" value={formData.linkedinUrl || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="https://linkedin.com/in/username" 
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">GitHub Profile</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <GitBranch className="h-4 w-4 text-slate-400" />
                </div>
                <input 
                  type="url" name="githubUrl" value={formData.githubUrl || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="https://github.com/username" 
                />
              </div>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Personal Portfolio</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Globe className="h-4 w-4 text-slate-400" />
                </div>
                <input 
                  type="url" name="portfolioUrl" value={formData.portfolioUrl || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  placeholder="https://myportfolio.com" 
                />
              </div>
            </div>
          </div>
        </div>

        {/* Preferences */}
        <div className="bg-white p-6 md:p-8 rounded-2xl border border-gray-100 shadow-sm space-y-6">
          <h2 className="text-lg font-semibold text-slate-800 flex items-center"><ShieldAlert className="mr-2 text-orange-500" size={20}/> App Preferences</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Preferred Language</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Type className="h-4 w-4 text-slate-400" />
                </div>
                <select 
                  name="preferredLanguage" value={formData.preferredLanguage || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
                >
                  <option value="en">English (US)</option>
                  <option value="es">Español</option>
                  <option value="fr">Français</option>
                  <option value="de">Deutsch</option>
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Timezone</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Clock className="h-4 w-4 text-slate-400" />
                </div>
                <select 
                  name="timezone" value={formData.timezone || ''} onChange={handleChange}
                  className="w-full pl-10 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
                >
                  <option value="UTC">UTC</option>
                  <option value="America/New_York">Eastern Time (US & Canada)</option>
                  <option value="America/Chicago">Central Time (US & Canada)</option>
                  <option value="America/Denver">Mountain Time (US & Canada)</option>
                  <option value="America/Los_Angeles">Pacific Time (US & Canada)</option>
                  <option value="Asia/Kolkata">India Standard Time</option>
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Default Link Expiry (Days)</label>
              <input 
                type="number" name="defaultLinkExpiryDays" value={formData.defaultLinkExpiryDays || ''} onChange={handleChange} min="1" max="365"
                className="w-full border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
              />
            </div>
            <div className="flex items-center space-x-3 pt-6">
              <input 
                type="checkbox" name="emailNotificationsEnabled" id="emailNotificationsEnabled" 
                checked={formData.emailNotificationsEnabled || false} onChange={handleChange}
                className="h-5 w-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500" 
              />
              <label htmlFor="emailNotificationsEnabled" className="text-sm font-medium text-slate-700 flex items-center">
                <Mail className="h-4 w-4 text-slate-400 mr-2" />
                Enable Email Notifications
              </label>
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button 
            type="submit" 
            disabled={saving}
            className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-full text-sm font-medium transition-all shadow-md hover:shadow-lg disabled:opacity-50"
          >
            {saving ? 'Saving...' : 'Save Preferences'}
          </button>
        </div>

      </form>
    </div>
  );
}
