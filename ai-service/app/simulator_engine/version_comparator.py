import re
from typing import Dict, List, Set, Tuple
from app.schemas.resume_schema import SimulationCompareRequest, SimulationCompareResponse

REQUIRED_WEIGHT = 0.80
PREFERRED_WEIGHT = 0.20

def normalize(skill: str) -> str:
    return skill.strip().lower() if skill else ""

def extract_skill_set(content: dict) -> Set[str]:
    skills = set()
    raw_skills = content.get("skills", [])
    if isinstance(raw_skills, list):
        for s in raw_skills:
            if isinstance(s, dict):
                name = s.get("name", "")
                if name:
                    skills.add(normalize(name))
            elif isinstance(s, str):
                if s.strip():
                    skills.add(normalize(s))
    return skills

def extract_content_text_blob(content: dict) -> str:
    parts = []
    if content.get("summary"):
        parts.append(str(content["summary"]))
    for exp in content.get("experience", []):
        if isinstance(exp, dict):
            parts.append(str(exp.get("company", "")) + " " + str(exp.get("position", "")) + " " + str(exp.get("description", "")))
    for proj in content.get("projects", []):
        if isinstance(proj, dict):
            parts.append(str(proj.get("title", "")) + " " + str(proj.get("description", "")) + " " + str(proj.get("technologies", "")))
    return " ".join(parts).lower()

def compute_metrics(content: dict, required_skills: List[str], preferred_skills: List[str]) -> Tuple[float, float, float]:
    """
    Returns (overall_score, skill_coverage_pct, project_relevance_pct)
    """
    resume_skills = extract_skill_set(content)
    text_blob = extract_content_text_blob(content)

    # 1. Required skills match
    req_matched = 0
    for req in required_skills:
        norm = normalize(req)
        if norm in resume_skills or norm in text_blob:
            req_matched += 1

    req_pct = (req_matched * 100.0 / len(required_skills)) if required_skills else 100.0

    # 2. Preferred skills match
    pref_matched = 0
    for pref in preferred_skills:
        norm = normalize(pref)
        if norm in resume_skills or norm in text_blob:
            pref_matched += 1

    pref_pct = (pref_matched * 100.0 / len(preferred_skills)) if preferred_skills else 0.0

    # 3. Overall Job Compatibility Score (using the standard 80/20 weights)
    if preferred_skills:
        overall_score = round((REQUIRED_WEIGHT * req_pct) + (PREFERRED_WEIGHT * pref_pct), 1)
    else:
        overall_score = round(req_pct, 1)

    # 4. Project Relevance Score: percentage of required skills explicitly demonstrated in projects/experience
    proj_exp_blob = " ".join([
        (p.get("description", "") + " " + p.get("technologies", "") + " " + p.get("title", ""))
        for p in content.get("projects", []) if isinstance(p, dict)
    ] + [
        (e.get("description", "") + " " + e.get("position", ""))
        for e in content.get("experience", []) if isinstance(e, dict)
    ]).lower()

    demonstrated_count = sum(1 for req in required_skills if normalize(req) in proj_exp_blob)
    project_relevance = round((demonstrated_count * 100.0 / len(required_skills)) if required_skills else 100.0, 1)

    skill_coverage = round(req_pct, 1)
    return overall_score, skill_coverage, project_relevance

def compare_versions(request: SimulationCompareRequest) -> SimulationCompareResponse:
    req_skills = [r.strip() for r in request.required_skills if r.strip()]
    pref_skills = [p.strip() for p in request.preferred_skills if p.strip()]

    score_a, cov_a, rel_a = compute_metrics(request.version_a_content, req_skills, pref_skills)
    score_b, cov_b, rel_b = compute_metrics(request.version_b_content, req_skills, pref_skills)

    delta = round(score_b - score_a, 1)

    skills_a = extract_skill_set(request.version_a_content)
    skills_b = extract_skill_set(request.version_b_content)

    blob_a = extract_content_text_blob(request.version_a_content)
    blob_b = extract_content_text_blob(request.version_b_content)

    positive_changes: List[str] = []
    regressions: List[str] = []

    # Check newly demonstrated required skills
    for req in req_skills:
        norm = normalize(req)
        in_a = norm in skills_a or norm in blob_a
        in_b = norm in skills_b or norm in blob_b

        if in_b and not in_a:
            positive_changes.append(f"Added required skill '{req}' with supporting context.")
        elif in_a and not in_b:
            regressions.append(f"Removed required skill '{req}', reducing match coverage.")

    # Check project clarity & length
    projs_a = request.version_a_content.get("projects", [])
    projs_b = request.version_b_content.get("projects", [])

    if len(projs_b) > len(projs_a):
        positive_changes.append(f"Added {len(projs_b) - len(projs_a)} new project portfolio item(s).")
    elif len(projs_b) < len(projs_a):
        regressions.append(f"Removed {len(projs_a) - len(projs_b)} project(s).")

    if rel_b > rel_a:
        positive_changes.append(f"Project relevance improved by {round(rel_b - rel_a, 1)}% through clearer technical articulation.")
    elif rel_b < rel_a:
        regressions.append(f"Project relevance dropped by {round(rel_a - rel_b, 1)}% due to loss of key technical descriptions.")

    # Build narrative explanation
    if delta > 0:
        pos_summary = "; ".join(positive_changes[:2]) if positive_changes else "improved content alignment"
        explanation = (
            f"Compatibility with '{request.job_title}' increased by +{delta}% (from {score_a}% to {score_b}%). "
            f"Key factors: {pos_summary}. Project relevance rose to {rel_b}%."
        )
    elif delta < 0:
        reg_summary = "; ".join(regressions[:2]) if regressions else "reduced required skill coverage"
        explanation = (
            f"Compatibility with '{request.job_title}' decreased by {delta}% (from {score_a}% to {score_b}%). "
            f"Warning: {reg_summary}. Ensure vital technical evidence was not inadvertently omitted."
        )
    else:
        explanation = (
            f"Compatibility with '{request.job_title}' remained unchanged at {score_a}%. "
            f"The edits did not alter core required skill coverage ({cov_a}%) or project relevance ({rel_a}%)."
        )

    return SimulationCompareResponse(
        base_score=score_a,
        improved_score=score_b,
        score_delta=delta,
        skill_coverage_base=cov_a,
        skill_coverage_improved=cov_b,
        project_relevance_base=rel_a,
        project_relevance_improved=rel_b,
        explanation=explanation,
        positive_changes=positive_changes,
        regressions=regressions
    )
