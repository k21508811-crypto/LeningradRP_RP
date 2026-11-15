import json
import shutil
import sys
from datetime import datetime
from pathlib import Path
import tkinter as tk
from tkinter import filedialog, messagebox, simpledialog

APP_NAME = "Proverka Police Mod"
TEST_KEY = "MENUBUC2"
PLUGIN_NAME = "ProverkaPoliceMod.dll"


def bundled_plugin():
    base = Path(getattr(sys, "_MEIPASS", Path(__file__).resolve().parent))
    candidate = base / "plugin" / PLUGIN_NAME
    return candidate if candidate.is_file() else None


def find_game_root(start: Path):
    for base in [start, *start.parents]:
        if (base / "GTA5.exe").is_file():
            return base
    return None


def validate_game_root(game: Path):
    errors = []
    if not (game / "GTA5.exe").is_file():
        errors.append("Не найден GTA5.exe — выберите корневую папку GTA V Legacy.")
    if not (game / "RagePluginHook.exe").is_file():
        errors.append("Не найден RagePluginHook.exe — сначала установите RAGE Plugin Hook.")
    if not (game / "Plugins" / "LSPD First Response.dll").is_file():
        errors.append("Не найден Plugins\\LSPD First Response.dll — сначала установите LSPDFR.")
    return errors


def install():
    root = tk.Tk()
    root.withdraw()

    key = simpledialog.askstring(APP_NAME, "Введите тестовый код доступа:", show="*")
    if key is None:
        return
    if key.strip().upper() != TEST_KEY:
        messagebox.showerror(APP_NAME, "Неверный код доступа.")
        return

    source = bundled_plugin()
    if source is None:
        messagebox.showerror(APP_NAME, "В установщике отсутствует ProverkaPoliceMod.dll. Скачайте свежую сборку.")
        return

    guess = find_game_root(Path(sys.executable if getattr(sys, "frozen", False) else __file__).resolve().parent)
    if guess is None or validate_game_root(guess):
        selected = filedialog.askdirectory(title="Выберите корневую папку GTA V Legacy (где GTA5.exe)")
        if not selected:
            return
        guess = Path(selected)

    game = Path(guess)
    problems = validate_game_root(game)
    if problems:
        messagebox.showerror(APP_NAME, "Установка отменена:\n\n" + "\n".join(problems))
        return

    # This is an LSPDFR plugin, not a standalone RAGE Plugin Hook plugin.
    # LSPDFR loads plugins from Plugins/LSPDFR when the player goes on duty.
    target_dir = game / "Plugins" / "LSPDFR"
    target_dir.mkdir(parents=True, exist_ok=True)
    target_dll = target_dir / PLUGIN_NAME

    if target_dll.exists():
        backup = target_dll.with_suffix(target_dll.suffix + ".bak")
        shutil.copy2(target_dll, backup)

    shutil.copy2(source, target_dll)

    config = {
        "product": APP_NAME,
        "version": "0.5.0",
        "plugin_file": str(target_dll),
        "loader": "LSPDFR",
        "menu_toggle_key": "F12",
        "menu_close_key": "ESC",
        "test_access_code": True,
        "dispatcher": "LSPDFR Functions.RequestBackup API; Ultimate Backup compatibility depends on its handling of standard LSPDFR backup requests"
    }
    (target_dir / "ProverkaPoliceMod.json").write_text(
        json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (target_dir / "ProverkaPoliceMod-INSTALL.txt").write_text(
        "Proverka Police Mod 0.5.0\n"
        "Installed as an LSPDFR plugin.\n"
        "File: Plugins/LSPDFR/ProverkaPoliceMod.dll\n"
        "Start the game with RagePluginHook.exe, then go on duty in LSPDFR.\n"
        "LSPDFR loads plugins from Plugins/LSPDFR when going on duty.\n"
        "Press F12 to open/close the mod menu.\n"
        "If it does not load, inspect RagePluginHook.log in the GTA V root folder.\n",
        encoding="utf-8"
    )

    messagebox.showinfo(
        APP_NAME,
        "Установка завершена.\n\n"
        f"Плагин: {target_dll}\n\n"
        "Запустите GTA V через RagePluginHook.exe, дождитесь загрузки LSPDFR "
        "и выйдите на службу (Go On Duty). Затем нажмите F12.\n\n"
        "Важно: сам Setup.exe не нужно помещать в Plugins/LSPDFR."
    )


if __name__ == "__main__":
    install()
