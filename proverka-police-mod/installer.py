import json
import os
import sys
from pathlib import Path
import tkinter as tk
from tkinter import filedialog, messagebox, simpledialog

APP_NAME = "Proverka Police Mod"
TEST_KEY = "MENUBUC2"

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

    guess = find_game_root(Path(sys.executable if getattr(sys, "frozen", False) else __file__).resolve().parent)
    if guess is None:
        guess = filedialog.askdirectory(title="Выберите корневую папку GTA 5 Legacy")
        if not guess:
            return
        guess = Path(guess)

    game = Path(guess)
    if not ((game / "GTA5.exe").exists() or (game / "GTA5_Enhanced.exe").exists()):
        messagebox.showerror(APP_NAME, "В выбранной папке не найден GTA5.exe. Выберите именно корневую папку игры.")
        return

    lspdfr = game / "LSPDFR"
    plugin_root = game / "Plugins"
    if not (lspdfr.exists() or (game / "RagePluginHook.exe").exists()):
        messagebox.showerror(APP_NAME, "LSPDFR / RagePluginHook не обнаружен. Сначала установите LSPDFR.")
        return

    target = plugin_root / "LSPDFR" / "ProverkaPoliceMod"
    target.mkdir(parents=True, exist_ok=True)
    config = {
        "product": APP_NAME,
        "version": "0.1.0-test",
        "license_mode": "test-code",
        "features": {
            "partner_control": True,
            "ultimate_backup_integration": "planned",
            "vehicle_spawn_menu": True,
            "dispatcher_menu": True,
            "custom_text_to_speech": "planned"
        },
        "note": "Installer scaffold only. Runtime RAGEPluginHook plugin is not included in this test build."
    }
    (target / "config.json").write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")
    (target / "INSTALLATION.txt").write_text(
        "Proverka Police Mod 0.1.0-test\n"
        "Установщик создал папку и тестовую конфигурацию.\n"
        "Игровой DLL-плагин и перечисленные игровые функции пока не входят в сборку.\n"
        "Для следующего этапа нужен SDK/сборка RAGE Plugin Hook и проверка версий LSPDFR/Ultimate Backup.\n",
        encoding="utf-8"
    )
    messagebox.showinfo(APP_NAME, f"Тестовая структура создана:\n{target}\n\nЭто пока установщик-заготовка, не готовый игровой плагин.")

if __name__ == "__main__":
    install()
