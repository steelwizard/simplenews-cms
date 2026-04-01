(function () {
  var THEMES = ['default', 'dark', 'solarized', 'terminal'];
  var KEY = 'simplenews-theme';

  function applyTheme(name) {
    if (!THEMES.includes(name)) name = 'default';
    if (name === 'default') {
      document.documentElement.removeAttribute('data-theme');
    } else {
      document.documentElement.setAttribute('data-theme', name);
    }
    localStorage.setItem(KEY, name);
    document.querySelectorAll('.theme-btn').forEach(function (btn) {
      btn.classList.toggle('active', btn.dataset.theme === name);
    });
  }

  // Apply saved theme immediately (before paint)
  var saved = localStorage.getItem(KEY) || 'default';
  applyTheme(saved);

  // Expose for onclick handlers
  window.setTheme = applyTheme;
})();
