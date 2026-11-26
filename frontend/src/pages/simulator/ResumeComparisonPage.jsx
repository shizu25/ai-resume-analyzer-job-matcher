import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  GitCompare,
  ArrowRight,
  TrendingUp,
  TrendingDown,
  CheckCircle2,
  AlertTriangle,
  Play,
  Layers,
  Sparkles,
  Award,
} from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import { simulatorService } from '../../services/simulatorService';
import { jobService } from '../../services/jobService';

export default function ResumeComparisonPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [versions, setVersions] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [baseVersionId, setBaseVersionId] = useState('');
  const [improvedVersionId, setImprovedVersionId] = useState(searchParams.get('improvedId') || '');
  const [selectedJobId, setSelectedJobId] = useState('');
  const [simulating, setSimulating] = useState(false);
  const [simulationResult, setSimulationResult] = useState(null);
  const [previousSimulations, setPreviousSimulations] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadPrerequisites();
  }, []);

  const loadPrerequisites = async () => {
    try {
      const [versionsData, jobsData, simList] = await Promise.all([
        simulatorService.listAllUserVersions(),
        jobService.listJobs(),
        simulatorService.listSimulations(),
      ]);

      const vList = versionsData || [];
      setVersions(vList);
      const jList = jobsData?.content || (Array.isArray(jobsData) ? jobsData : []);
      setJobs(jList);
      setPreviousSimulations(simList || []);

      if (vList.length >= 2) {
        setBaseVersionId(vList[vList.length - 1].id); // oldest as base
        if (!improvedVersionId) {
          setImprovedVersionId(vList[0].id); // newest as improved
        }
      } else if (vList.length === 1) {
        setBaseVersionId(vList[0].id);
        if (!improvedVersionId) setImprovedVersionId(vList[0].id);
      }

      if (jList.length > 0) {
        setSelectedJobId(jList[0].id);
      }
    } catch (err) {
      console.error('Failed to load comparison data:', err);
    }
  };

  const handleRunSimulation = async (e) => {
    e.preventDefault();
    if (!baseVersionId || !improvedVersionId || !selectedJobId) {
      setError('Please select both resume versions and the target job opportunity');
      return;
    }

    try {
      setSimulating(true);
      setError(null);
      const result = await simulatorService.runSimulation(baseVersionId, improvedVersionId, selectedJobId);
      setSimulationResult(result);
      setPreviousSimulations((prev) => [result, ...prev]);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to execute simulation. Check AI service connection.');
    } finally {
      setSimulating(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      {/* Top Banner */}
      <div className="border-b border-slate-200/80 pb-5">
        <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
          <GitCompare className="w-6 h-6 text-indigo-600" />
          Resume Improvement Simulator
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Perform rigorous side-by-side simulation to measure whether your revisions communicate qualifications more effectively against identical 80/20 scoring algorithms.
        </p>
      </div>

      {/* Selector Card */}
      <Card className="p-6">
        <form onSubmit={handleRunSimulation} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Baseline Version (Version A)
              </label>
              <select
                value={baseVersionId}
                onChange={(e) => setBaseVersionId(e.target.value)}
                className="w-full text-xs px-3 py-2 border border-slate-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                required
              >
                {versions.map((v) => (
                  <option key={v.id} value={v.id}>
                    V{v.versionNumber}: {v.versionName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Target Improved Version (Version B)
              </label>
              <select
                value={improvedVersionId}
                onChange={(e) => setImprovedVersionId(e.target.value)}
                className="w-full text-xs px-3 py-2 border border-slate-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                required
              >
                {versions.map((v) => (
                  <option key={v.id} value={v.id}>
                    V{v.versionNumber}: {v.versionName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Target Job Position
              </label>
              <select
                value={selectedJobId}
                onChange={(e) => setSelectedJobId(e.target.value)}
                className="w-full text-xs px-3 py-2 border border-slate-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                required
              >
                {jobs.map((j) => (
                  <option key={j.id} value={j.id}>
                    {j.title} • {j.company}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {error && (
            <div className="p-3 text-xs bg-rose-50 border border-rose-200 text-rose-700 rounded-lg flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              {error}
            </div>
          )}

          <div className="flex justify-end pt-2">
            <Button type="submit" disabled={simulating} className="flex items-center gap-2">
              <Play className="w-4 h-4 fill-current" />
              {simulating ? 'Simulating Dual Scoring...' : 'Run Side-by-Side Simulation'}
            </Button>
          </div>
        </form>
      </Card>

      {/* Simulation Results Display */}
      {simulationResult && (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
          {/* Main Score Comparison Banner */}
          <Card className="bg-gradient-to-br from-slate-900 to-indigo-950 text-white border-0 shadow-lg p-6 sm:p-8">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-indigo-300">
                  Simulation Outcome against {simulationResult.jobTitle}
                </span>
                <h2 className="text-2xl font-extrabold text-white mt-1">
                  {simulationResult.baseVersionName} vs {simulationResult.improvedVersionName}
                </h2>
                <p className="text-xs text-indigo-200 mt-2 max-w-xl leading-relaxed">
                  {simulationResult.explanation}
                </p>
              </div>

              {/* Score Delta Display */}
              <div className="flex items-center gap-6 bg-white/10 backdrop-blur-md rounded-2xl p-5 border border-white/15 shrink-0">
                <div className="text-center">
                  <span className="text-xs font-semibold text-slate-300 block">Baseline</span>
                  <span className="text-2xl font-bold text-slate-200">{simulationResult.baseScore}%</span>
                </div>

                <div className="flex flex-col items-center">
                  <ArrowRight className="w-5 h-5 text-indigo-300" />
                  <span
                    className={`text-xs font-extrabold px-2 py-0.5 rounded-full mt-1 ${
                      simulationResult.scoreDelta >= 0
                        ? 'bg-emerald-500 text-white'
                        : 'bg-rose-500 text-white'
                    }`}
                  >
                    {simulationResult.scoreDelta >= 0 ? '+' : ''}
                    {simulationResult.scoreDelta}%
                  </span>
                </div>

                <div className="text-center">
                  <span className="text-xs font-semibold text-emerald-300 block">Improved</span>
                  <span className="text-3xl font-extrabold text-emerald-400">
                    {simulationResult.improvedScore}%
                  </span>
                </div>
              </div>
            </div>

            {/* Sub-Metrics Comparison */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-8 pt-6 border-t border-indigo-800/40">
              <div className="bg-white/5 rounded-xl p-4 border border-white/10 space-y-2">
                <div className="flex justify-between text-xs">
                  <span className="text-indigo-200 font-semibold">Skill Requirement Coverage</span>
                  <span className="font-bold text-white">
                    {simulationResult.skillCoverageBase}% &rarr; {simulationResult.skillCoverageImproved}%
                  </span>
                </div>
                <div className="h-2 w-full bg-white/10 rounded-full overflow-hidden flex">
                  <div
                    className="h-full bg-emerald-400 rounded-full transition-all"
                    style={{ width: `${simulationResult.skillCoverageImproved}%` }}
                  />
                </div>
              </div>

              <div className="bg-white/5 rounded-xl p-4 border border-white/10 space-y-2">
                <div className="flex justify-between text-xs">
                  <span className="text-indigo-200 font-semibold">Project & Evidence Relevance</span>
                  <span className="font-bold text-white">
                    {simulationResult.projectRelevanceBase}% &rarr; {simulationResult.projectRelevanceImproved}%
                  </span>
                </div>
                <div className="h-2 w-full bg-white/10 rounded-full overflow-hidden flex">
                  <div
                    className="h-full bg-indigo-400 rounded-full transition-all"
                    style={{ width: `${simulationResult.projectRelevanceImproved}%` }}
                  />
                </div>
              </div>
            </div>
          </Card>

          {/* Positive Changes & Regressions Detection */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card className="border-emerald-200 bg-emerald-50/20 space-y-3">
              <h3 className="font-bold text-sm text-emerald-900 flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Detected Positive Improvements ({simulationResult.positiveChanges?.length || 0})
              </h3>
              {(!simulationResult.positiveChanges || simulationResult.positiveChanges.length === 0) ? (
                <p className="text-xs text-slate-500 italic">No significant positive delta detected.</p>
              ) : (
                <ul className="space-y-2 text-xs text-slate-700">
                  {simulationResult.positiveChanges.map((change, cIdx) => (
                    <li key={cIdx} className="flex items-start gap-2">
                      <span className="text-emerald-600 font-bold">&check;</span>
                      <span>{change}</span>
                    </li>
                  ))}
                </ul>
              )}
            </Card>

            <Card className="border-amber-200 bg-amber-50/20 space-y-3">
              <h3 className="font-bold text-sm text-amber-900 flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 text-amber-600" />
                Detected Regressions & Warnings ({simulationResult.regressions?.length || 0})
              </h3>
              {(!simulationResult.regressions || simulationResult.regressions.length === 0) ? (
                <p className="text-xs text-slate-500 italic">
                  Zero regressions detected. Revisions maintained or improved all qualifications cleanly!
                </p>
              ) : (
                <ul className="space-y-2 text-xs text-slate-700">
                  {simulationResult.regressions.map((reg, rIdx) => (
                    <li key={rIdx} className="flex items-start gap-2">
                      <span className="text-rose-600 font-bold">&excl;</span>
                      <span>{reg}</span>
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          </div>
        </div>
      )}

      {/* Historical Simulations Table */}
      {previousSimulations.length > 0 && (
        <Card className="space-y-3">
          <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <Award className="w-4 h-4 text-indigo-600" />
            Previous Simulation History
          </h3>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="border-b border-slate-200 text-slate-500 uppercase font-semibold">
                <tr>
                  <th className="py-2.5 px-3">Target Job</th>
                  <th className="py-2.5 px-3">Baseline</th>
                  <th className="py-2.5 px-3">Improved</th>
                  <th className="py-2.5 px-3">Base Score</th>
                  <th className="py-2.5 px-3">Improved Score</th>
                  <th className="py-2.5 px-3">Delta</th>
                  <th className="py-2.5 px-3">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {previousSimulations.map((sim) => (
                  <tr key={sim.id} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3 font-semibold text-slate-900">{sim.jobTitle}</td>
                    <td className="py-2.5 px-3">{sim.baseVersionName}</td>
                    <td className="py-2.5 px-3 font-medium text-indigo-600">{sim.improvedVersionName}</td>
                    <td className="py-2.5 px-3">{sim.baseScore}%</td>
                    <td className="py-2.5 px-3 font-bold text-emerald-600">{sim.improvedScore}%</td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`font-extrabold ${
                          sim.scoreDelta >= 0 ? 'text-emerald-600' : 'text-rose-600'
                        }`}
                      >
                        {sim.scoreDelta >= 0 ? '+' : ''}
                        {sim.scoreDelta}%
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-slate-400">
                      {sim.createdAt ? new Date(sim.createdAt).toLocaleDateString() : 'Recent'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
}
