# AGRISELF: Autonomous AI Micro-Farming & Precision Agritech System

> **Executive Pitch Sheet Implementation**: An end-to-end, AI-powered autonomous farming platform designed to convert small-to-medium agricultural land into self-managing, high-yield ecosystems.

---

## 🌟 Executive Key Performance Metrics

- **`+35%` Average Yield Increase** — Micro-nutrient dosing and computer vision disease detection.
- **`-40%` Water & Chemical Reduction** — Precision sub-surface micro-drip tailored to real-time soil intake rates.
- **`70%` Manual Labor Hours Saved** — Autonomous smart valves, micro-injectors, and drone aerial surveys.
- **`1.2 Years` Estimated Payback Period** — High-ROI hardware setup + SaaS analytics subscription.

---

## 🏗️ Architecture & Core Components

```
agriself-platform/
├── backend/
│   ├── ai_models/
│   │   ├── train_models.py       # ML Training pipeline (RandomForest & Regressors)
│   │   ├── inference.py          # Real-time multi-model inference engine
│   │   └── weights/              # Serialized model weights (.joblib)
│   │       ├── crop_vision_model.joblib
│   │       ├── micro_dosing_model.joblib
│   │       └── yield_harvest_model.joblib
│   ├── farm_state.py             # Multi-zone digital twin, IoT sensors, actuator ledger
│   └── main.py                   # FastAPI REST API & WebSocket telemetry server
├── frontend/
│   ├── index.html                # Semantic HTML5 web application
│   ├── styles.css                # Modern agritech obsidian & emerald design system
│   ├── app.js                    # Dynamic interaction, Chart.js graphs, WebSocket client
│   └── sample_data/              # Sample agricultural leaves for 1-click test scans
├── start_agriself.py             # One-click launcher script
└── README.md
```

---

## 🧠 The 3 Machine Learning AI Models

### 1. `CropVisionAI` (Computer Vision & Multispectral Diagnostic)
- **Model**: Scikit-Learn Ensemble & Feature Extractor (95.56% Accuracy).
- **Features Extracted**: RGB channel moments, Laplacian texture edge variance, and simulated multispectral indices ($NDVI$, $NDRE$, $GNDVI$, $Chlorophyll\ Index$).
- **Classes**:
  1. *Healthy Crop (Optimal Vigour)*
  2. *Early Blight (Alternaria solani)*
  3. *Late Blight (Phytophthora infestans)*
  4. *Powdery Mildew (Erysiphe cichoracearum)*
  5. *Nitrogen Deficiency (Severe Chlorosis)*
  6. *Potassium Deficiency (Marginal Necrosis)*
  7. *Iron Chlorosis (Interveinal Yellowing)*
  8. *Two-Spotted Spider Mite (Tetranychus urticae)*
- **Outputs**: Anomaly bounding boxes, confidence %, severity ratings, and automated 3-pillar prescriptions (*Immediate Agronomic Action, Organic Biological Solution, Automated Smart Valve Protocol*).

### 2. `MicroDosingAI` (Dynamic Irrigation & Precision Fertigation Regressor)
- **Model**: Multi-Output Random Forest Regressor ($R^2 = 0.642$).
- **Input Parameters**: Soil moisture %, soil temp, ambient temp, relative humidity %, solar radiation ($W/m^2$), soil N-P-K (ppm), soil pH, 24h rain forecast, and phenological crop growth stage.
- **Outputs**: Exact water requirement ($L/m^2$), smart valve open duration (min), dynamic Nitrogen, Phosphorus, Potassium micro-dose injections ($g/m^2$), and crop stress gauge.

### 3. `YieldHarvestAI` (Predictive Market Timing & Harvest Regressor)
- **Model**: Time-Series & Crop Multi-Factor Regressor ($R^2 = 0.749$).
- **Inputs**: Crop variety, days planted, cumulative Growing Degree Days (GDD), seasonal mean NDVI, thermal stress events count, nutrient balance, spot price, and 7-day price momentum.
- **Outputs**: Optimal harvest window days from today, predicted yield tons/acre, gross revenue forecast, and net profit boost per acre.

---

## 🚀 Quick Start Guide

### 1. Run via Launcher
```bash
python agriself-platform/start_agriself.py
```
*(Automatically verifies model weights, starts FastAPI backend, and opens your browser at `http://127.0.0.1:8000`)*

### 2. Manual Start
```bash
# Optional: re-train AI models
python agriself-platform/backend/ai_models/train_models.py

# Launch FastAPI Server
python agriself-platform/backend/main.py
```
Open **`http://127.0.0.1:8000`** in any modern web browser.

---

## 📡 REST API & WebSocket Specifications

| Endpoint | Method | Description |
|---|---|---|
| `GET /api/status` | GET | System health, active models, drone status, pitch KPIs |
| `GET /api/zones` | GET | Digital Twin real-time telemetry for all 4 zones |
| `POST /api/ai/diagnose` | POST | Leaf image or preset sample computer vision diagnosis |
| `POST /api/ai/micro-dose` | POST | Dynamic micro-dosing and water dosage calculations |
| `POST /api/ai/predict-harvest` | POST | Optimal harvest window and financial yield projections |
| `POST /api/actuators/control` | POST | Direct trigger for smart valves & micro-dosing pumps |
| `POST /api/mode/toggle-autonomous` | POST | Toggle between Autonomous Auto-Pilot & Manual Override |
| `POST /api/simulation/tick` | POST | Advance micro-climate simulation step |
| `GET /api/logs/autonomous-ledger` | GET | Immutable log of AI actions and valve trigger events |
| `GET /api/market/trends` | GET | Commodity pricing trends & AI harvest timing signals |
| `WS /ws/telemetry` | WebSocket | Live streaming sensor telemetry & drone coordinates |
