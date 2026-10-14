from __future__ import annotations

from datetime import date

# Curated public starting points, not a guarantee of eligibility or availability.
# Keep country, source and review date visible to clients.
COMMON_REVIEW_DATE = date(2026, 10, 13)


def catalog_for(brand: str) -> list[dict[str, str]]:
    if brand.lower().startswith("blumtherapy"):
        return [
            {
                "id": "us-find-treatment",
                "brand": "blumtherapy",
                "title": "FindTreatment.gov",
                "summary": "Official U.S. treatment locator. Confirm services, cost, eligibility and current availability directly.",
                "category": "Support",
                "country": "US",
                "source_name": "U.S. Government",
                "url": "https://findtreatment.gov/",
                "reviewed_on": str(COMMON_REVIEW_DATE),
            },
            {
                "id": "uk-nhs-drug-support",
                "brand": "blumtherapy",
                "title": "NHS: getting help for drug addiction",
                "summary": "Official information and starting points for support in the UK. Local pathways differ by nation.",
                "category": "Support",
                "country": "UK",
                "source_name": "NHS",
                "url": "https://www.nhs.uk/live-well/addiction-support/drug-addiction-getting-help/",
                "reviewed_on": str(COMMON_REVIEW_DATE),
            },
            {
                "id": "ca-health-canada-treatment",
                "brand": "blumtherapy",
                "title": "Health Canada: treatment",
                "summary": "Explore official treatment information and check province or territory-specific services.",
                "category": "Support",
                "country": "CA",
                "source_name": "Government of Canada",
                "url": "https://www.canada.ca/en/health-canada/services/substance-use/treatment.html",
                "reviewed_on": str(COMMON_REVIEW_DATE),
            },
            {
                "id": "who-treatment-standards",
                "brand": "blumtherapy",
                "title": "WHO: treatment standards",
                "summary": "International guidance for understanding evidence-based care. This is information, not a local service.",
                "category": "Learning",
                "country": "GLOBAL",
                "source_name": "World Health Organization",
                "url": "https://www.who.int/publications/i/item/international-standards-for-the-treatment-of-drug-use-disorders",
                "reviewed_on": str(COMMON_REVIEW_DATE),
            },
        ]
    return [
        {
            "id": "co-minsalud-substances",
            "brand": "blumhelps",
            "title": "Orientación y ruta de atención",
            "summary": "Información del Ministerio de Salud sobre atención por consumo de sustancias. Confirma la ruta aplicable a tu territorio.",
            "category": "Apoyo",
            "country": "CO",
            "source_name": "Ministerio de Salud y Protección Social",
            "url": "https://minsalud.gov.co/salud/publica/salud-mental/Paginas/convivencia-desarrollo-humano-sustancias-psicoactivas.aspx",
            "reviewed_on": str(COMMON_REVIEW_DATE),
        },
        {
            "id": "co-minsalud-ips",
            "brand": "blumhelps",
            "title": "Directorio oficial de IPS",
            "summary": "Localiza prestadores y confirma servicios, cobertura, costos y disponibilidad directamente.",
            "category": "Apoyo",
            "country": "CO",
            "source_name": "Ministerio de Salud y Protección Social",
            "url": "https://prestadores.minsalud.gov.co/directorio/consultaIPS.aspx?pageTitle=Directorio",
            "reviewed_on": str(COMMON_REVIEW_DATE),
        },
        {
            "id": "co-sena-training",
            "brand": "blumhelps",
            "title": "Formación SENA",
            "summary": "Consulta cursos e inscripciones vigentes. La oferta depende de cada convocatoria.",
            "category": "Aprendizaje",
            "country": "CO",
            "source_name": "SENA Betowa",
            "url": "https://betowa.sena.edu.co/",
            "reviewed_on": str(COMMON_REVIEW_DATE),
        },
        {
            "id": "co-sena-employment",
            "brand": "blumhelps",
            "title": "Agencia Pública de Empleo",
            "summary": "Revisa los servicios públicos de orientación e intermediación laboral.",
            "category": "Oportunidades",
            "country": "CO",
            "source_name": "SENA",
            "url": "https://ape.sena.edu.co/Paginas/APE.aspx",
            "reviewed_on": str(COMMON_REVIEW_DATE),
        },
    ]
