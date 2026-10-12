# Resource Data Schema

Example data is not a guarantee that a service is available. Start with `unreviewed` until a person validates operational details with the official source.

## Fields
- `id`: stable non-personal identifier.
- `name`: public organization/resource name.
- `country`, `region`, `locality`: geographic scope; null when unknown.
- `language`: supported service language, only when confirmed.
- `category`: `health`, `education`, `employment`, `social_support`, `peer_support`, `housing`, `crisis`, or `research`.
- `resource_type`: `provider`, `nonprofit_provider`, `official_directory`, `official_program_directory`, `official_program_information`, `official_information`, `information_source`, `evidence_guideline`, or `program`.
- `official_url`: institutional source.
- `status`: `unreviewed`, `verified`, `stale`, or `withdrawn`.
- `last_checked_at`: ISO date of actual operational verification, not import date; null if not yet checked.
- `eligibility`, `cost`, `availability`: confirmed facts or `unknown`.
- `review_notes`: missing information and verification steps.

Never include user health information or personal contact details. A provider's website does not prove current availability, eligibility, accreditation, or partnership with BlumTherapy.
