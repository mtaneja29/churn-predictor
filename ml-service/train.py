import json

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, f1_score, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

NUMERIC = ["tenure", "MonthlyCharges", "TotalCharges", "SeniorCitizen"]
CATEGORICAL = [
    "gender", "Partner", "Dependents", "PhoneService", "MultipleLines",
    "InternetService", "OnlineSecurity", "OnlineBackup", "DeviceProtection",
    "TechSupport", "StreamingTV", "StreamingMovies", "Contract",
    "PaperlessBilling", "PaymentMethod",
]
MODEL_VERSION = "2.0-full-features"

df = pd.read_csv("Telco-Customer-Churn.csv")

# TotalCharges has 11 blank-string rows (new customers, tenure=0) -> coerce to NaN, drop
df["TotalCharges"] = pd.to_numeric(df["TotalCharges"], errors="coerce")
df = df.dropna(subset=["TotalCharges"])

X = df[NUMERIC + CATEGORICAL]
y = df["Churn"].map({"Yes": 1, "No": 0})

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, stratify=y, random_state=42
)

# identical preprocessing for both candidates; only the final stage differs
preprocess = ColumnTransformer([
    ("num", StandardScaler(), NUMERIC),
    ("cat", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL),
])

candidates = {
    "logistic_regression": LogisticRegression(class_weight="balanced", max_iter=2000),
    "gradient_boosting": HistGradientBoostingClassifier(class_weight="balanced", random_state=42),
}

results = {}
for name, clf in candidates.items():
    model = Pipeline([("prep", preprocess), ("clf", clf)])
    model.fit(X_train, y_train)
    y_pred = model.predict(X_test)
    y_prob = model.predict_proba(X_test)[:, 1]
    results[name] = {
        "model": model,
        "churn_f1": f1_score(y_test, y_pred),
        "roc_auc": roc_auc_score(y_test, y_prob),
    }
    print(f"=== {name} ===")
    print(classification_report(y_test, y_pred, target_names=["No churn", "Churn"]))
    print(f"ROC-AUC: {results[name]['roc_auc']:.4f}\n")

# prefer the simpler, interpretable model unless boosting wins F1 by a real margin
if results["gradient_boosting"]["churn_f1"] - results["logistic_regression"]["churn_f1"] > 0.01:
    winner = "gradient_boosting"
else:
    winner = "logistic_regression"
print(f"Winner: {winner} "
      f"(F1 {results[winner]['churn_f1']:.4f}, AUC {results[winner]['roc_auc']:.4f})")

joblib.dump(results[winner]["model"], "churn_model.joblib")
meta = {
    "model_version": MODEL_VERSION,
    "algorithm": winner,
    "churn_f1": round(results[winner]["churn_f1"], 4),
    "roc_auc": round(results[winner]["roc_auc"], 4),
    "numeric_features": NUMERIC,
    "categorical_features": CATEGORICAL,
}
with open("model_meta.json", "w") as f:
    json.dump(meta, f, indent=2)
print("Saved churn_model.joblib and model_meta.json")
