const tabs = ["home", "search", "site"];
function setTab(name) {
  tabs.forEach(id => { document.getElementById(id).hidden = id !== name; });
  document.querySelectorAll("[data-tab]").forEach(button => button.classList.toggle("active", button.dataset.tab === name));
}
function showHome() { setTab("home"); }
function showSearch() { setTab("search"); document.getElementById("search-query").focus(); }
function showSite() { setTab("site"); }
document.getElementById("year").textContent = new Date().getFullYear();
document.getElementById("search-form").addEventListener("submit", event => {
  event.preventDefault();
  const query = document.getElementById("search-query").value.trim();
  if (query) window.location.href = "https://yandex.ru/search/?text=" + encodeURIComponent(query);
});
document.getElementById("open-site").addEventListener("click", () => {
  window.location.href = "https://proverka29.ru/";
});