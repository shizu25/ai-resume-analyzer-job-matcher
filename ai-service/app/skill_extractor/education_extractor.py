import re
from typing import List, Optional
from app.schemas.resume_schema import EducationItem

DEGREE_PATTERNS = [
    r"\b(?:Bachelor|Master|Doctor|PhD|Ph\.D\.|B\.?S\.?|B\.?Tech|B\.?E\.?|M\.?S\.?|M\.?Tech|M\.?E\.?|B\.?A\.?|M\.?A\.?|MBA|BCA|MCA|Associate)\b[^\n,]*",
]

YEAR_PATTERN = re.compile(r"\b(19\d\d|20\d\d)\b")
GRADE_PATTERN = re.compile(r"(?:GPA|CGPA|Grade)[\s:]*([0-9\.]+(?:\s*\/\s*[0-9\.]+)?%?)", re.IGNORECASE)

def extract_education(section_text: str, full_text: str) -> List[EducationItem]:
    target_text = section_text if section_text and len(section_text.strip()) > 20 else full_text
    items = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    for i, line in enumerate(lines):
        for pattern in DEGREE_PATTERNS:
            match = re.search(pattern, line, re.IGNORECASE)
            if match:
                degree_text = match.group(0).strip()
                
                # Check for year in current line and adjacent next lines
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

                # Institution heuristic: previous line or part before/after comma
                institution = None
                field = None
                
                if "in " in degree_text.lower():
                    parts = degree_text.lower().split("in ", 1)
                    degree_text = parts[0].strip().title()
                    field = parts[1].strip().title()
                
                if i > 0 and len(lines[i-1]) < 80 and not any(kw in lines[i-1].lower() for kw in ["education", "degree"]):
                    institution = lines[i-1]
                elif "," in line:
                    parts = line.split(",")
                    institution = parts[0].strip()

                grade = None
                grade_match = GRADE_PATTERN.search(search_text)
                if grade_match:
                    grade = grade_match.group(0)

                items.append(EducationItem(
                    institution=institution if institution else "University / College",
                    degree=degree_text,
                    field=field,
                    start_year=start_yr,
                    end_year=end_yr,
                    grade=grade
                ))
                break

    return items
