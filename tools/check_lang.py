import json, os, re, sys

root = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
lang_dir = os.path.join(root, "src", "main", "resources", "assets", "picktweak", "lang")
lang_path = os.path.join(lang_dir, "en_us.json")

with open(lang_path, encoding="utf-8") as f:
    lang = json.load(f)

used = set()
dynamic_prefixes = set()  # 以 "." 结尾的动态键前缀（例如 picktweak.config.axiomMode.）

# Component.translatable("key") / StringUtils.translate("key") / translatedName("key")
explicit = re.compile(r'(?:translatable|translate|translatedName)\(\s*"([^"]+)"')
# 任意形如 picktweak.config/message/command/gui.* 或 key.picktweak.* 的字面量：
# 覆盖 warn(player, "key") 这类先赋值给变量再翻译的键
our_literal = re.compile(
    r'"((?:picktweak\.(?:config|message|command|gui)\.[A-Za-z0-9_.]+)|(?:key\.picktweak\.[A-Za-z0-9_.]+))"'
)
# MaLiLib 配置项 / 热键的 id -> picktweak.config.<id>
option_id = re.compile(r'(?:bool|dbl|integer|str|strList|optionList|hotkey|modifierHotkey)\(\s*"([^"]+)"')
# translatedName(Reference.CONFIG_KEY_PREFIX + "id")
config_id = re.compile(r'Reference\.CONFIG_KEY_PREFIX \+ "([^"]+)"')


def add_key(key):
    if key.endswith("."):
        dynamic_prefixes.add(key)
    else:
        used.add(key)


def add_option(key):
    # 动态生成的热键 id 例如 "hotkeyChatSlot" + slot：若语言文件里存在同前缀的键，全部算被使用
    if key in lang:
        used.add(key)
        return
    family = [k for k in lang if k.startswith(key)]
    if family:
        used.update(family)
    else:
        used.add(key)


for base, _dirs, files in os.walk(os.path.join(root, "src")):
    for name in files:
        if not name.endswith(".java"):
            continue
        with open(os.path.join(base, name), encoding="utf-8", errors="replace") as f:
            text = f.read()
        for key in explicit.findall(text):
            add_key(key)
        for key in our_literal.findall(text):
            add_key(key)
        for option in option_id.findall(text):
            add_option("picktweak.config." + option)
        for option in config_id.findall(text):
            add_option("picktweak.config." + option)

# 动态前缀：语言文件里所有以该前缀开头的键都算被使用
for key in lang:
    for prefix in dynamic_prefixes:
        if key.startswith(prefix):
            used.add(key)

missing = sorted(k for k in used if k not in lang and not k.startswith("enchantment."))
unused = sorted(k for k in lang if k not in used)

print(f"lang keys: {len(lang)}   used in code: {len(used)}")
print("\n=== used but MISSING in lang ===")
for k in missing:
    print("   ", k)
print("\n=== in lang but unused ===")
for k in unused:
    print("   ", k)

# 所有语言文件 key 必须一致
names = ["zh_cn", "zh_tw", "en_us", "fr_fr", "ru_ru"]
sets = {}
for n in names:
    with open(os.path.join(lang_dir, f"{n}.json"), encoding="utf-8") as f:
        sets[n] = set(json.load(f))
ref = sets["en_us"]
print("\n=== language key consistency ===")
mismatched = False
for n in names:
    diff = ref ^ sets[n]
    if diff:
        mismatched = True
    print(f"   {n}: {len(sets[n])} keys, {'OK' if not diff else 'MISMATCH ' + str(sorted(diff))}")

sys.exit(1 if (missing or mismatched) else 0)
