"""
로컬 PostgreSQL에서 problems / problem_specs / problem_sets 내용 덤프.
실행: pip install psycopg2-binary && python scripts/dump_problem_db.py

설정은 application-secret.yml과 동일하게 둠.
"""
import json
import sys
from datetime import date, datetime
from decimal import Decimal

try:
    import psycopg2
    from psycopg2.extras import RealDictCursor
except ImportError:
    print("pip install psycopg2-binary", file=sys.stderr)
    sys.exit(1)

CONN = dict(
    host="localhost",
    port=5435,
    dbname="ai_vibe_coding_test",
    user="postgres",
    password="postgres",
    options="-c search_path=ai_vibe_coding_test",
)


def _json_default(obj):
    if isinstance(obj, (datetime, date)):
        return obj.isoformat()
    if isinstance(obj, Decimal):
        return float(obj)
    raise TypeError(type(obj))


def pretty_json(val):
    if val is None:
        return None
    if isinstance(val, (dict, list)):
        return json.dumps(val, ensure_ascii=False, indent=2, default=_json_default)
    if isinstance(val, str):
        s = val.strip()
        if (s.startswith("{") and s.endswith("}")) or (s.startswith("[") and s.endswith("]")):
            try:
                return json.dumps(json.loads(s), ensure_ascii=False, indent=2)
            except json.JSONDecodeError:
                pass
        return val
    return val


def print_section(title):
    print("\n" + "=" * 72)
    print(title)
    print("=" * 72)


def main():
    conn = psycopg2.connect(**CONN)
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            print_section("problems (전체)")
            cur.execute(
                """
                SELECT id, title, difficulty, tags, status, current_spec_id,
                       created_at, updated_at, deleted_at
                FROM problems
                ORDER BY id
                """
            )
            rows = cur.fetchall()
            for r in rows:
                d = dict(r)
                if d.get("tags") is not None:
                    d["tags_pretty"] = pretty_json(d["tags"])
                print(json.dumps(d, ensure_ascii=False, indent=2, default=_json_default))

            print_section('problem3 후보 (title에 "위상" 포함)')
            cur.execute(
                """
                SELECT id, title, difficulty, status, current_spec_id
                FROM problems
                WHERE title LIKE '%위상%'
                ORDER BY id
                """
            )
            for r in cur.fetchall():
                print(json.dumps(dict(r), ensure_ascii=False, indent=2, default=_json_default))

            print_section("problem_specs (전체)")
            cur.execute(
                """
                SELECT spec_id, problem_id, version, content_md, checker_json, rubric_json,
                       changelog_md, published_at, created_at, updated_at, deleted_at
                FROM problem_specs
                ORDER BY problem_id, version
                """
            )
            for r in cur.fetchall():
                d = dict(r)
                for key in ("checker_json", "rubric_json"):
                    if d.get(key) is not None:
                        d[key + "_pretty"] = pretty_json(d[key])
                # 본문이 너무 길면 요약
                if d.get("content_md"):
                    cm = d["content_md"]
                    if len(cm) > 500:
                        d["content_md"] = cm[:500] + f"\n... ({len(cm)} chars total, truncated)"
                print(json.dumps(d, ensure_ascii=False, indent=2, default=_json_default))

            print_section("problem_sets (전체)")
            cur.execute(
                """
                SELECT id, name, created_by, created_at, updated_at, deleted_at
                FROM problem_sets
                ORDER BY id
                """
            )
            for r in cur.fetchall():
                print(json.dumps(dict(r), ensure_ascii=False, indent=2, default=_json_default))

            print_section("problem_set_items (참고: 세트–문제 연결)")
            cur.execute(
                """
                SELECT id, problem_set_id, problem_id, weight, created_at, updated_at, deleted_at
                FROM problem_set_items
                ORDER BY problem_set_id, problem_id
                """
            )
            for r in cur.fetchall():
                print(json.dumps(dict(r), ensure_ascii=False, indent=2, default=_json_default))

    finally:
        conn.close()


if __name__ == "__main__":
    main()
