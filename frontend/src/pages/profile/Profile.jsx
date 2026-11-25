import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/common/Card';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import ErrorMessage from '../../components/common/ErrorMessage';
import ToastNotification from '../../components/common/ToastNotification';
import { User, Mail, Phone, MapPin, Briefcase, Link2, Code2, Globe, Sparkles, Save } from 'lucide-react';

export default function Profile() {
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState({
    fullName: '',
    phone: '',
    location: '',
    headline: '',
    linkedinUrl: '',
    githubUrl: '',
    portfolioUrl: '',
    careerInterests: '',
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const res = await api.get('/users/me');
      if (res.data?.data) {
        const d = res.data.data;
        setProfile({
          fullName: d.fullName || '',
          phone: d.phone || '',
          location: d.location || '',
          headline: d.headline || '',
          linkedinUrl: d.linkedinUrl || '',
          githubUrl: d.githubUrl || '',
          portfolioUrl: d.portfolioUrl || '',
          careerInterests: d.careerInterests || '',
        });
      }
    } catch (err) {
      setError('Failed to load profile details. Please refresh.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field, value) => {
    setProfile((prev) => ({ ...prev, [field]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const res = await api.put('/users/me', profile);
      updateUser(res.data.data);
      setToast({ message: 'Profile updated successfully!', type: 'success' });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile. Please check inputs.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">User Profile</h1>
          <p className="text-sm text-slate-500">Manage your personal information and career preferences</p>
        </div>
        <LoadingSkeleton count={3} variant="card" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {toast && (
        <ToastNotification
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">User Profile</h1>
          <p className="text-sm text-slate-500 mt-1">Manage your identity, professional links, and career targets</p>
        </div>
        <Button
          onClick={handleSave}
          variant="primary"
          loading={saving}
          className="self-start sm:self-auto"
        >
          <Save className="w-4 h-4 mr-2" /> Save Profile
        </Button>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      <form onSubmit={handleSave} className="space-y-6">
        {/* Personal Details */}
        <Card title="Personal Information" subtitle="Your contact coordinates for employers and recruiters">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Input
              label="Full Name"
              value={profile.fullName}
              onChange={(e) => handleChange('fullName', e.target.value)}
              placeholder="e.g. Jane Doe"
              icon={User}
              required
            />

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  value={user?.email || ''}
                  disabled
                  className="w-full pl-9 pr-3 py-2 bg-slate-100 border border-slate-200 rounded-lg text-sm text-slate-500 cursor-not-allowed font-mono"
                />
              </div>
              <p className="text-[11px] text-slate-400 mt-1">Account login email cannot be edited.</p>
            </div>

            <Input
              label="Phone Number"
              value={profile.phone}
              onChange={(e) => handleChange('phone', e.target.value)}
              placeholder="+1 (555) 000-0000"
              icon={Phone}
            />

            <Input
              label="Location"
              value={profile.location}
              onChange={(e) => handleChange('location', e.target.value)}
              placeholder="San Francisco, CA or Remote"
              icon={MapPin}
            />
          </div>
        </Card>

        {/* Professional Details & Social Links */}
        <Card title="Professional Presence" subtitle="Highlight your engineering headline and online portfolios">
          <div className="space-y-5">
            <Input
              label="Professional Headline"
              value={profile.headline}
              onChange={(e) => handleChange('headline', e.target.value)}
              placeholder="e.g. Senior Full-Stack Engineer | Distributed Systems & AI"
              icon={Briefcase}
            />

            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              <Input
                label="LinkedIn Profile"
                value={profile.linkedinUrl}
                onChange={(e) => handleChange('linkedinUrl', e.target.value)}
                placeholder="https://linkedin.com/in/username"
                icon={Link2}
              />

              <Input
                label="GitHub Profile"
                value={profile.githubUrl}
                onChange={(e) => handleChange('githubUrl', e.target.value)}
                placeholder="https://github.com/username"
                icon={Code2}
              />

              <Input
                label="Portfolio / Website"
                value={profile.portfolioUrl}
                onChange={(e) => handleChange('portfolioUrl', e.target.value)}
                placeholder="https://yourportfolio.dev"
                icon={Globe}
              />
            </div>
          </div>
        </Card>

        {/* Career Interests */}
        <Card title="Career Targets & Interests" subtitle="Fields, technologies, or roles you are actively targeting">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-indigo-500" /> Target Roles & Focus Domains
            </label>
            <textarea
              rows={4}
              value={profile.careerInterests}
              onChange={(e) => handleChange('careerInterests', e.target.value)}
              placeholder="e.g. Cloud Infrastructure, Distributed Systems, AI Platform Engineering, Kubernetes, Microservice architectures..."
              className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-lg text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-colors"
            />
            <p className="text-xs text-slate-400 mt-1">This helps tailor future job recommendations and skill match scoring.</p>
          </div>
        </Card>
      </form>
    </div>
  );
}
