import re
from typing import List
from app.schemas.resume_schema import SkillItem
from app.skill_extractor.skill_dictionary import SKILL_CATEGORIES

def extract_skills(text: str) -> List[SkillItem]:
    """
    Extracts categorized skills from resume text.
    Uses regex boundary matching to prevent substring false-positives.
    """
    found_skills = []
    seen_names = set()
    lowered_text = text.lower()

    for category, skills in SKILL_CATEGORIES.items():
        for skill in skills:
            skill_lower = skill.lower()
            
            # Words with special characters like C++, C#, .NET
            if skill_lower in ["c++", "c#", ".net", ".net core"]:
                pattern = r'(?:^|[\s,;:\(\)\/\-])' + re.escape(skill_lower) + r'(?:[\s,;:\(\)\/\-]|$)'
            elif len(skill_lower) <= 2:
                # E.g. "c", "r", "go" - must have strict boundary
                pattern = r'\b' + re.escape(skill_lower) + r'\b'
            else:
                pattern = r'\b' + re.escape(skill_lower) + r'\b'

            if re.search(pattern, lowered_text, re.IGNORECASE):
                key = skill.lower()
                if key not in seen_names:
                    seen_names.add(key)
                    found_skills.append(SkillItem(name=skill, category=category))

    return found_skills
