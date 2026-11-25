from pydantic import BaseModel, Field
from typing import List, Optional

class SkillItem(BaseModel):
    name: str
    category: str = "General"

class EducationItem(BaseModel):
    institution: Optional[str] = None
    degree: Optional[str] = None
    field: Optional[str] = None
    start_year: Optional[int] = None
    end_year: Optional[int] = None
    grade: Optional[str] = None

class ExperienceItem(BaseModel):
    company: Optional[str] = None
    position: Optional[str] = None
    location: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    is_current: Optional[bool] = False
    description: Optional[str] = None

class ProjectItem(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    technologies: Optional[str] = None
    link: Optional[str] = None

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
