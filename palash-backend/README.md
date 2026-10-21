# PALASH Backend (M1 Server)

FastAPI backend server for PALASH Vernacular Pedagogy & Translation.

## Requirements & Environment

- Python 3.10+
- PyTorch with CUDA support (optional for GPU acceleration)
- Virtual Environment located at `venv`

## Server Startup Instructions

### 1. LAN Access Mode (Recommended for Multi-Device / Tablet Demo)

To make the backend reachable from Android tablets or other devices on the same Wi-Fi / Local Area Network (LAN):

```bash
# Using uvicorn directly
C:\Users\saich\Downloads\SIH\palash-backend\venv\Scripts\uvicorn app.main:app --host 0.0.0.0 --port 8000

# OR using Python directly
C:\Users\saich\Downloads\SIH\palash-backend\venv\Scripts\python.exe -m app.main
```

- **Binding Address:** `0.0.0.0` (Listens on all network interfaces)
- **Port:** `8000`
- **Tablet Base URL Format:** `http://<DEVELOPMENT_PC_LAN_IP>:8000/`
  - Example: `http://172.20.4.38:8000/`

### 2. Localhost Only Mode (Single-Machine Testing)

For local development or isolated testing on the host PC:

```bash
C:\Users\saich\Downloads\SIH\palash-backend\venv\Scripts\uvicorn app.main:app --host 127.0.0.1 --port 8000
```

## Windows Firewall Configuration

If external devices (such as an Android tablet over Wi-Fi) cannot reach `http://<PC_LAN_IP>:8000/health`, ensure an Inbound Rule is configured in Windows Defender Firewall:

1. Open **Windows Defender Firewall with Advanced Security**.
2. Select **Inbound Rules** -> **New Rule...**
3. Select **Port** -> **TCP** -> Specific local ports: **8000**.
4. Action: **Allow the connection**.
5. Profile: Check **Domain**, **Private** (and **Public** if using local Wi-Fi hotspot).
6. Name: `PALASH Backend API (TCP 8000)`.

*Note: Do not run automated script modifications against Windows Firewall rules.*

## API Endpoints

- `GET  /health` - Service health status check
- `GET  /lessons?grade=2&subject=mathematics` - Educational lessons
- `GET  /worksheets?lesson_id=1` - Worksheets
- `GET  /flashcards?lesson_id=1` - Flashcards
- `POST /translate` - Translation from Hindi (`hin_Deva`) to Santali (`sat_Olck`)
