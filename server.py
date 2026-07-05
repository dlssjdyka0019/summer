"""
充电站管理系统 - 后端服务
FastAPI + MySQL + JWT 认证
运行: python server.py
"""

import hashlib
import hmac
import json
import os
import secrets
import time
from datetime import datetime, timedelta
from functools import wraps

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import pymysql

app = FastAPI(title="充电站管理系统")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# MySQL 连接配置
DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "123456",
    "database": "charging",
    "charset": "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor,
}

# ==================== 数据库 ====================

def get_db():
    """获取 MySQL 数据库连接"""
    conn = pymysql.connect(**DB_CONFIG)
    return conn

def init_db():
    conn = get_db()
    cursor = conn.cursor()
    try:
        # 创建 users 表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id INT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(10) NOT NULL DEFAULT 'user',
                phone VARCHAR(20) DEFAULT '',
                created_at DATETIME DEFAULT NOW()
            )
        ''')

        # 创建 stations 表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS stations (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                address VARCHAR(500) DEFAULT '',
                longitude DOUBLE DEFAULT 0,
                latitude DOUBLE DEFAULT 0,
                operator VARCHAR(100) DEFAULT '',
                total_spots INT DEFAULT 0,
                available_spots INT DEFAULT 0,
                power DOUBLE DEFAULT 0,
                charger_types VARCHAR(500) DEFAULT '[]',
                price DOUBLE DEFAULT 0,
                open_hours VARCHAR(50) DEFAULT '00:00-24:00',
                phone VARCHAR(30) DEFAULT '',
                status VARCHAR(20) DEFAULT 'open',
                description VARCHAR(1000) DEFAULT '',
                created_at DATETIME DEFAULT NOW(),
                updated_at DATETIME DEFAULT NOW()
            )
        ''')

        # 创建 piles 表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS piles (
                id VARCHAR(50) PRIMARY KEY,
                station_id VARCHAR(50) NOT NULL,
                name VARCHAR(200) NOT NULL,
                type VARCHAR(20) DEFAULT '快充',
                power DOUBLE DEFAULT 0,
                price DOUBLE DEFAULT 0,
                status VARCHAR(20) DEFAULT 'open',
                FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
            )
        ''')

        # 创建 charging_sessions 表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS charging_sessions (
                id VARCHAR(50) PRIMARY KEY,
                user_id INT NOT NULL,
                station_id VARCHAR(50) NOT NULL,
                station_name VARCHAR(200) DEFAULT '',
                pile_id VARCHAR(50) NOT NULL,
                pile_name VARCHAR(200) DEFAULT '',
                duration DOUBLE DEFAULT 0,
                amount DOUBLE DEFAULT 0,
                start_time DATETIME DEFAULT NOW(),
                end_time DATETIME,
                status VARCHAR(20) DEFAULT 'active',
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (station_id) REFERENCES stations(id),
                FOREIGN KEY (pile_id) REFERENCES piles(id)
            )
        ''')
        conn.commit()

        # Create default admin if not exists
        cursor.execute("SELECT id FROM users WHERE username='admin'")
        admin = cursor.fetchone()
        if not admin:
            salt = secrets.token_hex(8)
            pw = hash_password("admin123", salt)
            cursor.execute(
                "INSERT INTO users (username, password, role) VALUES (%s, %s, %s)",
                ("admin", f"{salt}:{pw}", "admin")
            )
            conn.commit()
            print("[OK] 默认管理员账号: admin / admin123")

        # Insert demo data if no stations exist
        cursor.execute("SELECT COUNT(*) AS cnt FROM stations")
        count = cursor.fetchone()["cnt"]
        if count == 0:
            insert_demo_data(conn)

    finally:
        cursor.close()
        conn.close()

def hash_password(password: str, salt: str = None) -> str:
    if salt is None:
        salt = secrets.token_hex(8)
    return hmac.new(salt.encode(), password.encode(), hashlib.sha256).hexdigest()

def verify_password(password: str, stored: str) -> bool:
    salt, pw_hash = stored.split(":", 1)
    return hmac.compare_digest(hash_password(password, salt), pw_hash)

def insert_demo_data(conn):
    cursor = conn.cursor()
    try:
        stations = [
            ("d1", "大庆万达广场充电站", "萨尔图区东风路15号万达广场B1", 125.1036, 46.5982, "特来电", 6, 3, 120, 1.20, "06:00-23:00", "0459-6116789", "open", "万达广场地下停车场"),
            ("d2", "萨尔图机场充电站", "萨尔图区机场路1号P1区", 125.1347, 46.7433, "国网电动", 4, 3, 60, 1.50, "00:00-24:00", "0459-6390222", "open", "24小时营业"),
            ("d3", "大庆东站充电站", "龙凤区龙凤大街东站南广场", 125.1281, 46.5436, "星星充电", 6, 3, 180, 1.35, "00:00-24:00", "", "open", "支持超快充"),
            ("d4", "让胡路昆仑大街充电站", "让胡路区昆仑大街68号", 124.8847, 46.6269, "国网电动", 4, 3, 60, 0.90, "08:00-20:00", "", "open", "居民区充电"),
            ("d5", "大庆市政府充电站", "萨尔图区世纪大道市政府院内", 125.0382, 46.5897, "特来电", 3, 1, 120, 1.10, "07:00-21:00", "", "open", "市政府东侧停车场"),
            ("d6", "龙凤世纪大道充电站", "龙凤区世纪大道与龙永路交叉口", 125.1397, 46.5581, "星星充电", 3, 1, 120, 1.25, "00:00-24:00", "", "closed", "设备升级中"),
            ("d7", "红岗区充电站", "红岗区萨大路红岗商城", 124.9756, 46.3698, "国网电动", 3, 2, 60, 0.85, "06:00-22:00", "", "open", "红岗商城西侧"),
            ("d8", "大同区同阳路充电站", "大同区同阳路178号", 124.7983, 46.0487, "特来电", 3, 1, 120, 1.15, "07:00-21:00", "", "maintenance", "维护中"),
        ]
        piles_data = [
            ("d1", [("p1_1","1号桩","快充",120,1.20,"open"),("p1_2","2号桩","快充",120,1.20,"open"),("p1_3","3号桩","快充",120,1.20,"in_use"),
                    ("p1_4","4号桩","慢充",7,0.90,"open"),("p1_5","5号桩","慢充",7,0.90,"maintenance"),("p1_6","6号桩","慢充",7,0.90,"open")]),
            ("d2", [("p2_1","1号桩","快充",60,1.50,"open"),("p2_2","2号桩","快充",60,1.50,"open"),("p2_3","3号桩","快充",60,1.50,"open"),("p2_4","4号桩","快充",60,1.50,"in_use")]),
            ("d3", [("p3_1","1号桩","超快充",180,1.35,"open"),("p3_2","2号桩","超快充",180,1.35,"in_use"),("p3_3","3号桩","快充",120,1.20,"open"),
                    ("p3_4","4号桩","快充",120,1.20,"maintenance"),("p3_5","5号桩","快充",120,1.20,"open"),("p3_6","6号桩","快充",120,1.20,"open")]),
            ("d4", [("p4_1","1号桩","慢充",7,0.90,"open"),("p4_2","2号桩","慢充",7,0.90,"open"),("p4_3","3号桩","慢充",7,0.90,"open"),("p4_4","4号桩","慢充",7,0.90,"closed")]),
            ("d5", [("p5_1","1号桩","快充",120,1.10,"in_use"),("p5_2","2号桩","快充",120,1.10,"in_use"),("p5_3","3号桩","慢充",7,0.85,"open")]),
            ("d6", [("p6_1","1号桩","快充",120,1.25,"closed"),("p6_2","2号桩","快充",120,1.25,"closed"),("p6_3","3号桩","慢充",7,0.95,"closed")]),
            ("d7", [("p7_1","1号桩","慢充",7,0.85,"open"),("p7_2","2号桩","慢充",7,0.85,"open"),("p7_3","3号桩","慢充",7,0.85,"in_use")]),
            ("d8", [("p8_1","1号桩","快充",120,1.15,"maintenance"),("p8_2","2号桩","快充",120,1.15,"maintenance"),("p8_3","3号桩","慢充",7,0.85,"maintenance")]),
        ]
        for s in stations:
            cursor.execute('''INSERT INTO stations (id,name,address,longitude,latitude,operator,total_spots,available_spots,power,price,open_hours,phone,status,description)
                VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)''', s)
        for sid, piles in piles_data:
            for p in piles:
                cursor.execute("INSERT INTO piles (id,station_id,name,type,power,price,status) VALUES (%s,%s,%s,%s,%s,%s,%s)",
                             (p[0], sid, p[1], p[2], p[3], p[4], p[5]))
        conn.commit()
        print("[OK] 已初始化8个演示充电站")
    finally:
        cursor.close()

# ==================== Token ====================

TOKENS = {}  # token -> user_id, role, expires_at

def create_token(user_id: int, role: str) -> str:
    token = secrets.token_hex(32)
    TOKENS[token] = {
        "user_id": user_id,
        "role": role,
        "expires_at": time.time() + 86400 * 7  # 7 days
    }
    return token

def get_user_from_token(token: str):
    data = TOKENS.get(token)
    if not data or data["expires_at"] < time.time():
        if token in TOKENS:
            del TOKENS[token]
        return None
    return data

def require_auth(request: Request):
    auth = request.headers.get("Authorization", "")
    if not auth.startswith("Bearer "):
        raise HTTPException(401, "未登录")
    user = get_user_from_token(auth[7:])
    if not user:
        raise HTTPException(401, "登录已过期")
    return user

def require_admin(request: Request):
    user = require_auth(request)
    if user["role"] != "admin":
        raise HTTPException(403, "需要管理员权限")
    return user

# ==================== Pydantic Models ====================

class RegisterReq(BaseModel):
    username: str
    password: str
    phone: str = ""

class LoginReq(BaseModel):
    username: str
    password: str

class StationReq(BaseModel):
    id: str = None
    name: str
    address: str = ""
    longitude: float = 0
    latitude: float = 0
    operator: str = ""
    total_spots: int = 0
    available_spots: int = 0
    power: float = 0
    charger_types: list = []
    price: float = 0
    open_hours: str = "00:00-24:00"
    phone: str = ""
    status: str = "open"
    description: str = ""

class PileReq(BaseModel):
    id: str = None
    name: str
    type: str = "快充"
    power: float = 0
    price: float = 0
    status: str = "open"

class SessionReq(BaseModel):
    station_id: str
    pile_id: str
    duration: float = 1
    amount: float = 0

# ==================== Auth APIs ====================

@app.post("/api/register")
def register(req: RegisterReq):
    if not req.username or not req.password:
        raise HTTPException(400, "用户名和密码不能为空")
    if len(req.username) < 2:
        raise HTTPException(400, "用户名至少2个字符")
    if len(req.password) < 4:
        raise HTTPException(400, "密码至少4个字符")

    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT id FROM users WHERE username=%s", (req.username,))
        if cursor.fetchone():
            raise HTTPException(400, "用户名已存在")

        pw = hash_password(req.password)
        cursor.execute("INSERT INTO users (username, password, role, phone) VALUES (%s,%s,%s,%s)",
                     (req.username, pw, "user", req.phone))
        conn.commit()
        cursor.execute("SELECT * FROM users WHERE username=%s", (req.username,))
        user = cursor.fetchone()
        token = create_token(user["id"], "user")
        return {"ok": True, "token": token, "user": {"id": user["id"], "username": user["username"], "role": "user", "phone": user["phone"]}}
    finally:
        cursor.close()
        conn.close()

@app.post("/api/login")
def login(req: LoginReq):
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM users WHERE username=%s", (req.username,))
        user = cursor.fetchone()
        if not user:
            raise HTTPException(400, "用户名或密码错误")
        if not verify_password(req.password, user["password"]):
            raise HTTPException(400, "用户名或密码错误")

        token = create_token(user["id"], user["role"])
        return {"ok": True, "token": token, "user": {"id": user["id"], "username": user["username"], "role": user["role"], "phone": user["phone"]}}
    finally:
        cursor.close()
        conn.close()

@app.get("/api/me")
def me(request: Request):
    user = require_auth(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM users WHERE id=%s", (user["user_id"],))
        u = cursor.fetchone()
        if not u:
            raise HTTPException(404, "用户不存在")
        return {"id": u["id"], "username": u["username"], "role": u["role"], "phone": u["phone"]}
    finally:
        cursor.close()
        conn.close()

# ==================== Station APIs ====================

def row_to_station(row) -> dict:
    return {
        "id": row["id"], "name": row["name"], "address": row["address"],
        "longitude": row["longitude"], "latitude": row["latitude"],
        "operator": row["operator"], "totalSpots": row["total_spots"],
        "availableSpots": row["available_spots"], "power": row["power"],
        "chargerTypes": json.loads(row["charger_types"] or "[]"),
        "price": row["price"], "openHours": row["open_hours"],
        "phone": row["phone"], "status": row["status"],
        "desc": row["description"], "created": 0, "updated": 0
    }

@app.get("/api/stations")
def get_stations():
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM stations ORDER BY name")
        stations = cursor.fetchall()
        result = []
        for s in stations:
            station = row_to_station(s)
            cursor.execute("SELECT * FROM piles WHERE station_id=%s", (s["id"],))
            piles = cursor.fetchall()
            station["piles"] = [{"id": p["id"], "name": p["name"], "type": p["type"],
                                 "power": p["power"], "price": p["price"], "status": p["status"]} for p in piles]
            # Update stats from piles
            station["totalSpots"] = len(piles)
            station["availableSpots"] = sum(1 for p in piles if p["status"] == "open")
            result.append(station)
        return result
    finally:
        cursor.close()
        conn.close()

@app.post("/api/stations")
def create_station(req: StationReq, request: Request):
    require_admin(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        sid = req.id or ("s" + secrets.token_hex(5))
        cursor.execute('''INSERT INTO stations (id,name,address,longitude,latitude,operator,total_spots,available_spots,power,charger_types,price,open_hours,phone,status,description)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)''',
            (sid, req.name, req.address, req.longitude, req.latitude, req.operator,
             req.total_spots, req.available_spots, req.power,
             json.dumps(req.charger_types), req.price, req.open_hours,
             req.phone, req.status, req.description))
        conn.commit()
        return {"ok": True, "id": sid}
    finally:
        cursor.close()
        conn.close()

@app.put("/api/stations/{sid}")
def update_station(sid: str, req: StationReq, request: Request):
    require_admin(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute('''UPDATE stations SET name=%s,address=%s,longitude=%s,latitude=%s,operator=%s,
            total_spots=%s,available_spots=%s,power=%s,charger_types=%s,price=%s,open_hours=%s,
            phone=%s,status=%s,description=%s,updated_at=NOW()
            WHERE id=%s''',
            (req.name, req.address, req.longitude, req.latitude, req.operator,
             req.total_spots, req.available_spots, req.power,
             json.dumps(req.charger_types), req.price, req.open_hours,
             req.phone, req.status, req.description, sid))
        conn.commit()
        return {"ok": True}
    finally:
        cursor.close()
        conn.close()

@app.delete("/api/stations/{sid}")
def delete_station(sid: str, request: Request):
    require_admin(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("DELETE FROM piles WHERE station_id=%s", (sid,))
        cursor.execute("DELETE FROM stations WHERE id=%s", (sid,))
        conn.commit()
        return {"ok": True}
    finally:
        cursor.close()
        conn.close()

# ==================== Pile APIs ====================

@app.post("/api/stations/{sid}/piles")
def add_pile(sid: str, req: PileReq, request: Request):
    require_admin(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        pid = req.id or ("p" + secrets.token_hex(4))
        cursor.execute("INSERT INTO piles (id,station_id,name,type,power,price,status) VALUES (%s,%s,%s,%s,%s,%s,%s)",
                     (pid, sid, req.name, req.type, req.power, req.price, req.status))
        conn.commit()
        return {"ok": True, "id": pid}
    finally:
        cursor.close()
        conn.close()

@app.put("/api/piles/{pid}")
def update_pile(pid: str, req: PileReq, request: Request):
    require_admin(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("UPDATE piles SET name=%s,type=%s,power=%s,price=%s,status=%s WHERE id=%s",
                     (req.name, req.type, req.power, req.price, req.status, pid))
        conn.commit()
        return {"ok": True}
    finally:
        cursor.close()
        conn.close()

@app.delete("/api/piles/{pid}")
def delete_pile(pid: str, request: Request):
    require_admin(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("DELETE FROM piles WHERE id=%s", (pid,))
        conn.commit()
        return {"ok": True}
    finally:
        cursor.close()
        conn.close()

# ==================== Session APIs ====================

@app.get("/api/sessions")
def get_sessions(request: Request):
    user = require_auth(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM charging_sessions WHERE user_id=%s ORDER BY start_time DESC", (user["user_id"],))
        rows = cursor.fetchall()
        return [{"id": r["id"], "stationId": r["station_id"], "stationName": r["station_name"],
                 "pileId": r["pile_id"], "pileName": r["pile_name"], "duration": r["duration"],
                 "amount": r["amount"], "startTime": r["start_time"].strftime("%Y-%m-%d %H:%M:%S") if r["start_time"] else "",
                 "endTime": r["end_time"].strftime("%Y-%m-%d %H:%M:%S") if r["end_time"] else "",
                 "status": r["status"]} for r in rows]
    finally:
        cursor.close()
        conn.close()

@app.post("/api/sessions")
def create_session(req: SessionReq, request: Request):
    user = require_auth(request)
    if user["role"] != "user":
        raise HTTPException(403, "只有用户可以进行充电")
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM stations WHERE id=%s", (req.station_id,))
        station = cursor.fetchone()
        if not station:
            raise HTTPException(404, "充电站不存在")
        cursor.execute("SELECT * FROM piles WHERE id=%s AND station_id=%s", (req.pile_id, req.station_id))
        pile = cursor.fetchone()
        if not pile:
            raise HTTPException(404, "充电桩不存在")
        if pile["status"] != "open":
            raise HTTPException(400, "该充电桩当前不可用")

        # Update pile status
        cursor.execute("UPDATE piles SET status='in_use' WHERE id=%s", (req.pile_id,))

        # Create session
        sid = "ch" + secrets.token_hex(5)
        cursor.execute('''INSERT INTO charging_sessions (id,user_id,station_id,station_name,pile_id,pile_name,duration,amount,status)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)''',
            (sid, user["user_id"], req.station_id, station["name"], req.pile_id, pile["name"],
             req.duration, req.amount, "active"))
        conn.commit()
        return {"ok": True, "id": sid}
    finally:
        cursor.close()
        conn.close()

@app.put("/api/sessions/{sid}/stop")
def stop_session(sid: str, request: Request):
    user = require_auth(request)
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM charging_sessions WHERE id=%s AND user_id=%s", (sid, user["user_id"]))
        session = cursor.fetchone()
        if not session:
            raise HTTPException(404, "记录不存在")
        if session["status"] != "active":
            raise HTTPException(400, "该充电已结束")

        cursor.execute("UPDATE charging_sessions SET status='completed', end_time=NOW() WHERE id=%s", (sid,))
        cursor.execute("UPDATE piles SET status='open' WHERE id=%s", (session["pile_id"],))
        conn.commit()
        return {"ok": True}
    finally:
        cursor.close()
        conn.close()

# ==================== Startup ====================

@app.on_event("startup")
def startup():
    init_db()
    print("[OK] 服务启动: http://localhost:8000")
    print("[OK] API 文档: http://localhost:8000/docs")
    print("[OK] 管理员: admin / admin123")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
