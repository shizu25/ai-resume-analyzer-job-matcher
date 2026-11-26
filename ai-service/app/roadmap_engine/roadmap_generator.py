import re
from typing import Dict, List, Set, Tuple
from app.schemas.resume_schema import RoadmapGenerateRequest, RoadmapGenerateResponse, RoadmapItemSchema

RELATED_SKILLS_GRAPH: Dict[str, List[str]] = {
    "tensorflow": ["scikit-learn", "pytorch", "keras", "machine learning", "deep learning", "python"],
    "pytorch": ["tensorflow", "keras", "scikit-learn", "deep learning", "python"],
    "deep learning": ["machine learning", "neural networks", "scikit-learn", "python", "ai"],
    "machine learning": ["data science", "statistics", "python", "scikit-learn"],
    "nlp": ["text processing", "spacy", "nltk", "machine learning", "deep learning"],
    "postgresql": ["mysql", "sqlite", "sql", "oracle", "database"],
    "mysql": ["postgresql", "sqlite", "sql", "database"],
    "mongodb": ["postgresql", "mysql", "nosql", "redis"],
    "redis": ["caching", "memcached", "postgresql", "rabbitmq"],
    "docker": ["kubernetes", "containerization", "linux", "ci/cd", "devops"],
    "kubernetes": ["docker", "containerization", "helm", "devops", "cloud"],
    "aws": ["azure", "gcp", "cloud computing", "devops"],
    "azure": ["aws", "gcp", "cloud computing"],
    "gcp": ["aws", "azure", "cloud computing"],
    "fastapi": ["flask", "django", "rest api", "python"],
    "django": ["fastapi", "flask", "python", "backend"],
    "spring boot": ["java", "hibernate", "microservices", "spring", "rest api"],
    "react": ["vue", "angular", "next.js", "javascript", "typescript"],
    "next.js": ["react", "typescript", "javascript", "ssr"],
    "angular": ["react", "vue", "typescript"],
    "vue": ["react", "angular", "javascript"],
    "typescript": ["javascript", "frontend", "full stack"],
    "kafka": ["rabbitmq", "redis", "message queues", "event-driven"],
    "rabbitmq": ["kafka", "message queues", "redis"],
    "graphql": ["rest api", "backend", "apollo"],
    "terraform": ["cloud", "devops", "ansible", "aws"]
}

DEFAULT_ROADMAP_TEMPLATES: Dict[str, dict] = {
    "tensorflow": {
        "why": "Crucial for productionizing deep neural networks, computer vision, and tensor operations in modern AI engineering.",
        "objectives": [
            "Master Tensor data structures, operations, and automatic differentiation (tf.GradientTape).",
            "Build custom architectures using Keras Functional API and subclassing.",
            "Implement image classification and sequential neural network pipelines.",
            "Save, export, and benchmark models for inference efficiency."
        ],
        "sequence": [
            "1. Tensors & Computational Graphs Fundamentals",
            "2. Model Architecture with tf.keras & Loss Functions",
            "3. Training Loops, Callbacks, and Hyperparameter Tuning",
            "4. Practical Deep Learning Project Deployment"
        ],
        "tasks": "Implement a Convolutional Neural Network on the CIFAR-10 dataset achieving >80% validation accuracy.",
        "project": "End-to-End Image Classification API with TensorFlow Serving and FastAPI.",
        "duration": "2-3 weeks (est.)",
        "criteria": "Candidate can articulate tensor transformations, train multi-layer architectures, and deploy inference endpoints."
    },
    "pytorch": {
        "why": "Industry standard for cutting-edge AI research, dynamic computation graphs, and LLM fine-tuning.",
        "objectives": [
            "Understand PyTorch Tensors, Autograd, and GPU acceleration with CUDA.",
            "Define custom neural modules inheriting from torch.nn.Module.",
            "Write structured training and validation loops with DataLoader and optimizers.",
            "Fine-tune pre-trained torchvision or huggingface transformer models."
        ],
        "sequence": [
            "1. PyTorch Basics & Tensor Manipulation",
            "2. Custom nn.Module & Loss Optimization",
            "3. Dataset Loaders & Augmentations",
            "4. Transfer Learning Project"
        ],
        "tasks": "Fine-tune a ResNet model on a custom medical or satellite dataset.",
        "project": "Neural Style Transfer or Sentiment Analysis Service with PyTorch.",
        "duration": "2-3 weeks (est.)",
        "criteria": "Candidate can construct custom forward passes, manage training loops, and evaluate confusion matrices."
    },
    "kubernetes": {
        "why": "Essential for container orchestration, zero-downtime rollouts, and multi-service cloud resiliency.",
        "objectives": [
            "Understand Pods, Deployments, Services, and Ingress routing.",
            "Author declarative YAML manifests for multi-tier applications.",
            "Configure ConfigMaps, Secrets, persistent volumes, and health probes.",
            "Practice local cluster debugging with Minikube / K3s."
        ],
        "sequence": [
            "1. Kubernetes Architecture & Cluster Mechanics",
            "2. Workload Management: Deployments, StatefulSets & Services",
            "3. Networking, ConfigMaps & Persistent Storage",
            "4. Multi-Service Microservices Deployment"
        ],
        "tasks": "Deploy a multi-pod web app with Redis caching and ingress load balancing on Minikube.",
        "project": "Automated Microservices Cluster Deployment with Helm charts.",
        "duration": "2 weeks (est.)",
        "criteria": "Candidate can author clean manifests, troubleshoot crash-looping pods, and inspect service ingress."
    },
    "docker": {
        "why": "Standardizes reproducible environments and builds immutable deployment artifacts across development and production.",
        "objectives": [
            "Write optimized multi-stage Dockerfiles minimizing image footprints.",
            "Manage container networking, bind mounts, and persistent volumes.",
            "Compose multi-container environments with Docker Compose.",
            "Implement layer caching and security non-root user practices."
        ],
        "sequence": [
            "1. Images, Containers & Daemon Mechanics",
            "2. Multi-Stage Dockerfile Optimization",
            "3. Networking & Volume Persistence",
            "4. Multi-Service Docker Compose Production Setup"
        ],
        "tasks": "Containerize a full-stack Spring Boot and React application with PostgreSQL backing.",
        "project": "Production-hardened microservices Docker Compose stack with healthchecks.",
        "duration": "1 week (est.)",
        "criteria": "Candidate can create minimal (<150MB) multi-stage images and coordinate complex local services."
    },
    "postgresql": {
        "why": "Industry-standard relational database for ACID compliance, JSONB documents, and complex analytics.",
        "objectives": [
            "Master advanced indexing (B-Tree, GIN, composite) and query execution plans (EXPLAIN ANALYZE).",
            "Design normalized schemas, foreign key constraints, and transactional boundaries.",
            "Utilize JSONB columns and PostgreSQL full-text search.",
            "Write efficient window functions, CTEs, and schema migrations."
        ],
        "sequence": [
            "1. Schema Design, Constraints & Normalization",
            "2. Complex Queries, CTEs & Window Functions",
            "3. Query Optimization & Index Tuning with EXPLAIN ANALYZE",
            "4. Migration Automation with Flyway / Liquibase"
        ],
        "tasks": "Optimize a slow multi-join reporting query using covering indexes and EXPLAIN ANALYZE.",
        "project": "High-throughput Transactional Ledger with schema migrations and JSONB auditing.",
        "duration": "1-2 weeks (est.)",
        "criteria": "Candidate can interpret EXPLAIN ANALYZE trees, avoid table scans, and structure fault-tolerant transactions."
    },
    "fastapi": {
        "why": "High-performance Python async framework designed for high-concurrency microservices and AI model APIs.",
        "objectives": [
            "Implement async request handlers with Pydantic type validation.",
            "Structure modular routes with APIRouter and dependency injection.",
            "Handle file uploads, background tasks, and streaming responses.",
            "Integrate automated OpenAPI documentation and JWT authentication."
        ],
        "sequence": [
            "1. Async Python & Pydantic Data Contracts",
            "2. Route Architecture & Dependency Injection",
            "3. Authentication, Middlewares & Background Tasks",
            "4. Production Deployment with Uvicorn & Gunicorn"
        ],
        "tasks": "Build an asynchronous file processing API that validates uploads and runs background analysis.",
        "project": "Real-time Machine Learning Inference Microservice with OpenAPI docs.",
        "duration": "1 week (est.)",
        "criteria": "Candidate can write async endpoints, configure dependency injection, and enforce request schemas."
    },
    "spring boot": {
        "why": "Enterprise standard for robust, high-throughput microservices, dependency injection, and database transactions.",
        "objectives": [
            "Master Inversion of Control, Bean lifecycles, and Spring Data JPA repositories.",
            "Configure Spring Security, JWT filters, and stateless authentication.",
            "Write transactional business services with exception translation.",
            "Design comprehensive JUnit 5 and Mockito test suites."
        ],
        "sequence": [
            "1. Spring IoC, Configuration & Bean Lifecycle",
            "2. Spring Data JPA, Hibernate Relationships & Transactions",
            "3. Spring Security & JWT Filter Chains",
            "4. Unit & Integration Testing with MockMvc"
        ],
        "tasks": "Implement a secure REST API with role-based access control and transactional entity updates.",
        "project": "Enterprise Resource Management API with Spring Boot, Flyway, and PostgreSQL.",
        "duration": "2 weeks (est.)",
        "criteria": "Candidate can construct secure Spring Security filter chains and maintain transactional consistency."
    },
    "react": {
        "why": "Leading UI library for reactive single-page applications, modular components, and rich user interactions.",
        "objectives": [
            "Master modern React hooks (useState, useEffect, useMemo, useCallback, useContext).",
            "Implement robust client-side routing and protected routes with React Router.",
            "Manage global application state cleanly without unnecessary re-renders.",
            "Integrate REST APIs with Axios, loading skeletons, and optimistic updates."
        ],
        "sequence": [
            "1. Modern Component Patterns & Hooks Mechanics",
            "2. State Architecture & Context Providers",
            "3. Routing, Forms & Asynchronous API Synchronization",
            "4. Responsive SaaS Dashboard Build"
        ],
        "tasks": "Build an interactive multi-step data wizard with client-side validation and responsive layout.",
        "project": "Real-time Career Intelligence Analytics Dashboard in React & Tailwind CSS.",
        "duration": "1-2 weeks (est.)",
        "criteria": "Candidate can manage complex component state, prevent render thrashing, and handle async errors."
    }
}

def normalize_skill_name(name: str) -> str:
    cleaned = name.strip().lower()
    cleaned = re.sub(r'[\(\)]', '', cleaned)
    cleaned = re.sub(r'\s+', ' ', cleaned)
    return cleaned

def check_evidence_support(skill: str, experience_texts: List[str], project_texts: List[str], full_resume_text: str) -> bool:
    norm = normalize_skill_name(skill)
    # Check for direct presence in project descriptions or technologies
    for proj in project_texts:
        if norm in proj.lower():
            return True
    # Check for direct presence in experience descriptions
    for exp in experience_texts:
        if norm in exp.lower():
            return True

    # Check for evidence pattern in full text: e.g. "built ... using <skill>", "developed with <skill>"
    pattern = rf'\b(?:using|with|via|in|implemented|developed|built|managed)\s+[^\.\n]*\b{re.escape(norm)}\b'
    if re.search(pattern, full_resume_text, re.IGNORECASE):
        return True

    return False

def generate_custom_roadmap_template(skill: str, job_title: str) -> dict:
    title = skill.strip().title()
    return {
        "why": f"Required for {job_title} to fulfill core technical responsibilities and deliver robust engineering solutions.",
        "objectives": [
            f"Understand core syntax, paradigms, and architecture of {title}.",
            f"Apply {title} to practical scenarios adhering to production best practices.",
            f"Build and test modular components using {title}.",
            f"Troubleshoot common failure modes and optimize performance."
        ],
        "sequence": [
            f"1. Foundations, Tooling & Core Architecture of {title}",
            f"2. Practical Implementation of {title} in Isolated Exercises",
            f"3. Integration into Multi-Tier Systems",
            f"4. Testing, Benchmarking & Deployment"
        ],
        "tasks": f"Complete hands-on technical labs implementing standard {title} design patterns.",
        "project": f"Portfolio-ready project integrating {title} into a real-world application.",
        "duration": "1-2 weeks (est.)",
        "criteria": f"Candidate can independently build, debug, and explain architectural decisions using {title}."
    }

def generate_roadmap(request: RoadmapGenerateRequest) -> RoadmapGenerateResponse:
    normalized_resume_skills = {normalize_skill_name(s): s for s in request.resume_skills if s.strip()}
    required_skills = [s.strip() for s in request.required_skills if s.strip()]
    preferred_skills = [s.strip() for s in request.preferred_skills if s.strip()]

    all_job_skills = required_skills + [p for p in preferred_skills if p not in required_skills]
    
    demonstrated_skills = []
    mentioned_skills = []
    missing_skills = []
    related_skills = []

    for job_skill in all_job_skills:
        norm_job_skill = normalize_skill_name(job_skill)
        # 1. Direct match check
        if norm_job_skill in normalized_resume_skills:
            has_evidence = check_evidence_support(
                job_skill,
                request.experience_descriptions,
                request.project_descriptions,
                request.resume_text
            )
            if has_evidence:
                demonstrated_skills.append(job_skill)
            else:
                mentioned_skills.append(job_skill)
        else:
            missing_skills.append(job_skill)

    # 2. Related skills check (skills the candidate has that are related to missing job skills)
    for missing in missing_skills:
        norm_missing = normalize_skill_name(missing)
        rel_list = RELATED_SKILLS_GRAPH.get(norm_missing, [])
        for rel in rel_list:
            if rel in normalized_resume_skills:
                orig_name = normalized_resume_skills[rel]
                if orig_name not in related_skills and orig_name not in demonstrated_skills and orig_name not in mentioned_skills:
                    related_skills.append(orig_name)

    # Calculate overall coverage percentage:
    # Demonstrated = 1.0 weight, Mentioned = 0.5 weight, Related = 0.25 supporting weight
    total_requirements = max(1, len(all_job_skills))
    effective_score = (
        len(demonstrated_skills) * 1.0 +
        len(mentioned_skills) * 0.60 +
        min(len(related_skills) * 0.25, 1.5)
    )
    overall_coverage = round(min(100.0, (effective_score / total_requirements) * 100.0), 1)

    # Build Roadmap Items with transparent priority
    items: List[RoadmapItemSchema] = []

    # Priority calculation logic:
    # High: Mandatory missing skill or mentioned without evidence for key job skill
    # Medium: Preferred missing skill, or missing skill where candidate has related background
    # Low: Secondary or already demonstrated skill
    for skill in all_job_skills:
        norm_skill = normalize_skill_name(skill)
        is_required = any(normalize_skill_name(r) == norm_skill for r in required_skills)
        has_related = any(rel in normalized_resume_skills for rel in RELATED_SKILLS_GRAPH.get(norm_skill, []))

        if skill in demonstrated_skills:
            status = "DEMONSTRATED"
            priority = "LOW"
            reason = "Skill is already demonstrated with supporting project or work experience in your resume."
        elif skill in mentioned_skills:
            status = "MENTIONED"
            priority = "HIGH" if is_required else "MEDIUM"
            reason = (
                "Skill is listed in your profile but lacks supporting project or work experience evidence. "
                "Adding measurable project deliverables will validate your proficiency."
            )
        else:
            status = "MISSING"
            if is_required and not has_related:
                priority = "HIGH"
                reason = "Mandatory requirement explicitly listed in job specifications with no direct prior foundation detected."
            elif is_required and has_related:
                priority = "HIGH"
                reason = "Mandatory requirement. You have related foundational skills, making this high-impact and fast to acquire."
            else:
                priority = "MEDIUM"
                reason = "Preferred qualification that enhances candidate competitiveness for advanced responsibilities."

        template = DEFAULT_ROADMAP_TEMPLATES.get(norm_skill) or generate_custom_roadmap_template(skill, request.job_title)

        items.append(RoadmapItemSchema(
            skill_name=skill,
            status=status,
            priority=priority,
            priority_reason=reason,
            why_required=template["why"],
            learning_objectives=template["objectives"],
            learning_sequence=template["sequence"],
            practical_tasks=template["tasks"],
            suggested_project=template["project"],
            estimated_duration=template["duration"],
            completion_criteria=template["criteria"],
            progress_status="NOT_STARTED" if status != "DEMONSTRATED" else "COMPLETED"
        ))

    # Add related supporting items if applicable
    for rel_skill in related_skills:
        norm_rel = normalize_skill_name(rel_skill)
        template = DEFAULT_ROADMAP_TEMPLATES.get(norm_rel) or generate_custom_roadmap_template(rel_skill, request.job_title)
        items.append(RoadmapItemSchema(
            skill_name=rel_skill,
            status="RELATED",
            priority="LOW",
            priority_reason="Supporting foundation skill present in your background that accelerates your learning curve.",
            why_required=f"Valuable supporting technology that complements {request.job_title} requirements.",
            learning_objectives=template["objectives"],
            learning_sequence=template["sequence"],
            practical_tasks=template["tasks"],
            suggested_project=template["project"],
            estimated_duration=template["duration"],
            completion_criteria=template["criteria"],
            progress_status="IN_PROGRESS"
        ))

    # Sort items: HIGH priority first, then MEDIUM, then LOW
    priority_order = {"HIGH": 0, "MEDIUM": 1, "LOW": 2}
    items.sort(key=lambda x: priority_order.get(x.priority, 3))

    return RoadmapGenerateResponse(
        overall_coverage_pct=overall_coverage,
        demonstrated_skills_count=len(demonstrated_skills),
        mentioned_skills_count=len(mentioned_skills),
        missing_skills_count=len(missing_skills),
        related_skills_count=len(related_skills),
        demonstrated_skills=demonstrated_skills,
        mentioned_skills=mentioned_skills,
        missing_skills=missing_skills,
        related_skills=related_skills,
        items=items
    )
