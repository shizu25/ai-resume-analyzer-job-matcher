from pydantic import BaseModel, Field
from typing import List, Optional

class SkillItem(BaseModel):
    name: str
    category: str = "General"
    confidence: float = 0.95
    evidence: Optional[str] = None

class EducationItem(BaseModel):
    institution: Optional[str] = None
    degree: Optional[str] = None
    field: Optional[str] = None
    start_year: Optional[int] = None
    end_year: Optional[int] = None
    grade: Optional[str] = None
    confidence: float = 0.90
    evidence: Optional[str] = None

class ExperienceItem(BaseModel):
    company: Optional[str] = None
    position: Optional[str] = None
    location: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    is_current: Optional[bool] = False
    description: Optional[str] = None
    confidence: float = 0.90
    evidence: Optional[str] = None

class ProjectItem(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    technologies: Optional[str] = None
    link: Optional[str] = None
    confidence: float = 0.90
    evidence: Optional[str] = None

class SuggestionItem(BaseModel):
    category: str
    priority: str  # high, medium, low
    issue: str
    recommendation: str

class ParsedResumeResponse(BaseModel):
    candidate_name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    summary: Optional[str] = None
    raw_text: Optional[str] = None
    skills: List[SkillItem] = Field(default_factory=list)
    education: List[EducationItem] = Field(default_factory=list)
    experience: List[ExperienceItem] = Field(default_factory=list)
    projects: List[ProjectItem] = Field(default_factory=list)
    suggestions: List[SuggestionItem] = Field(default_factory=list)
    warnings: List[str] = Field(default_factory=list)

ResumeParseResponse = ParsedResumeResponse

# --- Feature 1: Skill-Gap Roadmap Schemas ---

class RoadmapItemSchema(BaseModel):
    skill_name: str
    status: str  # DEMONSTRATED, MENTIONED, MISSING, RELATED
    priority: str  # HIGH, MEDIUM, LOW
    priority_reason: str
    why_required: str
    learning_objectives: List[str] = Field(default_factory=list)
    learning_sequence: List[str] = Field(default_factory=list)
    practical_tasks: str
    suggested_project: str
    estimated_duration: str
    completion_criteria: str
    progress_status: str = "NOT_STARTED"

class RoadmapGenerateRequest(BaseModel):
    resume_skills: List[str] = Field(default_factory=list)
    resume_text: str = ""
    experience_descriptions: List[str] = Field(default_factory=list)
    project_descriptions: List[str] = Field(default_factory=list)
    job_title: str
    job_description: str = ""
    required_skills: List[str] = Field(default_factory=list)
    preferred_skills: List[str] = Field(default_factory=list)
    experience_level: Optional[str] = "MID"

class RoadmapGenerateResponse(BaseModel):
    overall_coverage_pct: float
    demonstrated_skills_count: int
    mentioned_skills_count: int
    missing_skills_count: int
    related_skills_count: int
    demonstrated_skills: List[str] = Field(default_factory=list)
    mentioned_skills: List[str] = Field(default_factory=list)
    missing_skills: List[str] = Field(default_factory=list)
    related_skills: List[str] = Field(default_factory=list)
    items: List[RoadmapItemSchema] = Field(default_factory=list)

# --- Feature 2: Resume Simulator & Version Comparison Schemas ---

class ImprovementSuggestionSchema(BaseModel):
    category: str  # MISSING_RELEVANT_INFO, WEAK_PROJECT_DESCRIPTION, UNCLEAR_CONTRIBUTION, MISSING_SKILL_EVIDENCE, POORLY_STRUCTURED, IRRELEVANT_INFO, GRAMMAR_READABILITY
    original_text: str
    suggested_text: str
    issue: str
    reason: str
    related_requirement: Optional[str] = None
    expected_benefit: str

class ImprovementSuggestionsRequest(BaseModel):
    resume_content: dict
    job_title: str
    job_description: str = ""
    required_skills: List[str] = Field(default_factory=list)
    preferred_skills: List[str] = Field(default_factory=list)

class ImprovementSuggestionsResponse(BaseModel):
    suggestions: List[ImprovementSuggestionSchema] = Field(default_factory=list)

class SimulationCompareRequest(BaseModel):
    version_a_content: dict
    version_b_content: dict
    job_title: str
    job_description: str = ""
    required_skills: List[str] = Field(default_factory=list)
    preferred_skills: List[str] = Field(default_factory=list)

class SimulationCompareResponse(BaseModel):
    base_score: float
    improved_score: float
    score_delta: float
    skill_coverage_base: float
    skill_coverage_improved: float
    project_relevance_base: float
    project_relevance_improved: float
    explanation: str
    positive_changes: List[str] = Field(default_factory=list)
    regressions: List[str] = Field(default_factory=list)

