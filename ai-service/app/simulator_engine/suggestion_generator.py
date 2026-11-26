import re
from typing import Dict, List, Optional
from app.schemas.resume_schema import ImprovementSuggestionSchema, ImprovementSuggestionsRequest

WEAK_VERBS = ["worked on", "helped", "assisted", "responsible for", "handled", "participated in", "involved in", "did"]
STRONG_VERBS = ["Engineered", "Developed", "Architected", "Implemented", "Designed", "Built", "Optimized", "Automated"]

def generate_suggestions(request: ImprovementSuggestionsRequest) -> List[ImprovementSuggestionSchema]:
    suggestions: List[ImprovementSuggestionSchema] = []
    content = request.resume_content or {}
    
    projects = content.get("projects", [])
    experiences = content.get("experience", [])
    skills = content.get("skills", [])
    summary = content.get("summary", "")

    skill_names = set()
    if isinstance(skills, list):
        for s in skills:
            if isinstance(s, dict):
                skill_names.add(s.get("name", "").lower())
            elif isinstance(s, str):
                skill_names.add(s.lower())

    required_skills = [r.strip() for r in request.required_skills if r.strip()]

    # 1. Evaluate Project Descriptions for weak action verbs and clarity
    for proj in projects:
        if isinstance(proj, dict):
            title = proj.get("title", "Project")
            desc = proj.get("description", "")
            tech = proj.get("technologies", "")
            
            lines = [l.strip() for l in desc.splitlines() if l.strip()] if desc else [title]
            for line in lines:
                line_lower = line.lower()
                # Check for weak verb
                for weak in WEAK_VERBS:
                    if weak in line_lower:
                        # Find related requirement
                        rel_req = next((r for r in required_skills if r.lower() in line_lower or (tech and r.lower() in tech.lower())), None)
                        strong_verb = STRONG_VERBS[hash(line) % len(STRONG_VERBS)]
                        improved = re.sub(rf'\b{re.escape(weak)}\b', strong_verb.lower(), line, flags=re.IGNORECASE)
                        if improved == line:
                            improved = f"{strong_verb} {line.lstrip('•- *')}"
                        else:
                            # Capitalize first word
                            improved = improved[0].upper() + improved[1:] if improved else line

                        suggestions.append(ImprovementSuggestionSchema(
                            category="WEAK_PROJECT_DESCRIPTION",
                            original_text=line,
                            suggested_text=improved,
                            issue=f"Passive phrasing with weak phrase '{weak}'; minimizes your individual impact.",
                            reason="Strong action verbs clearly convey your personal technical contribution and project ownership.",
                            related_requirement=rel_req or "Technical Delivery",
                            expected_benefit="Increases ATS impact score and captures recruiter attention during visual scanning."
                        ))
                        break

                # Check for short, unelaborated project bullet (< 35 chars)
                if len(line) < 40 and not any(line.startswith(c) for c in ["http", "github"]):
                    if tech:
                        suggested = f"{line.rstrip('.')} using {tech}."
                        suggestions.append(ImprovementSuggestionSchema(
                            category="UNCLEAR_CONTRIBUTION",
                            original_text=line,
                            suggested_text=suggested,
                            issue="Project bullet point is very brief and does not clearly specify technical components or outcome.",
                            reason="Contextualizing the tools and framework utilized demonstrates practical software engineering depth.",
                            related_requirement=tech.split(",")[0] if tech else "Technical Breadth",
                            expected_benefit="Provides concrete evidence of tooling and implementation capability."
                        ))

    # 2. Evaluate Experience Descriptions
    for exp in experiences:
        if isinstance(exp, dict):
            company = exp.get("company", "Company")
            desc = exp.get("description", "")
            if desc:
                for line in desc.splitlines():
                    cleaned = line.strip()
                    if not cleaned:
                        continue
                    cleaned_lower = cleaned.lower()
                    for weak in ["responsible for", "handled", "worked on"]:
                        if weak in cleaned_lower:
                            strong_verb = "Spearheaded" if "lead" in company.lower() else "Delivered"
                            improved = re.sub(rf'\b{re.escape(weak)}\b', strong_verb.lower(), cleaned, flags=re.IGNORECASE)
                            improved = improved[0].upper() + improved[1:] if improved else cleaned
                            suggestions.append(ImprovementSuggestionSchema(
                                category="UNCLEAR_CONTRIBUTION",
                                original_text=cleaned,
                                suggested_text=improved,
                                issue=f"Task-oriented description using '{weak}' rather than outcome-oriented achievement.",
                                reason="Highlighting outcomes over task assignments distinguishes top candidates.",
                                related_requirement="Core Engineering Responsibilities",
                                expected_benefit="Presents your role proactively rather than as a passive participant."
                            ))
                            break

    # 3. Check for Skills listed without evidence
    proj_text_blob = " ".join([
        (p.get("description", "") + " " + p.get("technologies", "") + " " + p.get("title", ""))
        for p in projects if isinstance(p, dict)
    ]).lower()
    exp_text_blob = " ".join([
        (e.get("description", "") + " " + e.get("position", "") + " " + e.get("company", ""))
        for e in experiences if isinstance(e, dict)
    ]).lower()

    for req in required_skills:
        req_norm = req.lower()
        if req_norm in skill_names:
            if req_norm not in proj_text_blob and req_norm not in exp_text_blob:
                suggestions.append(ImprovementSuggestionSchema(
                    category="MISSING_SKILL_EVIDENCE",
                    original_text=f"Skills: {req}",
                    suggested_text=f"Incorporate a bullet in your projects or experience demonstrating how you applied {req} (e.g. 'Implemented {req} workflows for data persistence/caching').",
                    issue=f"'{req}' is listed in your skills section but does not appear in your project or experience descriptions.",
                    reason=f"Recruiters and hiring managers look for validated evidence of {req} in realistic projects.",
                    related_requirement=req,
                    expected_benefit=f"Converts '{req}' from a 'mentioned' skill into a verified 'demonstrated' skill."
                ))

    # 4. Check Summary alignment with Job Title
    if summary and request.job_title:
        if request.job_title.lower() not in summary.lower():
            suggestions.append(ImprovementSuggestionSchema(
                category="MISSING_RELEVANT_INFO",
                original_text=summary[:140] + ("..." if len(summary) > 140 else ""),
                suggested_text=f"Target your summary towards {request.job_title} roles to immediately establish relevance.",
                issue=f"Professional summary does not explicitly reference your specialization towards {request.job_title}.",
                reason="Tailoring your objective to the exact target role improves relevance in the first 6 seconds of review.",
                related_requirement=request.job_title,
                expected_benefit="Signals strong role alignment and clear career focus."
            ))

    return suggestions
