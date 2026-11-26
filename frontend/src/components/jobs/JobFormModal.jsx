import React, { useState } from 'react';
import Modal from '../common/Modal';
import Input from '../common/Input';
import FormField from '../common/FormField';
import Button from '../common/Button';
import ErrorMessage from '../common/ErrorMessage';

const buildInitialForm = (initialJob) => {
  if (initialJob) {
    return {
      title: initialJob.title || '',
      company: initialJob.company || '',
      location: initialJob.location || '',
      workMode: initialJob.workMode || 'HYBRID',
      jobType: initialJob.jobType || 'FULL_TIME',
      experienceLevel: initialJob.experienceLevel || 'MID',
      salaryMin: initialJob.salaryMin != null ? String(initialJob.salaryMin) : '',
      salaryMax: initialJob.salaryMax != null ? String(initialJob.salaryMax) : '',
      salaryCurrency: initialJob.salaryCurrency || 'INR',
      salaryPeriod: initialJob.salaryPeriod || 'ANNUAL',
      description: initialJob.description || '',
      requiredSkillsText: (initialJob.requiredSkills || []).join(', '),
      preferredSkillsText: (initialJob.preferredSkills || []).join(', '),
      applicationUrl: initialJob.applicationUrl || '',
    };
  }
  return {
    title: '',
    company: '',
    location: '',
    workMode: 'HYBRID',
    jobType: 'FULL_TIME',
    experienceLevel: 'MID',
    salaryMin: '',
    salaryMax: '',
    salaryCurrency: 'INR',
    salaryPeriod: 'ANNUAL',
    description: '',
    requiredSkillsText: '',
    preferredSkillsText: '',
    applicationUrl: '',
  };
};

const selectClass =
  'block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500';

function JobFormContent({
  onClose,
  onSubmit,
  initialJob,
  submitting,
  serverError,
}) {
  const [form, setForm] = useState(() => buildInitialForm(initialJob));
  const [errors, setErrors] = useState({});

  const parseSkillList = (text) =>
    text
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);

  const validate = () => {
    const nextErrors = {};
    if (!form.title.trim()) nextErrors.title = 'Job title is required.';
    if (!form.company.trim()) nextErrors.company = 'Company name is required.';
    if (!form.location.trim()) nextErrors.location = 'Location is required.';
    if (!form.description.trim()) nextErrors.description = 'Description is required.';

    const requiredSkills = parseSkillList(form.requiredSkillsText);
    if (requiredSkills.length === 0) {
      nextErrors.requiredSkillsText = 'Provide at least one required skill (comma-separated).';
    }

    const minVal = form.salaryMin !== '' ? Number(form.salaryMin) : null;
    const maxVal = form.salaryMax !== '' ? Number(form.salaryMax) : null;
    if (minVal !== null && (Number.isNaN(minVal) || minVal < 0)) {
      nextErrors.salaryMin = 'Minimum salary must be 0 or greater.';
    }
    if (maxVal !== null && (Number.isNaN(maxVal) || maxVal < 0)) {
      nextErrors.salaryMax = 'Maximum salary must be 0 or greater.';
    }
    if (minVal !== null && maxVal !== null && minVal > maxVal) {
      nextErrors.salaryMax = 'Maximum salary must be greater than or equal to minimum salary.';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = {
      title: form.title.trim(),
      company: form.company.trim(),
      location: form.location.trim(),
      workMode: form.workMode,
      jobType: form.jobType,
      experienceLevel: form.experienceLevel,
      salaryMin: form.salaryMin !== '' ? Number(form.salaryMin) : null,
      salaryMax: form.salaryMax !== '' ? Number(form.salaryMax) : null,
      salaryCurrency: form.salaryCurrency.trim() || 'INR',
      salaryPeriod: form.salaryPeriod,
      description: form.description.trim(),
      requiredSkills: parseSkillList(form.requiredSkillsText),
      preferredSkills: parseSkillList(form.preferredSkillsText),
      applicationUrl: form.applicationUrl.trim() || null,
    };

    await onSubmit(payload);
  };

  const updateField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  return (
    <Modal
      isOpen
      onClose={() => {
        if (!submitting) onClose();
      }}
      title={initialJob ? 'Edit Job Posting' : 'Post a New Job'}
      maxWidth="max-w-2xl"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSubmit} loading={submitting}>
            {initialJob ? 'Save Changes' : 'Publish Job'}
          </Button>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
        <ErrorMessage message={serverError} />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Job Title"
            value={form.title}
            onChange={(e) => updateField('title', e.target.value)}
            placeholder="e.g. Senior Java Backend Engineer"
            error={errors.title}
            required
          />
          <Input
            label="Company"
            value={form.company}
            onChange={(e) => updateField('company', e.target.value)}
            placeholder="e.g. Nimbus Cloud Systems"
            error={errors.company}
            required
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Location"
            value={form.location}
            onChange={(e) => updateField('location', e.target.value)}
            placeholder="e.g. Bengaluru, Karnataka or Remote"
            error={errors.location}
            required
          />
          <FormField label="Work Mode" required>
            <select
              value={form.workMode}
              onChange={(e) => updateField('workMode', e.target.value)}
              className={selectClass}
            >
              <option value="ONSITE">On-site</option>
              <option value="HYBRID">Hybrid</option>
              <option value="REMOTE">Remote</option>
            </select>
          </FormField>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <FormField label="Job Type" required>
            <select
              value={form.jobType}
              onChange={(e) => updateField('jobType', e.target.value)}
              className={selectClass}
            >
              <option value="FULL_TIME">Full-time</option>
              <option value="PART_TIME">Part-time</option>
              <option value="CONTRACT">Contract</option>
              <option value="INTERNSHIP">Internship</option>
            </select>
          </FormField>
          <FormField label="Experience Level" required>
            <select
              value={form.experienceLevel}
              onChange={(e) => updateField('experienceLevel', e.target.value)}
              className={selectClass}
            >
              <option value="ENTRY">Entry Level</option>
              <option value="MID">Mid Level</option>
              <option value="SENIOR">Senior</option>
              <option value="LEAD">Lead</option>
            </select>
          </FormField>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
          <Input
            label="Min Salary"
            type="number"
            min="0"
            value={form.salaryMin}
            onChange={(e) => updateField('salaryMin', e.target.value)}
            placeholder="1200000"
            error={errors.salaryMin}
          />
          <Input
            label="Max Salary"
            type="number"
            min="0"
            value={form.salaryMax}
            onChange={(e) => updateField('salaryMax', e.target.value)}
            placeholder="1800000"
            error={errors.salaryMax}
          />
          <Input
            label="Currency"
            value={form.salaryCurrency}
            onChange={(e) => updateField('salaryCurrency', e.target.value.toUpperCase())}
            placeholder="INR"
            maxLength={10}
          />
          <FormField label="Period">
            <select
              value={form.salaryPeriod}
              onChange={(e) => updateField('salaryPeriod', e.target.value)}
              className={selectClass}
            >
              <option value="ANNUAL">Annual</option>
              <option value="HOURLY">Hourly</option>
            </select>
          </FormField>
        </div>

        <FormField
          label="Required Skills"
          required
          error={errors.requiredSkillsText}
          helperText="Separate skills with commas (e.g. Java, Spring Boot, PostgreSQL, Docker)"
        >
          <input
            type="text"
            value={form.requiredSkillsText}
            onChange={(e) => updateField('requiredSkillsText', e.target.value)}
            placeholder="Java, Spring Boot, PostgreSQL, Docker"
            className={selectClass}
          />
        </FormField>

        <FormField
          label="Preferred Skills"
          helperText="Optional nice-to-have skills, comma-separated"
        >
          <input
            type="text"
            value={form.preferredSkillsText}
            onChange={(e) => updateField('preferredSkillsText', e.target.value)}
            placeholder="Kubernetes, Redis, AWS"
            className={selectClass}
          />
        </FormField>

        <Input
          label="External Application URL (Optional)"
          type="url"
          value={form.applicationUrl}
          onChange={(e) => updateField('applicationUrl', e.target.value)}
          placeholder="https://company.com/careers/role"
        />

        <FormField label="Job Description" required error={errors.description}>
          <textarea
            rows={4}
            value={form.description}
            onChange={(e) => updateField('description', e.target.value)}
            placeholder="Describe responsibilities, tech stack, and qualifications..."
            className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </FormField>
      </form>
    </Modal>
  );
}

export default function JobFormModal({
  isOpen,
  onClose,
  onSubmit,
  initialJob = null,
  submitting = false,
  serverError = null,
}) {
  if (!isOpen) return null;

  return (
    <JobFormContent
      key={initialJob?.id || 'create'}
      onClose={onClose}
      onSubmit={onSubmit}
      initialJob={initialJob}
      submitting={submitting}
      serverError={serverError}
    />
  );
}
