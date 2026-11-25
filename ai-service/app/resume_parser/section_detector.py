import re
from typing import Dict, List, Optional

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
        "skills & abilities", "skills and tools", "tools & technologies", "competencies"
    ],
    "experience": [
        "experience", "work experience", "employment history", "professional experience", 
        "work history", "career history", "internships", "relevant experience",
        "work experience & employment history", "professional background", "industry experience",
        "practical experience", "work background", "employment"
    ],
    "education": [
        "education", "academic background", "academic qualifications", "qualifications", 
        "degrees", "studies", "education & qualifications", "educational background",
        "education & training", "academic history", "formal education"
    ],
    "projects": [
        "projects", "academic projects", "key projects", "personal projects", 
        "selected projects", "technical projects", "projects & highlights",
        "project experience", "notable projects", "portfolio", "featured projects",
        "software projects"
    ],
    "certifications": [
        "certifications", "certificates", "licenses", "courses", "accreditations",
        "professional certifications", "licenses & certifications", "training & certifications",
        "credentials"
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
        "volunteer", "volunteering", "leadership", "community service", "extracurricular",
        "activities", "extracurricular activities"
    ]
}

def is_section_header(line: str) -> Optional[str]:
    """
    Checks if a line looks like a resume section header.
    Returns the section category or None.
    """
    raw = line.strip()
    if not raw or len(raw) > 65:
        return None

    # Strip leading markdown, numbering, bullet points, and trailing punctuation
    cleaned = re.sub(r'^(?:[0-9\.\-\*\#\•\·\>\s]+)', '', raw).strip()
    cleaned = re.sub(r'[\s:\-\|\/]+$', '', cleaned).strip()
    
    if not cleaned or len(cleaned) < 3:
        return None

    lowered = cleaned.lower()

    # Match against keywords
    for sec, keywords in SECTION_KEYWORDS.items():
        for kw in keywords:
            # Exact match or match with common delimiters (&, and, /, -)
            if lowered == kw:
                return sec
            # Allow variations like "Technical Skills & Tools", "Work Experience / History"
            pattern = r'^(?:' + re.escape(kw) + r')(?:[\s&/and\-\|\(\)]*[\w\s]{0,20})?$'
            if re.match(pattern, lowered):
                return sec

    return None

def detect_sections(text: str) -> Dict[str, str]:
    """
    Splits resume text into recognized functional sections.
    Returns a dictionary mapping section name to section text.
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
            continue

        matched_section = is_section_header(cleaned_line)
        if matched_section:
            current_section = matched_section
        else:
            sections[current_section].append(cleaned_line)

    return {k: "\n".join(v).strip() for k, v in sections.items()}
