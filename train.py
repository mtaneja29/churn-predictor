import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

NUMERIC = ["tenure", "MonthlyCharges", "TotalCharges"]
CATEGORICAL = ["Contract"]

df = pd.read_csv("Telco-Customer-Churn.csv")

# TotalCharges has 11 blank-string rows (new customers, tenure=0) -> coerce to NaN, drop
df["TotalCharges"] = pd.to_numeric(df["TotalCharges"], errors="coerce")
df = df.dropna(subset=["TotalCharges"])

X = df[NUMERIC + CATEGORICAL]
y = df["Churn"].map({"Yes": 1, "No": 0})

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, stratify=y, random_state=42
)

model = Pipeline([
    ("prep", ColumnTransformer([
        ("num", StandardScaler(), NUMERIC),
        ("cat", OneHotEncoder(), CATEGORICAL),
    ])),
    ("clf", LogisticRegression(class_weight="balanced")),
])

model.fit(X_train, y_train)

print(classification_report(y_test, model.predict(X_test), target_names=["No churn", "Churn"]))

joblib.dump(model, "churn_model.joblib")
print("Saved churn_model.joblib")
