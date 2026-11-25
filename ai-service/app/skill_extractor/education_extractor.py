import re
from typing import List, Optional
from app.schemas.resume_schema import EducationItem

YEAR_RANGE_RE = re.compile(
    r"\b(19\d\d|20\d\d)\s*[-–—to]+\s*(19\d\d|20\d\d|present|current)?\b",
    re.IGNORECASE
)

SCORE_RE = re.compile(
    r"(?:Score|Marks|Grade|CGPA|GPA|Percentage)[\s:]*([0-9\.]+(?:\s*\/\s*[0-9\.]+)?(?:\s*\([0-9\.]+%\)|\s*%)?)",
    re.IGNORECASE
)

STREAM_RE = re.compile(
    r"\b(PCMC|PCMB|PCM|PCB|Commerce|Arts)\b",
    re.IGNORECASE
)

DEGREE_TERMS = [
    (r"\b(?:Bachelor(?:\s+of\s+[\w\s]+)?|B\.?S\.?|B\.?Tech|B\.?E\.?|B\.?A\.?|BCA|BBA|B\.Com)\b[^\n,]*", "Degree"),
    (r"\b(?:Master(?:\s+of\s+[\w\s]+)?|M\.?S\.?|M\.?Tech|M\.?E\.?|M\.?A\.?|MBA|MCA|M\.Com)\b[^\n,]*", "Degree"),
    (r"\b(?:Doctor(?:\s+of\s+[\w\s]+)?|Ph\.?D\.?|Doctorate)\b[^\n,]*", "Doctorate"),
    (r"\b(?:Associate(?:\s+Degree|\s+of\s+[\w\s]+)?|A\.?S\.?|A\.?A\.?)\b[^\n,]*", "Associate Degree"),
    (r"\b(?:Diploma|Post\s+Graduate\s+Diploma|PG\s+Diploma)\b[^\n,]*", "Diploma"),
    (r"\b(?:PU\s+College|PUC|Pre-University|Intermediate|Higher\s+Secondary|Senior\s+Secondary|Class\s+12|12th\s+Standard|12th)\b[^\n,]*", "Pre-University Course (PUC)"),
    (r"\b(?:Public\s+School|High\s+School|Secondary\s+School|Class\s+10|10th\s+Standard|10th|SSLC|Matriculation)\b[^\n,]*", "Secondary School (10th Standard)")
]

INSTITUTION_KEYWORDS = [
    "university", "college", "institute", "school", "academy", "polytechnic", 
    "campus", "faculty", "nitte", "iit", "nit", "bits", "mit", "stanford", "berkeley"
]

NON_INSTITUTION_KEYWORDS = [
    "engineer", "developer", "intern", "manager", "lead", "architect", 
    "reference", "referee", "available upon request", "january", "february", 
    "march", "april", "may", "june", "july", "august", "september", "october", 
    "november", "december", "github", "http"
]

DEG_RE = re.compile(
    r'\b(?:Bachelor|Master|Doctor|Ph\.?D|Associate|Diploma|PU\s*College|PUC|Pre-University|Intermediate|Higher\s*Secondary|Senior\s*Secondary|Class\s*12|12th|Public\s*School|High\s*School|Secondary\s*School|Class\s*10|10th|SSLC|B\.?Tech|B\.?E|B\.?S|M\.?S|M\.?Tech|MBA|MCA)\b',
    re.IGNORECASE
)

INST_RE = re.compile(
    r'\b(?:University|College|Institute|School|Academy|Polytechnic|Nitte|IIT|NIT|BITS|MIT|Stanford|Berkeley)\b',
    re.IGNORECASE
)

def segment_education_blocks(text: str) -> List[str]:
    """
    Segments education text into individual institution/degree blocks.
    Accurately handles entries separated by vertical whitespace (blank lines/spacing)
    or sequential institution/date boundary transitions.
    """
    lines = [l.strip() for l in text.splitlines() if l.strip()]
    if not lines:
        return []

    blocks = []
    curr = []

    def has_match(b_lines, regex):
        return any(bool(regex.search(x)) for x in b_lines)

    for l in lines:
        is_deg = bool(DEG_RE.search(l))
        is_inst = bool(INST_RE.search(l))

        if curr:
            c_has_date = has_match(curr, YEAR_RANGE_RE) or any(bool(re.search(r'\b(19\d\d|20\d\d)\b', x)) for x in curr)
            c_has_score = has_match(curr, SCORE_RE)
            c_has_inst = has_match(curr, INST_RE)
            c_has_deg = has_match(curr, DEG_RE)

            starts_new = False
            if (is_inst or is_deg) and (c_has_date or c_has_score):
                starts_new = True
            elif is_inst and c_has_inst:
                starts_new = True
            elif (is_deg or is_inst) and (c_has_deg and c_has_inst):
                starts_new = True

            if starts_new:
                blocks.append("\n".join(curr))
                curr = [l]
                continue

        curr.append(l)

    if curr:
        blocks.append("\n".join(curr))

    return blocks

def extract_education(section_text: str, full_text: str) -> List[EducationItem]:
    """
    Extracts structured education history.
    Recognizes multiple institutions separated by spacing, double newlines,
    or distinct date/qualification blocks (Degrees, PU Colleges, Secondary Schools).
    """
    has_dedicated_section = bool(section_text and len(section_text.strip()) > 10)
    target_text = section_text if has_dedicated_section else full_text

    blocks = segment_education_blocks(target_text)
    items: List[EducationItem] = []

    for block in blocks:
        lowered_block = block.lower()

        # Reject blocks that are references or jobs
        if any(bad in lowered_block for bad in ["reference", "referee", "available upon request"]):
            continue
        if any(role in lowered_block for role in ["senior software engineer", "full stack developer", "backend developer", "product manager"]):
            continue

        # 1. Extract Dates
        date_match = YEAR_RANGE_RE.search(block)
        start_yr = None
        end_yr = None
        if date_match:
            start_yr = int(date_match.group(1))
            if date_match.group(2) and date_match.group(2).lower() not in ["present", "current"]:
                end_yr = int(date_match.group(2))
        else:
            single_years = re.findall(r'\b(19\d\d|20\d\d)\b', block)
            if len(single_years) >= 2:
                start_yr = int(single_years[0])
                end_yr = int(single_years[1])
            elif len(single_years) == 1:
                end_yr = int(single_years[0])

        # 2. Extract Grade / Score
        score_match = SCORE_RE.search(block)
        grade = score_match.group(0).strip() if score_match else None

        # 3. Clean lines (remove dates and score to isolate degree, field, institution)
        lines = [l.strip() for l in block.splitlines() if l.strip()]
        cleaned_lines = []
        for l in lines:
            c = YEAR_RANGE_RE.sub("", l)
            c = SCORE_RE.sub("", c)
            c = c.strip(" \t-,|:")
            if c:
                cleaned_lines.append(c)

        if not cleaned_lines:
            continue

        # 4. Extract Degree & Field of Study
        degree = None
        field = None

        # Check for field in degree (e.g. "Bachelor of Technology in Computer Science")
        for c_line in cleaned_lines:
            if " in " in c_line.lower():
                parts = re.split(r'\s+in\s+', c_line, flags=re.IGNORECASE, maxsplit=1)
                p0 = parts[0].strip().title()
                p1 = parts[1].strip().title()
                if any(re.search(pat, p0, re.IGNORECASE) for pat, _ in DEGREE_TERMS):
                    degree = p0
                    field = p1
                    break

        # Check for stream/branch (e.g. "PCMC", "PCMB")
        if not field:
            stream_match = STREAM_RE.search(block)
            if stream_match:
                field = stream_match.group(1).upper()

        # If degree not found yet, check all degree/level terms
        if not degree:
            for c_line in cleaned_lines:
                for pat, default_title in DEGREE_TERMS:
                    m = re.search(pat, c_line, re.IGNORECASE)
                    if m:
                        if default_title == "Degree":
                            degree = m.group(0).strip().title()
                        else:
                            degree = default_title
                        break
                if degree:
                    break

        if not degree:
            if any(k in lowered_block for k in ["pu college", "puc", "pre-university"]):
                degree = "Pre-University Course (PUC)"
            elif any(k in lowered_block for k in ["school", "sslc", "10th"]):
                degree = "Secondary School (10th Standard)"
            else:
                degree = "Degree / Coursework"

        # 5. Extract Institution
        institution = None
        for c_line in cleaned_lines:
            # Strip stream token from candidate line
            c_inst = STREAM_RE.sub("", c_line).strip(" \t-,|:")
            # If line is identical to degree, skip
            if degree and c_inst.lower() == degree.lower():
                continue
            if any(k in c_inst.lower() for k in INSTITUTION_KEYWORDS):
                institution = c_inst
                break

        # If institution still not identified, pick the candidate line that is not the degree
        if not institution:
            for c_line in cleaned_lines:
                c_inst = STREAM_RE.sub("", c_line).strip(" \t-,|:")
                if not degree or c_inst.lower() != degree.lower():
                    institution = c_inst
                    break

        if not institution:
            institution = "Educational Institution"

        conf = 0.95 if (institution and degree != "Degree / Coursework") else 0.85

        items.append(EducationItem(
            institution=institution,
            degree=degree,
            field=field,
            start_year=start_yr,
            end_year=end_yr,
            grade=grade,
            confidence=conf,
            evidence=block.replace("\n", " ")[:120]
        ))

    return items
