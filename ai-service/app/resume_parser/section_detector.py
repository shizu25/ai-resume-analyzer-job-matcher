import re
from typing import Dict, List, Optional, Tuple
from app.resume_parser.semantic_classifier import classify_heading_semantically, classify_content_block_semantically

SECTION_KEYWORDS = {
    "summary": [
        "summary", "professional summary", "profile", "about me", "objective", 
        "career objective", "executive summary", "overview", "personal profile",
        "professional profile", "career profile", "personal statement"
    ],
    "skills": [
        "skills", "technical skills", "core competencies", "technologies", 
        "areas of expertise", "tools and technologies", "tech stack", "programming skills",
        "key skills", "technical proficiencies", "technical expertise", "it skills",
        "skills and competencies", "skills & tools", "technical skills & tools",
        "skills & abilities", "skills and tools", "tools & technologies", "competencies",
        "languages & tools", "computer skills"
    ],
    "experience": [
        "experience", "work experience", "employment history", "professional experience", 
        "work history", "career history", "internship", "internships", "internship experience",
        "industrial training", "summer internship", "vocational training", "apprenticeship",
        "relevant experience", "work experience & employment history", "professional background",
        "industry experience", "practical experience", "work background", "employment",
        "training & internship", "intern"
    ],
    "education": [
        "education", "academic background", "academic qualifications", "qualifications", 
        "degrees", "studies", "education & qualifications", "educational background",
        "education & training", "academic history", "formal education"
    ],
    "projects": [
        "projects", "project", "academic projects", "key projects", "personal projects", 
        "selected projects", "technical projects", "projects & highlights",
        "project experience", "notable projects", "portfolio", "featured projects",
        "software projects", "mini projects", "major projects", "capstone project"
    ],
    "certifications": [
        "certifications", "certification", "certificates", "licenses", "courses",
        "accreditations", "professional certifications", "licenses & certifications",
        "training & certifications", "credentials", "achievements & certifications"
    ],
    "references": [
        "references", "referees", "recommendations", "references & recommendations",
        "professional references", "references available upon request"
    ],
    "awards": [
        "awards", "honors", "achievements", "accomplishments", "scholarships",
        "honors & awards", "awards & achievements", "key achievements"
    ],
    "publications": [
        "publications", "research papers", "patents", "conferences", "published papers"
    ],
    "languages": [
        "languages", "language proficiency", "languages known"
    ],
    "volunteer": [
        "volunteer", "volunteering", "volunteering experiences", "volunteering experience",
        "volunteer experience", "volunteer experiences", "leadership", "community service",
        "extracurricular", "activities", "extracurricular activities"
    ]
}

def is_heading_formatted(line: str) -> bool:
    """
    Checks if a line displays visual heading formatting cues:
    - Preceded by markdown markers (#, ##, **), or
    - All uppercase with 1-6 words, or
    - Short standalone title line (< 60 chars) without ending sentence punctuation.
    """
    cleaned = line.strip()
    if not cleaned or len(cleaned) > 65:
        return False

    if cleaned.startswith(("#", "##", "###", "**")) and len(cleaned.split()) <= 6:
        return True

    words = cleaned.split()
    if len(words) <= 5 and cleaned.isupper() and any(c.isalpha() for c in cleaned):
        return True

    if len(words) <= 5 and not cleaned.endswith((".", ";", ",")):
        if all(w[0].isupper() for w in words if w and w[0].isalpha()):
            return True

    return False

def identify_section_heading(line: str, current_section: str = "") -> Tuple[Optional[str], float]:
    """
    Identifies if a line is a section heading using:
    1. Keyword dictionary matching (including singular forms like INTERNSHIP, PROJECT).
    2. Heading formatting cues + spaCy semantic classification for unfamiliar headings.
    Returns (canonical_section, confidence_score).
    """
    raw = line.strip()
    if not raw or len(raw) > 65:
        return None, 0.0

    cleaned = re.sub(r'^(?:[0-9\.\-\*\#\•\·\>\s]+)', '', raw).strip()
    cleaned = re.sub(r'[\s:\-\|\/]+$', '', cleaned).strip()

    if not cleaned or len(cleaned) < 3:
        return None, 0.0

    lowered = cleaned.lower()

    if any(k in lowered for k in ["university", "college", "institute", "school of", "academy of"]):
        return None, 0.0
    if any(k in lowered for k in ["senior software engineer", "full stack developer", "backend developer", "frontend developer", "project manager"]):
        return None, 0.0

    # 1. Direct Keyword Dictionary Match (Exact and Prefix)
    for sec, keywords in SECTION_KEYWORDS.items():
        for kw in keywords:
            if lowered == kw:
                return sec, 1.0
            pattern = r'^(?:' + re.escape(kw) + r')(?:[\s&/and\-\|\(\)]*[\w\s]{0,20})?$'
            if re.match(pattern, lowered):
                return sec, 0.95

    # 2. Heading Formatting + spaCy Semantic Classification for unfamiliar headings
    if is_heading_formatted(raw):
        semantic_sec, confidence = classify_heading_semantically(cleaned)
        if semantic_sec and confidence >= 0.65 and semantic_sec != current_section:
            return semantic_sec, confidence

    return None, 0.0

def detect_sections(text: str) -> Dict[str, str]:
    """
    Splits resume text into recognized functional sections position-independently.
    """
    lines = text.splitlines()
    sections: Dict[str, List[str]] = {
        "header": [],
        "summary": [],
        "skills": [],
        "experience": [],
        "education": [],
        "projects": [],
        "certifications": [],
        "references": [],
        "awards": [],
        "publications": [],
        "languages": [],
        "volunteer": [],
        "other": []
    }

    current_section = "header"

    for line in lines:
        cleaned_line = line.strip()
        if not cleaned_line:
            # Preserve spacing between items in a section so double-newline separation works
            if current_section != "header" and sections[current_section] and sections[current_section][-1] != "":
                sections[current_section].append("")
            continue

        matched_section, confidence = identify_section_heading(cleaned_line, current_section)
        if matched_section:
            current_section = matched_section
        else:
            sections[current_section].append(cleaned_line)

    if sections["other"]:
        other_text = "\n".join(sections["other"])
        inferred_sec, conf = classify_content_block_semantically(other_text)
        if inferred_sec and conf >= 0.80:
            sections[inferred_sec].extend(sections["other"])
            sections["other"] = []

    return {k: "\n".join(v).strip() for k, v in sections.items()}
