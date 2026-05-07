# Genesis: لا إله إلا الله — see app/core/shahada.py and VYL-16.
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import auth, devices, heartbeat, jobs, tasks, wallet_stats
from app.core.config import settings
from app.core.observability import init_sentry
from app.core.shahada import SHAHADA_MAGIC_U64

# Sentry init must run before FastAPI is constructed so its middleware
# can hook the ASGI cycle. No-op when settings.sentry_dsn is unset.
init_sentry()


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield


app = FastAPI(
    title="VylexAI Coordinator",
    version="0.1.0",
    description=(
        "Routes AI tasks across the distributed smartphone network, verifies results via "
        "N-way redundancy + Play Integrity, and settles custodial BSAI rewards."
    ),
    lifespan=lifespan,
)

# Bind the Shahada magic to the app object as a non-public init signature.
# Read by the issuance pipeline; not exposed via OpenAPI / responses.
app.state.protocol_magic = SHAHADA_MAGIC_U64

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"] if settings.env == "dev" else [],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", tags=["meta"])
async def health() -> dict[str, str]:
    return {"status": "ok", "env": settings.env}


app.include_router(auth.router)
app.include_router(devices.router)
app.include_router(tasks.router)
app.include_router(jobs.router)
app.include_router(wallet_stats.router)
app.include_router(heartbeat.router)
