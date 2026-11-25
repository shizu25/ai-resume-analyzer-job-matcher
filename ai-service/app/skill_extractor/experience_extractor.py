import re
from typing import List, Optional
from app.schemas.resume_schema import ExperienceItem

DATE_RANGE_PATTERN = re.compile(
    r"\b((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s*[-–—/]?\s*(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)?[a-z]*\.?\s*\d{4}|\d{4})\s*[-–—to]*\s*((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s*\d{4}|\d{4}|Present|Current)?\b",
    re.IGNORECASE
)

MONTH_RANGE_PATTERN = re.compile(
    r"\b((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s*[-–—/]\s*(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s*\d{4})\b",
    re.IGNORECASE
)

JOB_ROLE_KEYWORDS = [
    "engineer", "developer", "architect", "analyst", "manager", "specialist",
    "consultant", "intern", "internship", "trainee", "lead", "associate", "assistant",
    "director", "administrator", "scientist", "designer", "coordinator", "officer",
    "programmer", "technician", "fellow", "instructor", "researcher", "contributor"
]

EDUCATION_EXCLUSIONS = [
    "bachelor", "master", "doctor", "phd", "ph.d.", "b.s.", "b.tech", "b.e.",
    "m.s.", "m.tech", "m.e.", "b.a.", "m.a.", "bca", "mca", "degree",
    "diploma", "associate degree", "high school", "university", "college",
    "institute of technology", "school of", "academy of", "gpa", "cgpa", "coursework"
]

SECTION_BOUNDARY_KEYWORDS = [
    "projects", "technical skills", "skills", "volunteering", "certifications",
    "education", "academic", "references", "languages", "publications"
]

def extract_experience(section_text: str, full_text: str) -> List[ExperienceItem]:
    """
    Extracts work experience and internship history.
    Strictly terminates when other section headings are encountered so that
    skills, projects, and volunteering entries are never swallowed into job descriptions.
    """
    has_dedicated_section = bool(section_text and len(section_text.strip()) > 10)
    target_text = section_text if has_dedicated_section else full_text
    items: List[ExperienceItem] = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    current_item: Optional[ExperienceItem] = None
    desc_lines: List[str] = []

    for line in lines:
        lowered_line = line.lower()
        cleaned_content = re.sub(r'^[•\-\*·\>+\s]+', '', line).strip()

        # Stop if line is another section heading
        clean_header_check = cleaned_content.lower().rstrip(" :|-")
        if any(clean_header_check == sec or clean_header_check.startswith(sec) for sec in SECTION_BOUNDARY_KEYWORDS):
            if current_item:
                current_item.description = "\n".join(desc_lines).strip()
                if hasattr(current_item, '_needs_dates'):
                    delattr(current_item, '_needs_dates')
                items.append(current_item)
                current_item = None
                desc_lines = []
            if not has_dedicated_section:
                continue

        if any(bad in lowered_line for bad in EDUCATION_EXCLUSIONS):
            continue
        if any(bad in lowered_line for bad in ["reference", "referee", "available upon request", "@acme.com", "email:", "phone:"]):
            continue

        date_match = MONTH_RANGE_PATTERN.search(cleaned_content) or DATE_RANGE_PATTERN.search(cleaned_content)
        has_job_role = any(re.search(r'\b' + re.escape(role) + r'\b', lowered_line) for role in JOB_ROLE_KEYWORDS)

        is_bullet = line.startswith(("-", "•", "*", "·", ">", "+"))

        # Check for multi-line date continuation
        if date_match and current_item and not desc_lines and getattr(current_item, '_needs_dates', False):
            current_item.start_date = date_match.group(1)
            current_item.end_date = date_match.group(2) if date_match.lastindex and date_match.lastindex >= 2 and date_match.group(2) else "Completed"
            current_item._needs_dates = False
            continue

        is_job_header = False
        if date_match and (has_job_role or len(cleaned_content) < 100):
            is_job_header = True
        elif has_job_role and len(cleaned_content) < 75 and not is_bullet:
            is_job_header = True
        elif has_dedicated_section and is_bullet and ("|" in cleaned_content or date_match):
            is_job_header = True

        if is_job_header:
            if current_item:
                current_item.description = "\n".join(desc_lines).strip()
                if hasattr(current_item, '_needs_dates'):
                    delattr(current_item, '_needs_dates')
                items.append(current_item)
                desc_lines = []

            needs_dates = not bool(date_match)
            start_d = date_match.group(1) if date_match else "2024"
            end_d = date_match.group(2) if date_match and date_match.lastindex and date_match.lastindex >= 2 and date_match.group(2) else "Present"
            is_curr = "present" in end_d.lower() or "current" in end_d.lower()

            cleaned_header = DATE_RANGE_PATTERN.sub("", cleaned_content).strip(" |-–—,\t")
            cleaned_header = re.sub(r'\|\s*(?:Remote|[A-Za-z\s]+,\s*[A-Z]{2})\b', '', cleaned_header, flags=re.IGNORECASE).strip()

            pos = None
            company = None

            for delim in [" | ", " - ", " – ", " — ", " at "]:
                if delim in cleaned_header:
                    parts = cleaned_header.split(delim, 1)
                    p0 = parts[0].strip()
                    p1 = parts[1].strip()

                    if any(r in p0.lower() for r in JOB_ROLE_KEYWORDS):
                        pos = p0
                        company = p1
                    elif any(r in p1.lower() for r in JOB_ROLE_KEYWORDS):
                        pos = p1
                        company = p0
                    else:
                        company = p0
                        pos = p1
                    break

            if not company and not pos:
                company = cleaned_header if cleaned_header else "Company / Organization"
                pos = "Intern" if any(k in full_text.lower() for k in ["internship", "intern", "fab lab", "summer of code"]) else "Software Engineer"
            elif not pos:
                pos = "Intern" if any(k in full_text.lower() for k in ["internship", "intern", "fab lab", "summer of code"]) else "Software Engineer"
            elif not company:
                company = "Company / Organization"

            pos = re.sub(r'^[•\-\*\|\s]+', '', pos).strip()
            company = re.sub(r'^[•\-\*\|\s]+', '', company).strip()

            current_item = ExperienceItem(
                company=company,
                position=pos,
                start_date=start_d,
                end_date=end_d,
                is_current=is_curr,
                description="",
                confidence=0.92,
                evidence=line[:120]
            )
            current_item._needs_dates = needs_dates
        else:
            if current_item:
                desc_lines.append(cleaned_content)

    if current_item:
        current_item.description = "\n".join(desc_lines).strip()
        if hasattr(current_item, '_needs_dates'):
            delattr(current_item, '_needs_dates')
        items.append(current_item)

    return items
