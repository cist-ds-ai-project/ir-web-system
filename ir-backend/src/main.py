from contextlib import asynccontextmanager

from fastapi import FastAPI

from src.routers import teacher
from src.routers import personal
from src.datasource import DbConnectionHolder
from src.routers import sample
from src.routers import grade
from src.routers import user_manage
from src.routers import enrollment
from src.routers import scholarship
from src.routers import chat
from src.routers import gpa
from src.routers import course_difficulty
from src.routers import lecture
from src.routers import questionnaire
from src.routers import syllabus


# region agent log
import json as _agent_json
import time as _agent_time


def _agent_debug_log(hypothesis_id, location, message, data):
    try:
        entry = {
            "sessionId": "045e9e",
            "runId": "initial-debug",
            "hypothesisId": hypothesis_id,
            "location": location,
            "message": message,
            "data": data,
            "timestamp": int(_agent_time.time() * 1000),
        }
        with open(r"C:/programing/ir-web/ir-web/debug-045e9e.log", "a", encoding="utf-8") as _f:
            _f.write(_agent_json.dumps(entry, ensure_ascii=False) + "\n")
    except Exception:
        pass



@asynccontextmanager
async def lifespan(app: FastAPI):
    DbConnectionHolder.init()
    # region agent log
    try:
        _agent_debug_log(
            "H1",
            "main.py:lifespan",
            "registered routes at startup",
            {"paths": sorted({getattr(r, "path", None) for r in app.routes if getattr(r, "path", None)})},
        )
    except Exception:
        pass
    # endregion
    yield
    DbConnectionHolder.close_all()
    print("Connection pool closed")


app = FastAPI(lifespan=lifespan)
app.include_router(sample.router)
app.include_router(grade.router)
app.include_router(gpa.router)
app.include_router(user_manage.router)
app.include_router(enrollment.router)
app.include_router(scholarship.router)
app.include_router(lecture.router)
app.include_router(syllabus.router)
app.include_router(chat.router)
app.include_router(gpa.router)
app.include_router(course_difficulty.router)
app.include_router(questionnaire.router)
app.include_router(personal.router)
app.include_router(teacher.router)

# region agent log
@app.middleware("http")
async def _agent_request_logger(request, call_next):
    response = await call_next(request)
    _agent_debug_log(
        "H3",
        "main.py:middleware",
        "incoming request",
        {
            "path": request.url.path,
            "method": request.method,
            "status": response.status_code,
        }
    )
    return response
# endregion
