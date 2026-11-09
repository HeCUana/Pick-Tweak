import json, os, re, sys

root = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
lang_path = os.path.join(root, "src", "main", "resources", "assets", "picktweak", "lang", "en_us.json")
with open(lang_path, encoding="utf-8") as f:
    lang = json.load(f)

pattern = re.compile(r'translatable\(\s*"([^"]+)"')
used = set()
for base, _dirs, files in os.walk(os.path.join(root, "src")):
    for name in files:
        if name.endswith(".java"):
            with open(os.path.join(base, name), encoding="utf-8", errors="replace") as f:
                used.update(pattern.findall(f.read()))

missing = sorted(k for k in used if k not in lang and not k.startswith("enchantment."))
unused = sorted(k for k in lang if k not in used and not k.startswith("pickblockpro.config.tool.kind.")
				and not k.startswith("pickblockpro.config.weapon.kind."))

print(f"lang keys: {len(lang)}   used in code: {len(used)}")
print("\n=== used but MISSING in lang ===")
for k in missing:
    print("   ", k)
print("\n=== in lang but unused (excluding dynamic kind keys) ===")
for k in unused:
    print("   ", k)

# 所有语言文件 key 必须一致
names = ["zh_cn", "zh_tw", "en_us", "fr_fr", "ru_ru"]
sets = {}
for n in names:
    with open(os.path.join(os.path.dirname(lang_path), f"{n}.json"), encoding="utf-8") as f:
        sets[n] = set(json.load(f))
ref = sets["en_us"]
print("\n=== language key consistency ===")
for n in names:
    diff = ref ^ sets[n]
    print(f"   {n}: {len(sets[n])} keys, {'OK' if not diff else 'MISMATCH ' + str(diff)}")

sys.exit(1 if missing else 0)
