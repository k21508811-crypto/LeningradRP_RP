using System;
using System.Windows.Forms;
using Rage;

namespace ProverkaPoliceMod
{
    public static class EntryPoint
    {
        private static bool menuOpen;
        private static bool f12WasDown;
        private static bool escapeWasDown;
        private static DateTime nextMenuRefresh = DateTime.MinValue;

        // RAGE Plugin Hook calls this static Main method when the plugin is loaded.
        public static void Main()
        {
            Game.DisplayNotification("Proverka Police Mod", "Загружен", "Нажмите F12, чтобы открыть меню", "CHAR警察", 0);

            while (true)
            {
                try
                {
                    bool f12Down = Game.IsKeyDown(Keys.F12);
                    if (f12Down && !f12WasDown)
                    {
                        menuOpen = !menuOpen;
                        nextMenuRefresh = DateTime.MinValue;
                        Game.DisplaySubtitle(menuOpen
                            ? "~r~PROVERKA POLICE MOD~s~\nМеню открыто | F12 — закрыть"
                            : "Меню Proverka Police Mod закрыто", 1800);
                    }
                    f12WasDown = f12Down;

                    bool escapeDown = Game.IsKeyDown(Keys.Escape);
                    if (menuOpen && escapeDown && !escapeWasDown)
                    {
                        menuOpen = false;
                        Game.DisplaySubtitle("Меню закрыто", 1200);
                    }
                    escapeWasDown = escapeDown;

                    if (menuOpen && DateTime.UtcNow >= nextMenuRefresh)
                    {
                        Game.DisplaySubtitle(
                            "~r~PROVERKA POLICE MOD~s~\n" +
                            "~w~F12 / ESC — закрыть меню\n" +
                            "Разделы: напарники | транспорт | диспетчер | голос\n" +
                            "~c~Функции разделов подключаются следующими обновлениями",
                            2200);
                        nextMenuRefresh = DateTime.UtcNow.AddMilliseconds(1900);
                    }
                }
                catch (Exception ex)
                {
                    Game.LogTrivial("Proverka Police Mod error: " + ex);
                }

                GameFiber.Sleep(50);
            }
        }
    }
}
