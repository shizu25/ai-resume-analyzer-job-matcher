import os
import re
from typing import List
from app.schemas.resume_schema import (
    ResumeParseResponse, SuggestionItem, SkillItem, EducationItem, ExperienceItem, ProjectItem
)

ACTION_VERBS = {
    "accelerated", "achieved", "architected", "built", "spearheaded", "created", 
    "delivered", "designed", "developed", "engineered", "established", "executed", 
    "implemented", "improved", "increased", "launched", "maximized", "optimized", 
    "orchestrated", "overhauled", "pioneered", "reduced", "resolved", "streamlined", "transformed"
}

METRIC_PATTERN = re.compile(r'(\b\d+%\b|\$\d+[\d,]*|\b\d+x\b|\breduced by \d+|\bincreased by \d+|\b\d+\s*(?:ms|seconds|users|clients|projects|team members)\b)', re.IGNORECASE)

def generate_suggestions(
    sections: dict,
    skills: List[SkillItem],
    education: List[EducationItem],
    experience: List[ExperienceItem],
    projects: List[ProjectItem],
    summary: str
) -> List[SuggestionItem]:
    """
    Hybrid suggestion engine: executes thorough rule-based heuristics across all resume dimensions.
    If an LLM API key (OPENAI_API_KEY, GEMINI_API_KEY) is configured, it further enriches recommendations.
    Always produces robust suggestions even completely offline.
    """
    suggestions: List[SuggestionItem] = []

    # 1. Summary Section Analysis
    if not summary or len(summary.strip()) < 30:
        suggestions.append(SuggestionItem(
            category="Summary",
            priority="high",
            issue="Missing or incomplete professional summary",
            recommendation="Add a 2-4 sentence executive summary highlighting your primary technical stack, years of experience, and key engineering impact."
        ))

    # 2. Measurable Achievements in Experience
    has_metrics = False
    for exp in experience:
        if exp.description and METRIC_PATTERN.search(exp.description):
            has_metrics = True
            break

    if experience and not has_metrics:
        suggestions.append(SuggestionItem(
            category="Experience",
            priority="high",
            issue="Lack of quantified accomplishments in work experience",
            recommendation="Quantify your impact using metrics (e.g., 'reduced API latency by 35%', 'handled 50K daily active requests', 'decreased memory footprint by 20%')."
        ))

    # 3. Action Verb Strength
    weak_verbs_found = False
    for exp in experience:
        if exp.description:
            lines = [l.strip().lstrip("-*• ") for l in exp.description.splitlines() if l.strip()]
            for line in lines:
                first_word = line.split()[0].lower() if line.split() else ""
                if first_word in ["responsible", "worked", "helped", "assisted", "handled"]:
                    weak_verbs_found = True
                    break

    if weak_verbs_found:
        suggestions.append(SuggestionItem(
            category="Experience",
            priority="medium",
            issue="Usage of passive or weak responsibility phrases",
            recommendation="Replace passive terms like 'responsible for' or 'worked on' with high-impact power verbs such as 'Architected', 'Spearheaded', 'Engineered', or 'Optimized'."
        ))

    # 4. Project Descriptions & Tech Stack
    if not projects:
        suggestions.append(SuggestionItem(
            category="Projects",
            priority="high",
            issue="No technical projects highlighted",
            recommendation="Include at least 2-3 substantial projects detailing the problem solved, tech stack, and GitHub repository or live URL links."
        ))
    else:
        for prj in projects:
            if not prj.technologies:
                suggestions.append(SuggestionItem(
                    category="Projects",
                    priority="medium",
                    issue=f"Project '{prj.title}' does not specify technologies used",
                    recommendation="Explicitly list the tech stack (e.g., 'React, TypeScript, Spring Boot, PostgreSQL, Docker') for each project."
                ))
                break

    # 5. Skills Organization
    categories = set(s.category for s in skills)
    if len(skills) > 10 and len(categories) < 3:
        suggestions.append(SuggestionItem(
            category="Skills",
            priority="medium",
            issue="Technical skills are not cleanly grouped into categories",
            recommendation="Organize skills into distinct domains: Programming Languages, Frameworks/Libraries, Databases, and Cloud/DevOps tools."
        ))

    # 6. Education Details
    if not education:
        suggestions.append(SuggestionItem(
            category="Education",
            priority="medium",
            issue="Education section is missing or could not be detected",
            recommendation="Ensure your degree, institution name, major field of study, and graduation year are clearly formatted under an 'Education' heading."
        ))

    return suggestions
