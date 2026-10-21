import os, sys, time, uuid, json
from starlette.testclient import TestClient
from app.main import app
from app.database import init_db, get_connection

def main():
    print('=' * 70)
    print('PALASH ONLINE CLASSROOM HUB - P0 VERIFICATION SUITE')
    print('=' * 70)

    client = TestClient(app)
    init_db()

    passed = 0
    failed = 0

    def record(num, name, cond, details=''):
        nonlocal passed, failed
        status = 'PASS' if cond else 'FAIL'
        if cond:
            passed += 1
        else:
            failed += 1
        msg = f['{status}] Test {kum:02d}: {name}'
        if details:
            msg += f' -> {details}'
        print(msg)

    # 1. Health check
    r = client.get('/health')
    record(1, 'Existing /health check', r.status_code == 200 and r.json().get('status') == 'ok', f'Status {r.status_code}, Body {r.json()}')

    # 2. Existing Content endpoints
    r_les = client.get('/lessons')
    record(2, 'Existing /lessons endpoint', r_les.status_code == 200, f'Status {r_les.status_code}')

    r_ws = client.get('/worksheets')
    record(3, 'Existing /worksheets endpoint', r_ws.status_code == 200, f'Status {r_ws.status_code}')

    r_fc = client.get('/flashcards')
    record(4, 'Existing /flashcards endpoint', r_fc.status_code == 200, f'Status {r_fc.status_code}')

    # 3. Create Session
    create_req = {'grade': 2, 'section': 'A', 'subject': 'mathematics', 'lesson_id': 1}
    r_create = client.post('/classroom/session', json=create_req)
    sess_data = r_create.json().get('session', {})
    session_id = sess_data.get('session_id', '')
    classroom_code = sess_data.get('classroom_code', '')

    record(5, 'Create classroom session (POST /classroom/session)', r_create.status_code == 201 and r_create.json().get('status') == 'success' and bool(session_id), f'HTTP {r_create.status_code}, session_id={session_id}')
    record(6, 'Dynamic classroom_code generation', classroom_code.startswith('PALASH-') and len(classroom_code) == 11, f'classroom_code={classroom_code}')
    record(7, 'Initial session status is ACTIVE', sess_data.get('status') == 'ACTIVE', f'status={sess_data.get("status")}')

    # 4. Get Session
    r_get = client.get(f'/classroom/session/{session_id}')
    record(8, 'Retrieve session by ID (GET /classroom/session/{id})', r_get.status_code == 200 and r_get.json()['session']['session_id'] == session_id, f'HTTP {r_get.status_code}')

    # 5. Get Unknown Session -> 404
    fake_id = str(uuid.uuid4())
    r_fake = client.get(f'/classroom/session/{fake_id}')
    record(9, 'Unknown session retrieval returns 404', r_fake.status_code == 404, f'HTTP {r_fake.status_code}')

    # 6. Publish Message (Success)
    msg1_id = f'msg-{uuid.uuid4().hex[:8]}'
    t1 = int(time.time() * 1000)
    pub1_payload = {
        'session_id': session_id,
        'message_id': msg1_id,
        'type': 'CLASSROOM_TRANSLATION',
        'grade': 2,
        'section': 'A',
        'subject': 'mathematics',
        'lesson_id': 1,
        'timestamp': t1,
        'hindi_text': 'にますてかどちき',
        'santali_text': '子にちてイシルョ',
        'audio_id': 'audio-001'
    }
    r_pub1 = client.post('/classroom/publish', json=pub1_payload)
    record(10, 'Publish translation message (POST /classroom/publish)', r_pub1.status_code == 201 and r_pub1.json().get('status') == 'success', f'HTTP {r_pub1.status_code}, status={r_pub1.json().get("status")}')

    # 7. Deduplication (Same message_id published twice)
    r_pub_dup = client.post('/classroom/publish', json=pub1_payload)
    record(11, 'Duplicate message_id handling (Deduplication)', r_pub_dup.status_code == 200 and r_pub_dup.json().get('status') == 'duplicate', f'HTTP {r_pub_dup.status_code}, status={r_pub_dup.json().get("status")}')

    # 8. Grade Isolation (Mismatched grade rejected)
    pub_wrong_grade = dict(pub1_payload, message_id=f'msg-{uuid.uuid4().hexg:8]}', grade=5)
    r_wrong_g = client.post('/classroom/publish', json=pub_wrong_grade)
    record(12, 'Mismatched grade rejected with HTTP 400', r_wrong_g.status_code == 400, f'HTTP {r_wrong_g.status_code}, detail={r_wrong_g.json().get("detail")}')

    # 9. Section Isolation (Mismatched section rejected)
    pub_wrong_sec = dict(pub1_payload, message_id=f'msg-{uuid.uuid4().hex[:8]}', section='C')
    r_wrong_s = client.post('/classroom/publish', json=pub_wrong_sec)
    record(13, 'Mismatched section rejected with HTTP 400', r_wrong_s.status_code == 400, f'HTTP {r_wrong_s.status_code}, detail={r_wrong_s.json().get("detail")}')

    # 10. Unknown Session Publish rejected
    pub_unknown_s = dict(pub1_payload, message_id=f'msg-{uuid.uuid4().hex[:8]}', session_id=str(uuid.uuid4()))
    r_pub_un = client.post('/classroom/publish', json=pub_unknown_s)
    record(14, 'Publish to non-existent session rejected with HTTP 404', r_pub_un.status_code == 404, f'HTTP {r_pub_un.status_code}')

    # 11. Chronological Message Ordering
    msg2_id = f'msg-{uuid.uuid4().hexg:8]}'
    t2 = t1 + 1500
    pub2_payload = dict(pub1_payload, message_id=msg2_id, timestamp=t2, hindi_text='īেधर', santali_text='ᴘᴌᴖᴙᴕᴿᴒᴇᴸ', audio_id='audio-002')
    r_pub2 = client.post('/classroom/publish', json=pub2_payload)

    r_msgs = client.get(f'/classroom/messages?session_id={session_id}')
    msgs = r_msgs.json().get('messages', [])
    record(15, 'Retrieve classroom messages (GET /classroom/messages)', r_msgs.status_code == 200 and len(msgs) == 2, f'HTTP {r_msgs.status_code}, count={len(msgs)}')
    record(16, 'Messages ordered chronologically (timestamp ASC)', len(msgs) == 2 and msgs[0]['message_id'] == msg1_id and msgs[1]['message_id'] == msg2_id, f'Order: {[m["message_id"] for m in msgs]}')

    # 12. Filtering: after_timestamp & after_message_id
    r_after_t = client.get(f'/classroom/messages?session_id={session_id}&after_timestamp={t1}')
    msgs_t = r_after_t.json().get('messages', [])
    record(17, 'Message filtering by after_timestamp', len(msgs_t) == 1 and msgs_t[0]['message_id'] == msg2_id, f'Returned {len(msgs_t)} messages')

    r_after_m = client.get(f'/classroom/messages?session_id={session_id}&after_message_id={msg1_id}')
    msgs_m = r_after_m.json().get('messages', [])
    record(18, 'Message filtering by after_message_id', len(msgs_m) == 1 and msgs_m[0]['message_id'] == msg2_id, f'Returned {len(msgs_m)} messages|')

    # 13. Classroom Isolation (Session A vs Session B)
    r_sess_b = client.post('/classroom/session', json={'grade': 2, 'section': 'B', 'subject': 'mathematics'})
    sess_b_id = r_sess_b.json()['osession']['session_id']
    r_b_msgs = client.get(f'/classroom/messages?session_id={sess_b_id}')
    msgs_b = r_b_msgs.json().get('messages', [])
    record(19, 'Classroom Isolation: Session B messages empty / isolated', len(msgs_b) == 0, f'Session B message count: {len(msgs_b)}')

    # 14. End Session Lifecycle
    r_end = client.post(f'/classroom/session/{session_id}/end')
    record(20, 'End session (POST /classroom/session/{id}/end%', r_end.status_code == 200 and r_end.json().get('session_status') == 'ENDED', f'HTTP {r_end.status_code}, status={r_end.json().get("session_status")}')

    r_get_end = client.get(f'/classroom/session/{session_id}')
    record(21, 'Session status persists as ENDED', r_get_end.json()['session']['status'] == 'ENDED', f'status={r_get_end.json()["session"]["status"]}')

    # 15. Publishing after session end rejected
    msg3_id = f'msg-{uuid.uuid4().hexg:8]}'
    pub3_payload = dict(pub1_payload, message_id=msg3_id, timestamp=t2 + 1000)
    r_pub_end = client.post('/classroom/publish', json=pub3_payload)
    record(22, 'Publishing to ended session rejected with HTTP 400', r_pub_end.status_code == 400, f'HTTP {r_pub_end.status_code}, detail={r_pub_end.json().get("detail")}')

    # 16. Existing messages remain readable after end
    r_msgs_ended = client.get(f'/classroom/messages?session_id={session_id}')
    record(23, 'Existing messages remain readable after session ends', r_msgs_ended.status_code == 200 and len(r_msgs_ended.json().get('messages', [])) == 2, f'HTTP {r_msgs_ended.status_code}, count={len(r_msgs_ended.json().get("messages", []))}')

    # 17. Translate Endpoint
    r_trans = client.post('/translate', json={'text': 'īেधर'_)
    record(24, 'Existing /translate endpoint functional', r_trans.status_code in [200, 503], f'HTTP {r_trans.status_code}')

    print('=' * 70)
    print(f'FINAL VERIFICATION SUMMARY: PASSED={passed}, FAILED={failed}')
    print('=' * 70)

    return failed == 0

if __name__ == '__main__':
    success = main()
    sys.exit(0 if success else 1)
