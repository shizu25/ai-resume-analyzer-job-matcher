import re
from typing import List, Set
from app.schemas.resume_schema import SkillItem
from app.skill_extractor.skill_dictionary import SKILL_CATEGORIES

# Semester pattern to exclude academic dates from tech skill matching (e.g. "Spring 2022")
SEMESTER_PATTERN = re.compile(r'\b(?:Spring|Fall|Summer|Winter)\s+(?:19\d\d|20\d\d)\b', re.IGNORECASE)

# Ambiguous words that need explicit technical qualifiers when appearing in prose outside the Skills section
AMBIGUOUS_TECH_PATTERNS = {
    "spring": re.compile(r'\b(?:Spring\s+(?:Boot|Framework|MVC|Security|Data|Cloud))\b', re.IGNORECASE),
    "express": re.compile(r'\b(?:Express(?:\.js|js)?|Node(?:\.js)?\s*/\s*Express)\b', re.IGNORECASE),
    "go": re.compile(r'\b(?:Golang|Go\s+language|Go\s*/\s*Python|Python\s*/\s*Go|in\s+Go\b)\b', re.IGNORECASE),
    "c": re.compile(r'\b(?:C\s*/\s*C\+\+|C\+\+|C\s+language)\b', re.IGNORECASE),
    "r": re.compile(r'\b(?:R\s+language|R\s+programming|R\s*/\s*Python|Python\s*/\s*R)\b', re.IGNORECASE),
    "spark": re.compile(r'\b(?:Apache\s+Spark|PySpark|Spark\s+SQL)\b', re.IGNORECASE),
    "dart": re.compile(r'\b(?:Dart\s+language|Flutter\s*/\s*Dart|Dart\s*/\s*Flutter)\b', re.IGNORECASE),
    "rust": re.compile(r'\b(?:Rust\s+language|Rust\s+programming|in\s+Rust)\b', re.IGNORECASE),
    "echo": re.compile(r'\b(?:Echo\s+framework|Gin\s*/\s*Echo)\b', re.IGNORECASE),
    "gin": re.compile(r'\b(?:Gin\s+framework|Gin\s+web)\b', re.IGNORECASE),
    "koa": re.compile(r'\b(?:Koa(?:\.js)?\s+framework)\b', re.IGNORECASE),
    "git": re.compile(r'\b(?:Git|GitHub|GitLab)\b', re.IGNORECASE),
}

def extract_skills(skills_section_text: str, full_text: str = "") -> List[SkillItem]:
    """
    Extracts categorized skills with confidence scores and evidence snippets.
    - Inside dedicated skills section: 1.0 confidence.
    - In general resume text with contextual match: 0.85 confidence.
    """
    found_skills: List[SkillItem] = []
    seen_names: Set[str] = set()

    # 1. Process dedicated skills section first (100% confidence zone)
    if skills_section_text and len(skills_section_text.strip()) > 5:
        clean_section = SEMESTER_PATTERN.sub('', skills_section_text)
        section_lines = [l.strip() for l in clean_section.splitlines() if l.strip()]

        for category, skills in SKILL_CATEGORIES.items():
            for skill in skills:
                skill_lower = skill.lower()

                if skill_lower in ["c++", "c#", ".net", ".net core"]:
                    pattern = r'(?:^|[\s,;:\(\)\/\-])' + re.escape(skill_lower) + r'(?:[\s,;:\(\)\/\-]|$)'
                elif len(skill_lower) <= 2:
                    pattern = r'(?:^|[\s,;:\(\)\/\-])' + re.escape(skill_lower) + r'(?:[\s,;:\(\)\/\-]|$)'
                else:
                    pattern = r'\b' + re.escape(skill_lower) + r'\b'

                for s_line in section_lines:
                    if re.search(pattern, s_line, re.IGNORECASE):
                        key = skill.lower()
                        if key not in seen_names:
                            seen_names.add(key)
                            found_skills.append(SkillItem(
                                name=skill,
                                category=category,
                                confidence=0.98,
                                evidence=s_line[:120]
                            ))
                        break

    # 2. Extract from remaining resume text (experience, summary, projects) with strict guardrails
    if full_text:
        clean_body = SEMESTER_PATTERN.sub('', full_text)
        body_lines = [l.strip() for l in clean_body.splitlines() if l.strip()]

        for category, skills in SKILL_CATEGORIES.items():
            for skill in skills:
                key = skill.lower()
                if key in seen_names:
                    continue

                for b_line in body_lines:
                    matched = False
                    if key in AMBIGUOUS_TECH_PATTERNS:
                        strict_re = AMBIGUOUS_TECH_PATTERNS[key]
                        if strict_re.search(b_line):
                            matched = True
                    elif key in ["c++", "c#", ".net", ".net core"]:
                        pattern = r'(?:^|[\s,;:\(\)\/\-])' + re.escape(key) + r'(?:[\s,;:\(\)\/\-]|$)'
                        if re.search(pattern, b_line, re.IGNORECASE):
                            matched = True
                    elif len(key) <= 2:
                        continue
                    else:
                        pattern = r'\b' + re.escape(key) + r'\b'
                        if re.search(pattern, b_line, re.IGNORECASE):
                            matched = True

                    if matched:
                        seen_names.add(key)
                        found_skills.append(SkillItem(
                            name=skill,
                            category=category,
                            confidence=0.85,
                            evidence=b_line[:120]
                        ))
                        break

    return found_skills
