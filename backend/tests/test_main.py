import pytest
from pydantic import ValidationError

from app.main import PlanRequest, make_plan


def test_low_energy_plan_is_small_and_non_clinical():
    result = make_plan(PlanRequest(energy="low", time_minutes=20, focus="care", language="en"))
    assert result.minutes == 5
    assert len(result.steps) == 1
    assert "does not" in result.rationale


def test_connection_plan_matches_selected_focus_and_language():
    result = make_plan(PlanRequest(energy="steady", time_minutes=10, focus="connection", language="es"))
    assert result.title == "Acercarte sin presión"
    assert len(result.steps) == 2
    assert "no estima diagnósticos" in result.rationale


def test_minutes_are_bounded_by_schema():
    with pytest.raises(ValidationError):
        PlanRequest(energy="high", time_minutes=100, focus="learning", language="en")
