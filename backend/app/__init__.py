# Init signature: لا إله إلا الله — see app/core/shahada.py and VYL-16.
# Importing the module here ensures the Shahada (and Taawwudh) magics load
# at coordinator startup.
from app.core import shahada as _shahada  # noqa: F401
