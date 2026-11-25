import re
from typing import List, Optional
from app.schemas.resume_schema import EducationItem

DEGREE_PATTERNS = [
    r"\b(?:Bachelor(?:\s+of\s+[\w\s]+)?|B\.?S\.?|B\.?Tech|B\.?E\.?|B\.?A\.?|BCA|BBA)\b[^\n,]*",
    r"\b(?:Master(?:\s+of\s+[\w\s]+)?|M\.?S\.?|M\.?Tech|M\.?E\.?|M\.?A\.?|MBA|MCA)\b[^\n,]*",
    r"\b(?:Doctor(?:\s+of\s+[\w\s]+)?|Ph\.?D\.?|Doctorate)\b[^\n,]*",
    r"\b(?:Associate(?:\s+Degree|\s+of\s+[\w\s]+)?|A\.?S\.?|A\.?A\.?)\b[^\n,]*",
    r"\b(?:Diploma|Post\s+Graduate\s+Diploma|PG\s+Diploma|High\s+School(?:\s+Diploma)?)\b[^\n,]*",
]

YEAR_PATTERN = re.compile(r"\b(19\d\d|20\d\d)\b")
GRADE_PATTERN = re.compile(r"(?:GPA|CGPA|Grade|Percentage)[\s:]*([0-9\.]+(?:\s*\/\s*[0-9\.]+)?%?)", re.IGNORECASE)

INSTITUTION_KEYWORDS = [
    "university", "college", "institute", "school", "academy", "polytechnic", 
    "campus", "faculty", "iit", "nit", "bits", "mit", "stanford", "berkeley"
]

NON_INSTITUTION_KEYWORDS = [
    "engineer", "developer", "intern", "manager", "lead", "architect", 
    "reference", "referee", "available upon request", "january", "february", 
    "march", "april", "may", "june", "july", "august", "september", "october", 
    "november", "december", "present", "current", "github", "http"
]

def extract_education(section_text: str, full_text: str) -> List[EducationItem]:
    """
    Extracts education history strictly from the education section when available.
    Includes evidence snippets and confidence scores.
    """
    has_dedicated_section = bool(section_text and len(section_text.strip()) > 10)
    target_text = section_text if has_dedicated_section else full_text
    items: List[EducationItem] = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    for i, line in enumerate(lines):
        lowered_line = line.lower()
        if any(bad in lowered_line for bad in ["reference", "referee", "available upon request"]):
            continue
        if any(role in lowered_line for role in ["senior software engineer", "full stack developer", "backend developer", "product manager"]):
            continue

        for pattern in DEGREE_PATTERNS:
            match = re.search(pattern, line, re.IGNORECASE)
            if match:
                degree_raw = match.group(0).strip()
                degree_raw = re.sub(r'[\s\|\-\:\,]+$', '', degree_raw).strip()

                search_text = line
                if i + 1 < len(lines):
                    search_text += " " + lines[i+1]
                if i + 2 < len(lines):
                    search_text += " " + lines[i+2]

                years = YEAR_PATTERN.findall(search_text)
                start_yr = None
                end_yr = None
                if len(years) >= 2:
                    start_yr = int(years[0])
                    end_yr = int(years[1])
                elif len(years) == 1:
                    end_yr = int(years[0])

                degree_text = degree_raw
                field = None
                if " in " in degree_raw.lower():
                    parts = re.split(r'\s+in\s+', degree_raw, flags=re.IGNORECASE, maxsplit=1)
                    degree_text = parts[0].strip().title()
                    field = parts[1].strip().title()

                institution = None
                delimiters = [" - ", " | ", ", ", " at "]
                for delim in delimiters:
                    if delim in line:
                        parts = line.split(delim)
                        for part in parts:
                            p_clean = part.strip()
                            if any(k in p_clean.lower() for k in INSTITUTION_KEYWORDS):
                                institution = p_clean
                                break
                    if institution:
                        break

                if not institution:
                    candidates = []
                    if i > 0:
                        candidates.append(lines[i-1])
                    if i + 1 < len(lines):
                        candidates.append(lines[i+1])

                    for cand in candidates:
                        cand_lower = cand.lower()
                        if any(bad in cand_lower for bad in NON_INSTITUTION_KEYWORDS):
                            continue
                        if any(k in cand_lower for k in INSTITUTION_KEYWORDS) or len(cand.split()) <= 6:
                            institution = cand
                            break

                if not institution:
                    institution = "University / College"

                grade = None
                grade_match = GRADE_PATTERN.search(search_text)
                if grade_match:
                    grade = grade_match.group(0).strip()

                # Calculate confidence score based on completeness
                conf = 0.95 if has_dedicated_section and institution != "University / College" else 0.85

                items.append(EducationItem(
                    institution=institution,
                    degree=degree_text,
                    field=field,
                    start_year=start_yr,
                    end_year=end_yr,
                    grade=grade,
                    confidence=conf,
                    evidence=line[:120]
                ))
                break

    return items
