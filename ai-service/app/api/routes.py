import re
from fastapi import APIRouter, UploadFile, File, HTTPException
from app.schemas.resume_schema import ResumeParseResponse
from app.resume_parser.pdf_extractor import extract_text_from_pdf
from app.resume_parser.docx_extractor import extract_text_from_docx
from app.resume_parser.text_cleaner import clean_text
from app.resume_parser.section_detector import detect_sections
from app.skill_extractor.skill_extractor import extract_skills
from app.skill_extractor.education_extractor import extract_education
from app.skill_extractor.experience_extractor import extract_experience
from app.skill_extractor.project_extractor import extract_projects
from app.suggestion_engine.llm_suggester import get_suggestions
from app.schemas.resume_schema import (
    ResumeParseResponse,
    RoadmapGenerateRequest,
    RoadmapGenerateResponse,
    ImprovementSuggestionsRequest,
    ImprovementSuggestionsResponse,
    SimulationCompareRequest,
    SimulationCompareResponse
)
from app.roadmap_engine.roadmap_generator import generate_roadmap
from app.simulator_engine.suggestion_generator import generate_suggestions
from app.simulator_engine.version_comparator import compare_versions

router = APIRouter()

EMAIL_REGEX = re.compile(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b')
PHONE_REGEX = re.compile(r'(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b')

@router.post("/resume/parse", response_model=ResumeParseResponse)
async def parse_resume(file: UploadFile = File(...)):
    filename = file.filename or ""
    content = await file.read()

    warnings = []
    raw_text = ""

    if filename.lower().endswith(".pdf") or content.startswith(b"%PDF"):
        raw_text, pdf_warnings = extract_text_from_pdf(content)
        warnings.extend(pdf_warnings)
    elif filename.lower().endswith(".docx") or content.startswith(b"PK\x03\x04"):
        raw_text, docx_warnings = extract_text_from_docx(content)
        warnings.extend(docx_warnings)
    else:
        raise HTTPException(status_code=400, detail="Unsupported file format. Please upload a PDF or DOCX file.")

    cleaned_text = clean_text(raw_text)
    if not cleaned_text:
        warnings.append("No extractable text was found in the document.")

    # Detect sections
    sections = detect_sections(cleaned_text)

    # Extract Contact Information
    email_match = EMAIL_REGEX.search(cleaned_text)
    email = email_match.group(0) if email_match else None

    phone_match = PHONE_REGEX.search(cleaned_text)
    phone = phone_match.group(0) if phone_match else None

    # Candidate Name detection from first non-empty lines
    lines = [l.strip() for l in cleaned_text.splitlines() if l.strip()]
    candidate_name = None
    if lines:
        for candidate_line in lines[:3]:
            # Avoid common section headers, contact lines, or URLs as names
            cand_lower = candidate_line.lower()
            if (
                len(candidate_line.split()) <= 4
                and not any(k in cand_lower for k in ["resume", "curriculum", "cv", "http", "@", "phone", "email", "summary", "skills", "experience"])
            ):
                candidate_name = candidate_line
                break

    summary = sections.get("summary")
    if not summary and len(lines) > 1:
        # Fallback to second/third paragraph if short
        for candidate in lines[1:4]:
            if len(candidate) > 40 and not any(k in candidate.lower() for k in ["phone", "email", "github"]):
                summary = candidate
                break

    # Extract Entities with dedicated section targeting and strict negative filters
    skills = extract_skills(sections.get("skills", ""), cleaned_text)
    education = extract_education(sections.get("education", ""), cleaned_text)
    experience = extract_experience(sections.get("experience", ""), cleaned_text)
    projects = extract_projects(sections.get("projects", ""), cleaned_text)

    # Generate Suggestions
    suggestions = get_suggestions(sections, skills, education, experience, projects)

    return ResumeParseResponse(
        candidate_name=candidate_name,
        email=email,
        phone=phone,
        summary=summary,
        raw_text=cleaned_text,
        skills=skills,
        education=education,
        experience=experience,
        projects=projects,
        suggestions=suggestions,
        warnings=warnings
    )

@router.post("/roadmap/generate", response_model=RoadmapGenerateResponse)
async def create_skill_roadmap(request: RoadmapGenerateRequest):
    return generate_roadmap(request)

@router.post("/simulator/suggestions", response_model=ImprovementSuggestionsResponse)
async def create_improvement_suggestions(request: ImprovementSuggestionsRequest):
    suggestions = generate_suggestions(request)
    return ImprovementSuggestionsResponse(suggestions=suggestions)

@router.post("/simulator/simulate", response_model=SimulationCompareResponse)
async def simulate_version_comparison(request: SimulationCompareRequest):
    return compare_versions(request)

