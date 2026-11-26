export const WORK_MODE_LABELS = {
  ONSITE: 'On-site',
  REMOTE: 'Remote',
  HYBRID: 'Hybrid',
};

export const JOB_TYPE_LABELS = {
  FULL_TIME: 'Full-time',
  PART_TIME: 'Part-time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
};

export const EXPERIENCE_LEVEL_LABELS = {
  ENTRY: 'Entry Level',
  MID: 'Mid Level',
  SENIOR: 'Senior',
  LEAD: 'Lead',
};

export const SALARY_PERIOD_LABELS = {
  ANNUAL: '/ yr',
  HOURLY: '/ hr',
};

export const APPLICATION_STATUS_CONFIG = {
  APPLIED: {
    label: 'Awaiting Response',
    badgeVariant: 'primary',
    barColor: 'bg-indigo-600',
  },
  INTERVIEWING: {
    label: 'Interviewing',
    badgeVariant: 'warning',
    barColor: 'bg-amber-500',
  },
  OFFER: {
    label: 'Offer',
    badgeVariant: 'success',
    barColor: 'bg-emerald-600',
  },
  REJECTED: {
    label: 'Rejected',
    badgeVariant: 'danger',
    barColor: 'bg-rose-500',
  },
};

export const formatSalary = (salaryMin, salaryMax, currency = 'INR', period = 'ANNUAL') => {
  if (salaryMin == null && salaryMax == null) {
    return 'Compensation not disclosed';
  }

  const locale = currency === 'INR' ? 'en-IN' : 'en-US';
  const formatter = new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency || 'INR',
    maximumFractionDigits: 0,
  });

  const suffix = SALARY_PERIOD_LABELS[period] || '/ yr';

  if (salaryMin != null && salaryMax != null) {
    return `${formatter.format(salaryMin)} – ${formatter.format(salaryMax)} ${suffix}`;
  }
  if (salaryMin != null) {
    return `From ${formatter.format(salaryMin)} ${suffix}`;
  }
  return `Up to ${formatter.format(salaryMax)} ${suffix}`;
};

export const getMatchBadgeVariant = (score) => {
  if (score == null) return 'secondary';
  if (score >= 80) return 'success';
  if (score >= 50) return 'warning';
  return 'danger';
};

export const getApiErrorMessage = (err, fallback = 'Something went wrong. Please try again.') => {
  const data = err?.response?.data;
  if (data) {
    if (data.data && typeof data.data === 'object' && !Array.isArray(data.data)) {
      const fieldMessages = Object.values(data.data).filter(Boolean);
      if (fieldMessages.length > 0) {
        return fieldMessages.join(' ');
      }
    }
    if (data.message) {
      return data.message;
    }
  }
  return err?.message || fallback;
};
