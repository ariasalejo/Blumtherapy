# Safe diagnostics

Diagnostics should be read-only by default and distinguish device-reported facts from interpretation. Do not collect or display IMEI or serial numbers by default. Redact logs before export, obtain informed consent, and explain limitations. Any feature that changes device state needs a separate design review, clear warning, and recovery path.
