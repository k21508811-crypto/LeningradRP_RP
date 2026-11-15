import json
import shutil
import sys
from pathlib import Path
import tkinter as tk
from tkinter import filedialog, messagebox, simpledialog

APP_NAME = "Proverka Police Mod"
TEST_KEY = "MENUBUC2"

def bundled_plugin():
    base = Path(getattr(sys, "_MEIPASS", Path(__file__).resolve().parent))
    candidate = base / "plugin" / "ProverkaPoliceMod.dll"
    return candidate if candidate.exists() else None

def find_game_root(start: Path):
    for base in [start, *start.parents]:
        if (base / "GTA5.exe").exists() or (base / "GTA5_Enhanced.exe").exists():
            return base
    return None

def install():
    root = tk.Tk()
    root.withdraw()
    key = simpledialog.askstring(APP_NAME, "Введите тестовый код доступа:", show="*")
    if key is None:
        return
    if key.strip().upper() != TEST_KEY:
        messagebox.showerror(APP_NAME, "Неверный код доступа.")
        return

    plugin_source = bundled_plugin()
    if plugin_source is None:
        messagebox.showerror(APP_NAME, "В установщике отсутствует ProverkaPoliceMod.dll. Сборка повреждена.")
        return

    guess = find_game_root(Path(sys.executable if getattr(sys, "frozen", False) else __file__).resolve().parent)
    if guess is None:
        guess = filedialog.askdirectory(title="Выберите корневую папку GTA 5 Legacy")
        if not guess:
            return
        guess = Path(guess)

    game = Path(guess)
    if not ((game / "GTA5.exe").exists() or (game / "GTA5_Enhanced.exe").exists()):
        messagebox.showerror(APP_NAME, "В выбранной папке не найден GTA5.exe. Выберите корневую папку игры.")
        return

    if not ((game / "RagePluginHook.exe").exists() or (game / "Plugins").exists()):
        messagebox.showerror(APP_NAME, "RAGE Plugin Hook не обнаружен. Сначала установите RAGE Plugin Hook и LSPDFR.")
        return

    plugins = game / "Plugins"
    plugins.mkdir(parents=True, exist_ok=True)
    target_dll = plugins / "ProverkaPoliceMod.dll"
    shutil.copy2(plugin_source, target_dll)

    config_dir = plugins / "LSPDFR" / "ProverkaPoliceMod"
    config_dir.mkdir(parents=True, exist_ok=True)
    config = {
        "product": APP_NAME,
        "version": "0.2.0",
        "menu_toggle_key": "F12",
        "menu_close_key": "ESC",
        "license_mode": "test-code",
        "implemented": ["RAGE Plugin Hook entry point", "F12 menu toggle", "ESC menu close"],
        "planned": ["partner management", "Ultimate Backup integration", "vehicle spawn menu", "dispatcher menu", "custom text-to-speech"],
        "note": "This build contains the initial in-game menu shell. Other gameplay sections are not implemented yet."
    }
    (config_dir / "config.json").write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")
    (config_dir / "INSTALLATION.txt").write_text(
        "Proverka Police Mod 0.2.0\n"
        "Plugin: Plugins/ProverkaPoliceMod.dll\n"
        "Open/close the in-game menu with F12. ESC also closes it.\n"
        "Partner, vehicle, dispatcher and speech modules are not implemented in this build.\n",
        encoding="utf-8"
    )
    messagebox.showinfo(APP_NAME, f"Плагин установлен:\n{target_dll}\n\nЗапустите GTA V через RAGE Plugin Hook. Нажмите F12 в игре.")
    
if __name__ == "__main__":
    install()
