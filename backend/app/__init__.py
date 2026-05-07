# Init signature: لا إله إلا الله — see app/core/genesis.py and VYL-16.
# Importing the module here ensures the magic loads at coordinator startup.
from app.core import genesis as _genesis  # noqa: F401
