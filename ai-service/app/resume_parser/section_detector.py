import re
from typing import Dict

SECTION_HEADERS = {
    "summary": [
        "summary", "professional summary", "profile", "about me", "objective", 
        "career objective", "executive summary", "overview"
    ],
    "skills": [
        "skills", "technical skills", "core competencies", "technologies", 
        "areas of expertise", "tools and technologies", "tech stack", "programming skills"
    ],
    "experience": [
        "experience", "work experience", "employment history", "professional experience", 
        "work history", "career history", "internships", "relevant experience"
    ],
    "education": [
        "education", "academic background", "academic qualifications", "qualifications", 
        "degrees", "studies"
    ],
    "projects": [
        "projects", "academic projects", "key projects", "personal projects", 
        "selected projects", "technical projects"
    ],
    "certifications": [
        "certifications", "certificates", "licenses", "courses", "accreditations"
    ]
}

def detect_sections(text: str) -> Dict[str, str]:
    """
    Splits resume text into recognized functional sections.
    Returns a dictionary mapping section name to section text.
    """
    lines = text.splitlines()
    sections: Dict[str, list] = {
        "header": [],
        "summary": [],
        "skills": [],
        "experience": [],
        "education": [],
        "projects": [],
        "certifications": [],
        "other": []
    }

    current_section = "header"

    header_patterns = {}
    for sec, keywords in SECTION_HEADERS.items():
        pattern = r'^(?:[0-9\.\-\*\#\s]*)(?:' + '|'.join(re.escape(k) for k in keywords) + r')(?:\s*[:\-\|]?\s*)$'
        header_patterns[sec] = re.compile(pattern, re.IGNORECASE)

    for line in lines:
        cleaned_line = line.strip()
        if not cleaned_line:
            continue

        # Check if line matches a known section header
        matched_section = None
        for sec, pattern in header_patterns.items():
            if pattern.match(cleaned_line):
                matched_section = sec
                break

        if matched_section:
            current_section = matched_section
        else:
            sections[current_section].append(cleaned_line)

    return {k: "\n".join(v).strip() for k, v in sections.items()}
