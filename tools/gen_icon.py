import zlib, struct, os, math

SIZE = 64
BG = (0x1C, 0x19, 0x17, 255)
EDGE = (0x0D, 0x11, 0x17, 255)
HANDLE = (0x8B, 0x5A, 0x2B, 255)
HANDLE_DARK = (0x6B, 0x44, 0x1E, 255)
HEAD = (0xB8, 0xBE, 0xC4, 255)
HEAD_HI = (0xFA, 0xFA, 0xFA, 255)

px = [[(0, 0, 0, 0) for _ in range(SIZE)] for _ in range(SIZE)]


def put(x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        px[y][x] = color


def disc(cx, cy, r, color):
    for y in range(int(cy - r) - 1, int(cy + r) + 2):
        for x in range(int(cx - r) - 1, int(cx + r) + 2):
            if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= r * r:
                put(x, y, color)


def line(x0, y0, x1, y1, width, color):
    steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 2) + 1
    for i in range(steps + 1):
        t = i / steps
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        disc(x, y, width / 2.0, color)


# 圆角底板
disc(32, 32, 30, BG)
for y in range(SIZE):
    for x in range(SIZE):
        if px[y][x][3] == 0:
            px[y][x] = BG
for y in range(SIZE):
    for x in range(SIZE):
        d = math.hypot(x + 0.5 - 32, y + 0.5 - 32)
        if 29.0 < d <= 30.5:
            px[y][x] = EDGE

# 镐柄
line(17, 50, 41, 21, 5.0, HANDLE_DARK)
line(17, 49, 41, 20, 3.2, HANDLE)

# 镐头（弧线）
cx, cy, r = 42, 16, 13
for deg in range(188, 353, 1):
    a = math.radians(deg)
    x = cx + r * math.cos(a)
    y = cy + r * math.sin(a) * 0.92
    disc(x, y, 2.4, HEAD)
    if deg < 240:
        disc(x - 0.8, y - 0.8, 1.1, HEAD_HI)

# 高光点
disc(cx - r * 0.98, cy + 1, 2.0, HEAD_HI)

raw = b""
for y in range(SIZE):
    raw += b"\x00" + b"".join(struct.pack("BBBB", *px[y][x]) for x in range(SIZE))


def chunk(tag, data):
    return (struct.pack(">I", len(data)) + tag + data
            + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))


png = b"\x89PNG\r\n\x1a\n"
png += chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
png += chunk(b"IDAT", zlib.compress(raw, 9))
png += chunk(b"IEND", b"")

out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources",
                   "assets", "picktweak", "icon.png")
out = os.path.normpath(out)
os.makedirs(os.path.dirname(out), exist_ok=True)
with open(out, "wb") as f:
    f.write(png)
print("icon written:", out, os.path.getsize(out), "bytes")
