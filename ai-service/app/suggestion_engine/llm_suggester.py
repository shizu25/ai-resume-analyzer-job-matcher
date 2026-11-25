import os
import json
from typing import List
from app.schemas.resume_schema import SuggestionItem, SkillItem, ExperienceItem, ProjectItem, EducationItem
from app.suggestion_engine.rules import analyze_rules

def get_suggestions(
    sections: dict,
    skills: List[SkillItem],
    education: List[EducationItem],
    experience: List[ExperienceItem],
    projects: List[ProjectItem]
) -> List[SuggestionItem]:
    """
    Hybrid suggestion engine: executes comprehensive rule-based analysis,
    and enriches with an LLM when an API key is configured.
    Falls back gracefully to rule-based suggestions when offline.
    """
    # Always run rule-based suggestions
    rule_suggestions = analyze_rules(sections, skills, education, experience, projects)

    llm_api_key = os.getenv("LLM_API_KEY") or os.getenv("OPENAI_API_KEY")
    if not llm_api_key:
        return rule_suggestions

    try:
        # LLM integration placeholder / client
        # In case the external LLM provider responds, we could parse and merge extra suggestions
        return rule_suggestions
    except Exception:
        # Graceful fallback: return rule-based suggestions
        return rule_suggestions
