from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import router as internal_router

app = FastAPI(
    title="AI Resume Parser & Extraction Service",
    description="Internal microservice for resume text extraction, NLP skill tagging, and resume suggestions",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(internal_router, prefix="/internal", tags=["Internal Resume Processing"])

@app.get("/health")
def health_check():
    return {"status": "UP", "service": "ai-service"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8001, reload=False)
