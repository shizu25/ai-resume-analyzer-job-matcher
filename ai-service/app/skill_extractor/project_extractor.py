import re
from typing import List, Optional
from app.schemas.resume_schema import ProjectItem

URL_PATTERN = re.compile(
    r"(?:https?://(?:www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b[-a-zA-Z0-9()@:%_\+.~#?&//=]*|github\.com/[-a-zA-Z0-9_./]+)"
)

PROJECT_EXCLUSION_KEYWORDS = [
    "email:", "phone:", "linkedin", "location:", "address:", "portfolio:", "@",
    "professional summary", "technical skills", "work experience", "employment history",
    "education &", "references &", "certifications &", "languages known",
    "languages:", "programming:", "frameworks:", "libraries:", "databases:",
    "tools:", "devops:", "technologies:", "core competencies:", "tech stack:",
    "bachelor of", "master of", "b.tech", "b.e.", "b.s.", "m.s.", "phd", "degree",
    "university", "college", "gpa:", "cgpa:", "graduated",
    "references", "referee", "available upon request", "furnished upon request"
]

ACTION_VERBS = {
    "created", "developed", "built", "implemented", "designed", "engineered",
    "maintained", "configured", "optimized", "integrated", "collaborated",
    "led", "managed", "deployed", "assisted", "spearheaded", "authored", "gained",
    "utilized", "improved", "automated", "delivered", "programmed", "researched"
}

def extract_projects(section_text: str, full_text: str) -> List[ProjectItem]:
    """
    Extracts projects with confidence scores and evidence snippets.
    Seamlessly handles both:
    1. Header followed by description bullets (e.g. 'Project Name | Tech Stack' then bullets)
    2. Direct bullet projects (e.g. '• Face recognition system using Python and OpenCV')
    """
    has_dedicated_section = bool(section_text and len(section_text.strip()) > 10)
    target_text = section_text if has_dedicated_section else full_text

    # Normalize cases where a period ends one project and next project title starts on next line
    target_text = re.sub(r'\.\s+([A-Z][a-zA-Z\s]+)\n([A-Z])', r'.\n\1 \2', target_text)

    projects: List[ProjectItem] = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    current_proj: Optional[ProjectItem] = None
    desc_lines: List[str] = []

    for line in lines:
        lowered_line = line.lower()

        if any(bad in lowered_line for bad in PROJECT_EXCLUSION_KEYWORDS):
            continue

        url_match = URL_PATTERN.search(line)

        # Associate standalone URL line with active project
        if url_match and current_proj and not current_proj.link and len(line) < 100:
            current_proj.link = url_match.group(0)
            cleaned_rem = URL_PATTERN.sub("", line).strip(" |-–—,\t")
            if cleaned_rem:
                desc_lines.append(cleaned_rem)
            continue

        is_bullet = line.startswith(("-", "•", "*", "·", ">", "+"))
        cleaned_content = re.sub(r'^[•\-\*·\>+\d\.\s]+', '', line).strip()

        if not cleaned_content:
            continue

        # Check if line contains a project statement (e.g. "Face recognition system using Python and OpenCV")
        tech_marker_match = re.search(r'\b(?:using|with|via)\s+([A-Za-z0-9\s,\+/#\-]+?)(?:\.\s+[A-Z]|\.|$)', cleaned_content, re.IGNORECASE)

        is_project_header = False
        first_word = cleaned_content.split()[0].lower() if cleaned_content.split() else ""
        is_action_desc = first_word in ACTION_VERBS

        if has_dedicated_section:
            # Inside dedicated projects section:
            if is_action_desc:
                # Description bullet like "- Created async queue system."
                is_project_header = False
            elif is_bullet:
                # Direct bullet project (e.g. "• Face recognition system using Python and OpenCV")
                if tech_marker_match or not current_proj or any(d in cleaned_content for d in [" | ", " - ", " – ", ":"]):
                    is_project_header = True
            elif ("|" in cleaned_content or " - " in cleaned_content or ":" in cleaned_content) and len(cleaned_content) < 90:
                is_project_header = True
            elif len(cleaned_content.split()) <= 6 and not cleaned_content.endswith((".", ";")):
                is_project_header = True
            elif tech_marker_match:
                is_project_header = True
        else:
            # Outside dedicated section: require explicit indicator
            if re.match(r'^(?:Project|Key Project|Personal Project|Academic Project)\s*[:\-]', line, re.IGNORECASE):
                is_project_header = True
            elif tech_marker_match and url_match:
                is_project_header = True

        if is_project_header:
            if current_proj:
                current_proj.description = "\n".join(desc_lines).strip()
                projects.append(current_proj)
                desc_lines = []

            link = url_match.group(0) if url_match else None
            title = cleaned_content
            tech = None

            # 1. Parse using tech marker (using / with / via)
            if tech_marker_match:
                tech = tech_marker_match.group(1).strip(" .")
                title = cleaned_content[:tech_marker_match.start()].strip(" -–—:|.,")
            # 2. Parse using delimiters ( | , - , : )
            elif any(d in cleaned_content for d in [" | ", " - ", " – ", ":"]):
                for delim in [" | ", " - ", " – ", ":"]:
                    if delim in cleaned_content:
                        parts = cleaned_content.split(delim, 1)
                        title = parts[0].strip()
                        tech = parts[1].strip(" .")
                        break
            # 3. Parse parenthetical tech stack
            elif "(" in cleaned_content and ")" in cleaned_content:
                paren_match = re.search(r'\(([^)]+)\)', cleaned_content)
                if paren_match:
                    tech = paren_match.group(1).strip()
                    title = cleaned_content[:paren_match.start()].strip()

            title = re.sub(r'^[#•\-\*\s]+', '', title).strip().rstrip(" .")
            if not title:
                title = "Software Project"

            conf = 0.95 if (tech or link) else 0.88

            current_proj = ProjectItem(
                title=title,
                technologies=tech,
                link=link,
                description=cleaned_content,
                confidence=conf,
                evidence=line[:120]
            )
        else:
            if current_proj:
                if url_match and not current_proj.link:
                    current_proj.link = url_match.group(0)
                desc_lines.append(cleaned_content)

    if current_proj:
        current_proj.description = "\n".join(desc_lines).strip()
        projects.append(current_proj)

    return projects
