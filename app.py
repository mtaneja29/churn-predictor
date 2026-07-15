from typing import Literal

import joblib
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()
model = joblib.load("churn_model.joblib")  # once at startup, not per request


class CustomerFeatures(BaseModel):
    tenure: int
    MonthlyCharges: float
    TotalCharges: float
    Contract: Literal["Month-to-month", "One year", "Two year"]


@app.post("/predict")
def predict(customer: CustomerFeatures):
    row = pd.DataFrame([customer.model_dump()])
    prob = model.predict_proba(row)[0][1]
    band = "LOW" if prob < 0.3 else "MEDIUM" if prob < 0.6 else "HIGH"
    return {"churn_probability": round(float(prob), 4), "risk_band": band}
