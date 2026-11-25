import re
from typing import List, Dict
from app.schemas.resume_schema import SuggestionItem, SkillItem, ExperienceItem, ProjectItem, EducationItem

METRIC_PATTERN = re.compile(r"\b(?:\d+%(?: reduction| increase| boost)?|\$\d+[\d,]*|\d+\+? (?:users|clients|requests|ms|seconds|minutes|hours|days|percent|x faster|team members))\b", re.IGNORECASE)

WEAK_VERBS = ["helped", "assisted", "worked on", "handled", "responsible for", "participated in", "did"]
STRONG_VERBS = ["Architected", "Engineered", "Spearheaded", "Implemented", "Optimized", "Automated", "Delivered", "Accelerated"]

def analyze_rules(
    sections: Dict[str, str],
    skills: List[SkillItem],
    education: List[EducationItem],
    experience: List[ExperienceItem],
    projects: List[ProjectItem]
) -> List[SuggestionItem]:
    suggestions: List[SuggestionItem] = []

    # 1. Check Missing Sections
    if not sections.get("summary"):
        suggestions.append(SuggestionItem(
            category="Summary",
            priority="high",
            issue="Missing Professional Summary",
            recommendation="Add a compelling 2-3 sentence professional summary at the top highlighting your years of experience, core technical stack, and domain focus."
        ))

    if not projects and not experience:
        suggestions.append(SuggestionItem(
            category="Experience",
            priority="high",
            issue="No Work Experience or Projects Identified",
            recommendation="Include detailed professional experience or technical portfolio projects showcasing hands-on implementations."
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
            issue="Bullet points lack quantified impact or measurable outcomes",
            recommendation="Quantify your achievements using specific metrics (e.g., 'Reduced API latency by 35%', 'Scaled service to handle 10k concurrent users')."
        ))

    # 3. Weak Action Verbs
    for exp in experience:
        if exp.description:
            lowered = exp.description.lower()
            for weak in WEAK_VERBS:
                if weak in lowered:
                    suggestions.append(SuggestionItem(
                        category="Action Verbs",
                        priority="medium",
                        issue=f"Passive language detected: '{weak}'",
                        recommendation=f"Replace passive phrasing with dynamic action verbs such as {', '.join(STRONG_VERBS[:4])}."
                    ))
                    break

    # 4. Skill Organization & Diversity
    if skills:
        categories = set(s.category for s in skills)
        if len(categories) <= 1 and len(skills) > 5:
            suggestions.append(SuggestionItem(
                category="Skills",
                priority="medium",
                issue="Technical skills are not grouped by domain",
                recommendation="Organize your skills section into categorized domains (e.g., Programming Languages, Frameworks, Cloud & DevOps, Databases) for recruiter readability."
            ))
    else:
        suggestions.append(SuggestionItem(
            category="Skills",
            priority="high",
            issue="No recognizable technical skills found",
            recommendation="Explicitly list the primary tools, programming languages, and frameworks relevant to your target engineering roles."
        ))

    # 5. Project Quality & Links
    for proj in projects:
        if not proj.link:
            suggestions.append(SuggestionItem(
                category="Projects",
                priority="low",
                issue=f"Project '{proj.title}' has no repository or live demo link",
                recommendation="Provide a GitHub repository link or live deployment URL to allow recruiters and interviewers to review your codebase."
            ))
            break

    return suggestions
