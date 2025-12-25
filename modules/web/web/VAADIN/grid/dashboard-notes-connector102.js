// dashboard-notes-connector78.js
// initFunctionName в XML: com_company_untitled16_web_ui_components_jscomponent_GridDashboard
window.com_company_untitled16_web_ui_components_jscomponent_GridDashboard = function () {
  var connector = this;
  var element = connector.getElement();

  // ===== deps =====
  function $jq() { return window.jQuery || window.$; }
  function depsReady() {
    var $ = $jq();
    return !!($ && window.GridStack && $.fn && $.fn.simpleCalendar);
  }

  // ===== state =====
  var grid = null;
  var $grid = null;
  var LS_KEY = 'dash.layout.v3';

  // HTML picker
  var picker = { inited: false, backdrop: null };

  // ===== context menu (ПКМ) + lock move/resize =====
  var ctx = { el: null, isOpen: false, bound: false };
  var LS_LOCK_KEY = 'dash.locked.v1';
  var isLocked = false;
  var ctxHandlers = { onCtx: null, onDown: null, onKey: null };

  function closeContextMenu() {
    if (!ctx.el) return;
    ctx.el.style.display = 'none';
    ctx.isOpen = false;
  }

  function updateContextMenuLabels() {
    if (!ctx.el) return;
    var lockItem = ctx.el.querySelector('[data-act="lock"]');
    if (lockItem) lockItem.textContent = isLocked ? ' Разрешить перемещение' : 'Запретить перемещение';
  }

  function setLocked(lock) {
    isLocked = !!lock;
    try { localStorage.setItem(LS_LOCK_KEY, isLocked ? '1' : '0'); } catch (e) {}

    // GridStack: new
    if (grid && typeof grid.setStatic === 'function') {
      grid.setStatic(isLocked);
    } else {
      // GridStack: old fallback
      try {
        if (grid && typeof grid.enableMove === 'function') grid.enableMove(!isLocked);
        if (grid && typeof grid.enableResize === 'function') grid.enableResize(!isLocked);
      } catch (e) {}
    }

    if (element) {
      if (isLocked) element.classList.add('dash-locked');
      else element.classList.remove('dash-locked');
    }

    updateContextMenuLabels();
  }

  function restoreLockedState() {
    var v = '0';
    try { v = localStorage.getItem(LS_LOCK_KEY) || '0'; } catch (e) {}
    setLocked(v === '1');
  }

  function ensureContextMenu() {
    if (ctx.el) return;

    var div = document.createElement('div');
    div.className = 'dash-ctx tezis-skin';
    div.style.display = 'none';
    div.innerHTML =
      '<div class="dash-ctx-item" data-act="add">Добавить виджет</div>' +
      '<div class="dash-ctx-item" data-act="reset">Сбросить раскладку</div>' +
      '<div class="dash-ctx-sep"></div>' +
      '<div class="dash-ctx-item" data-act="lock"></div>';

    document.body.appendChild(div);
    ctx.el = div;

    div.addEventListener('click', function (ev) {
      var it = ev.target.closest ? ev.target.closest('.dash-ctx-item') : null;
      if (!it) return;

      var act = it.getAttribute('data-act');
      closeContextMenu();

      if (act === 'add') {
        openPicker();
      } else if (act === 'reset') {
        resetLayoutToDefaults();
      } else if (act === 'lock') {
        setLocked(!isLocked);
      }
    });

    updateContextMenuLabels();
  }

  function openContextMenu(x, y) {
    ensureContextMenu();
    updateContextMenuLabels();

    ctx.el.style.display = 'block';

    // чтобы не вылезало за экран
    var rect = ctx.el.getBoundingClientRect();
    var vw = window.innerWidth, vh = window.innerHeight;

    var nx = Math.min(x, vw - rect.width - 8);
    var ny = Math.min(y, vh - rect.height - 8);

    ctx.el.style.left = Math.max(8, nx) + 'px';
    ctx.el.style.top  = Math.max(8, ny) + 'px';

    ctx.isOpen = true;
  }

  function bindContextMenuOnce() {
    if (ctx.bound) return;
    ctx.bound = true;

    ctxHandlers.onCtx = function (e) {
      // стандартное меню оставляем для полей ввода/редактирования
      if (e.target && e.target.closest && e.target.closest('input,textarea,[contenteditable="true"]')) return;

      e.preventDefault();
      e.stopPropagation();
      openContextMenu(e.clientX, e.clientY);
    };

    ctxHandlers.onDown = function (e) {
      if (!ctx.isOpen) return;
      if (ctx.el && !ctx.el.contains(e.target)) closeContextMenu();
    };

    ctxHandlers.onKey = function (e) {
      if (e.key === 'Escape') closeContextMenu();
    };

    element.addEventListener('contextmenu', ctxHandlers.onCtx);
    document.addEventListener('mousedown', ctxHandlers.onDown);
    document.addEventListener('keydown', ctxHandlers.onKey);
  }

  function unbindContextMenu() {
    if (!ctx.bound) return;
    ctx.bound = false;

    try { element.removeEventListener('contextmenu', ctxHandlers.onCtx); } catch (e) {}
    try { document.removeEventListener('mousedown', ctxHandlers.onDown); } catch (e) {}
    try { document.removeEventListener('keydown', ctxHandlers.onKey); } catch (e) {}

    ctxHandlers.onCtx = null;
    ctxHandlers.onDown = null;
    ctxHandlers.onKey = null;

    if (ctx.el) {
      try { ctx.el.remove(); } catch (e) {}
      ctx.el = null;
    }

    ctx.isOpen = false;
  }

  // ===== utils =====
  function pad(n) { return n < 10 ? '0' + n : '' + n; }
  function toIso(d) {
    var y = d.getFullYear();
    var m = ('0' + (d.getMonth() + 1)).slice(-2);
    var day = ('0' + d.getDate()).slice(-2);
    return y + '-' + m + '-' + day;
  }
  function isoToDate(iso) {
    var p = String(iso || '').split('-');
    if (p.length !== 3) return new Date();
    return new Date(parseInt(p[0], 10), parseInt(p[1], 10) - 1, parseInt(p[2], 10));
  }
  function widgetExists(id) { return !!element.querySelector('#' + id); }

  function defaultRectById(id) {
    switch (id) {
      case 'widget-manager':  return { x: 0, y: 0, w: 3, h: 2 };
      case 'widget-clock':    return { x: 3, y: 0, w: 3, h: 2 };
      case 'widget-calendar': return { x: 6, y: 0, w: 6, h: 6 };
      case 'widget-notes':    return { x: 0, y: 2, w: 6, h: 6 };
      case 'widget-news':     return { x: 0, y: 8, w: 6, h: 4 };
      case 'widget-recent':   return { x: 6, y: 8, w: 6, h: 4 };
      default: return { x: 0, y: 0, w: 3, h: 3 };
    }
  }

  // ===== root HTML =====
function setRootHtmlOnce() {
  element.style.display = 'block';
  element.style.width = '100%';
  element.style.height = '100%';
  element.classList.add('dash-root', 'tezis-skin'); // <-- добавь tezis-skin

  if (!element.querySelector('.dash-grid')) {
    element.innerHTML = '<div class="grid-stack dash-grid" style="width:100%;height:100%"></div>';
  }
}


  // ===== tile building =====
  function makeTile(id, title, bodyHtml, canRemove) {
    var removeBtn = canRemove
      ? '<button type="button" class="dash-remove" title="Убрать">✕</button>'
      : '';

    return '' +
      '<div class="grid-stack-item" id="' + id + '">' +
      '  <div class="grid-stack-item-content widget-flex">' +
      '    <div class="widget-drag-handle dash-handle">' +
      '      <span class="dash-title">' + title + '</span>' +
      removeBtn +
      '    </div>' +
      '    <div class="dash-body">' + bodyHtml + '</div>' +
      '  </div>' +
      '</div>';
  }

  function addWidget(id, rect, fromRestore) {
    if (widgetExists(id)) return;

    rect = rect || defaultRectById(id);

    var title = id;
    var bodyHtml = '';
    var canRemove = (id !== 'widget-manager');

    if (id === 'widget-manager') {
      title = 'Панель';
      bodyHtml =
        '<div class="dash-panel">' +
        '  <button type="button" class="dash-btn dash-add">➕ Добавить виджет</button>' +
        '  <button type="button" class="dash-btn dash-reset">Сбросить раскладку</button>' +
        '  <button type="button" class="dash-btn dash-ge-test">📣 Тест GlobalEvent</button>' +
        '</div>';

    } else if (id === 'widget-clock') {
      title = 'Часы';
      bodyHtml =
        '<div class="dash-clock-wrap">' +
        '  <div class="dash-clock-time"></div>' +
        '  <div class="dash-clock-date"></div>' +
        '</div>';

    } else if (id === 'widget-calendar') {
      title = 'Календарь';
      bodyHtml = '<div class="calendar-widget-container"></div>';

    } else if (id === 'widget-notes') {
      title = 'Заметки';
      bodyHtml =
        '<div class="dash-notes-head">' +
        '  <div class="dash-notes-date"></div>' +
        '  <button type="button" class="dash-btn dash-create-note">+ Создать</button>' +
        '</div>' +
        '<div class="dash-notes-list"></div>';

    } else if (id === 'widget-news') {
      title = 'Новости';
      bodyHtml =
        '<div class="dash-news-head">' +
        '  <button type="button" class="dash-btn dash-news-refresh">Обновить</button>' +
        '</div>' +
        '<div class="dash-news-list"></div>';

    } else if (id === 'widget-recent') {
      title = 'Последние документы';
      bodyHtml =
        '<div class="dash-recent-head">' +
        '  <button type="button" class="dash-btn dash-recent-refresh">Обновить</button>' +
        '</div>' +
        '<div class="dash-recent-list"></div>';

    } else {
      bodyHtml = '<div>Unknown widget</div>';
    }

    $grid[0].insertAdjacentHTML('beforeend', makeTile(id, title, bodyHtml, canRemove));
    var el = element.querySelector('#' + id);

    el.setAttribute('gs-x', '' + rect.x);
    el.setAttribute('gs-y', '' + rect.y);
    el.setAttribute('gs-w', '' + rect.w);
    el.setAttribute('gs-h', '' + rect.h);

    grid.makeWidget(el);
    grid.update(el, rect.x, rect.y, rect.w, rect.h);

    bindTileLogic(id);

    if (!fromRestore) saveLayout();
  }

  function removeWidget(id) {
    var el = element.querySelector('#' + id);
    if (!el || !grid) return;

    if (id === 'widget-clock') stopClock();
    if (id === 'widget-calendar') resetCalendarState();

    grid.removeWidget(el);
    saveLayout();
  }

  // ===== layout persistence =====
  function saveLayout() {
    if (!grid || !grid.engine || !grid.engine.nodes) return;

    var arr = grid.engine.nodes
      .filter(function (n) { return n && n.el && n.el.id && n.el.id.indexOf('widget-') === 0; })
      .map(function (n) { return { id: n.el.id, x: n.x, y: n.y, w: n.w, h: n.h }; });

    try { localStorage.setItem(LS_KEY, JSON.stringify(arr)); } catch (e) {}
  }

  function restoreLayout() {
    var raw = null;
    try { raw = localStorage.getItem(LS_KEY); } catch (e) { raw = null; }
    if (!raw) return false;

    var arr;
    try { arr = JSON.parse(raw); } catch (e) { arr = null; }
    if (!arr || !arr.length) return false;

    var mgr = arr.filter(function (x) { return x && x.id === 'widget-manager'; })[0];
    addWidget('widget-manager',
      mgr ? { x: mgr.x || 0, y: mgr.y || 0, w: mgr.w || 3, h: mgr.h || 2 } : defaultRectById('widget-manager'),
      true
    );

    arr.forEach(function (it) {
      if (!it || !it.id || it.id === 'widget-manager') return;
      addWidget(it.id, { x: it.x || 0, y: it.y || 0, w: it.w || 3, h: it.h || 3 }, true);
    });

    return true;
  }

  function resetLayoutToDefaults() {
    try { localStorage.removeItem(LS_KEY); } catch (e) {}

    ['widget-clock', 'widget-calendar', 'widget-notes', 'widget-news', 'widget-recent'].forEach(function (x) {
      if (widgetExists(x)) removeWidget(x);
    });

    addWidget('widget-clock', defaultRectById('widget-clock'), false);
    addWidget('widget-calendar', defaultRectById('widget-calendar'), false);
    addWidget('widget-notes', defaultRectById('widget-notes'), false);
    addWidget('widget-news', defaultRectById('widget-news'), false);
    addWidget('widget-recent', defaultRectById('widget-recent'), false);

    saveLayout();
  }

  // ===== picker styles + widgets styles =====
  function injectPickerStylesOnce() {
    if (document.getElementById('dash-picker-styles')) return;

    var css = [
      '.dash-picker-backdrop{display:none;align-items:center;justify-content:center;',
      '  position:fixed;left:0;top:0;right:0;bottom:0;',
      '  background:rgba(0,0,0,.40);z-index:99999;',
      '}',
      '.dash-picker{width:min(760px,92vw);background:#fff;border-radius:14px;overflow:hidden;',
      '  box-shadow:0 12px 40px rgba(0,0,0,.25);',
      '}',
      '.dash-picker-head{display:flex;align-items:center;justify-content:space-between;gap:10px;',
      '  padding:12px 14px;border-bottom:1px solid rgba(0,0,0,.08);background:#fafafa;',
      '}',
      '.dash-picker-title{font-weight:800;font-size:16px;}',
      '.dash-picker-close{border:none;background:transparent;cursor:pointer;width:34px;height:34px;',
      '  border-radius:10px;font-size:16px;',
      '}',
      '.dash-picker-close:hover{background:rgba(0,0,0,.06);}',
      '.dash-picker-grid{display:grid;grid-template-columns:repeat(1,minmax(0,1fr));gap:10px;padding:12px;}',
      '@media (min-width:760px){.dash-picker-grid{grid-template-columns:repeat(4,minmax(0,1fr));}}',
      '.dash-widget-card{display:flex;flex-direction:column;gap:10px;',
      '  border:1px solid rgba(0,0,0,.10);border-radius:12px;padding:12px;cursor:pointer;background:#fff;',
      '}',
      '.dash-widget-card:hover{background:#fafafa;}',
      '.dash-widget-icon{font-size:28px;}',
      '.dash-widget-title{font-weight:800;}',
      '.dash-widget-desc{color:#6f6f6f;font-size:13px;margin-top:2px;}',
      '.dash-widget-action{margin-top:auto;font-weight:800;color:#333;',
      '  padding:8px 10px;border-radius:10px;border:1px solid rgba(0,0,0,.12);text-align:center;',
      '}',
      '.dash-widget-card:hover .dash-widget-action{background:#f6f6f6;}',
      '.dash-widget-card.is-disabled{opacity:.45;cursor:not-allowed;}',
      '.dash-widget-card.is-disabled:hover{background:#fff;}',
      '.dash-widget-card.is-disabled .dash-widget-action{background:#fff;}',
      'body.dash-picker-open{overflow:hidden;}',

      // layout flex
      '.widget-flex{display:flex;flex-direction:column;}',
      '.dash-body{flex:1;min-height:0;}', // важно для правильного ресайза часов/календаря
      '.dash-handle{flex:0 0 auto;}',

      // NEWS
      '.dash-news-row{padding:10px 10px;border-bottom:1px solid rgba(0,0,0,.06);cursor:pointer;}',
      '.dash-news-row:hover{background:rgba(0,0,0,.03);}',
      '.dash-news-title{font-weight:800;}',
      '.dash-news-short{font-size:13px;opacity:.85;margin-top:4px;line-height:1.25;}',

      // CLOCK (большие, занимают почти весь виджет)
      '.dash-clock-wrap{height:100%;width:100%;padding:0;box-sizing:border-box;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:2px;overflow:hidden;}',
      '.dash-clock-time{font-weight:900;line-height:1;letter-spacing:.02em;white-space:nowrap;font-size:var(--dash-clock-size, 80px);font-variant-numeric:tabular-nums;}',
      '.dash-clock-sec{font-size:.38em;opacity:.65;font-weight:800;margin-left:8px;vertical-align:baseline;}',
      '.dash-clock-date{font-size:clamp(11px,1.1vw,16px);opacity:.75;white-space:nowrap;}',

      // RECENT
      '.dash-recent-row{padding:10px 10px;border-bottom:1px solid rgba(0,0,0,.06);cursor:pointer;display:flex;gap:10px;align-items:flex-start;}',
      '.dash-recent-row:hover{background:rgba(0,0,0,.03);}',
      '.dash-recent-ico{width:22px;min-width:22px;opacity:.85;font-size:16px;line-height:1.2;margin-top:2px;}',
      '.dash-recent-main{flex:1;min-width:0;}',
      '.dash-recent-cap{font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}',
      '.dash-recent-meta{font-size:12px;opacity:.75;margin-top:3px;}',

      // BUTTONS (общие для всех .dash-btn)
      '.dash-btn{appearance:none;-webkit-appearance:none;cursor:pointer;',
      '  border:1px solid rgba(0,0,0,.14);background:#fff;color:#222;',
      '  border-radius:10px;padding:6px 10px;font-weight:700;',
      '  transition:background .15s ease, box-shadow .15s ease, transform .05s ease;',
      '}',
      '.dash-btn:hover{background:rgba(0,0,0,.06);box-shadow:0 2px 10px rgba(0,0,0,.10);}',
      '.dash-btn:active{transform:translateY(1px);box-shadow:0 1px 6px rgba(0,0,0,.10);}',
      '.dash-btn:focus-visible{outline:2px solid rgba(0,0,0,.25);outline-offset:2px;}',

      // крестик "убрать виджет"
      '.dash-remove{border:none;background:transparent;cursor:pointer;',
      '  width:30px;height:30px;border-radius:10px;',
      '  transition:background .15s ease, transform .05s ease;',
      '}',
      '.dash-remove:hover{background:rgba(0,0,0,.08);}',
      '.dash-remove:active{transform:translateY(1px);}',

      // CONTEXT MENU (ПКМ)
      '.dash-ctx{position:fixed;z-index:100000;display:none;',
      '  background:#fff;border:1px solid rgba(0,0,0,.12);border-radius:12px;',
      '  box-shadow:0 12px 40px rgba(0,0,0,.18);padding:6px;min-width:230px;',
      '}',
      '.dash-ctx-item{padding:10px 10px;border-radius:10px;cursor:pointer;user-select:none;font-weight:800;}',
      '.dash-ctx-item:hover{background:rgba(0,0,0,.06);}',
      '.dash-ctx-sep{height:1px;background:rgba(0,0,0,.08);margin:6px 4px;}',

      // LOCK MODE
      '.dash-locked .widget-drag-handle{cursor:default;}'
    ].join('\n');

    var st = document.createElement('style');
    st.id = 'dash-picker-styles';
    st.type = 'text/css';
    st.appendChild(document.createTextNode(css));
    document.head.appendChild(st);
  }

  // ===== picker HTML menu =====
  function ensurePicker() {
    if (picker.inited) return;
    picker.inited = true;

    injectPickerStylesOnce();

    var html =
      '<div class="dash-picker-backdrop" style="display:none">' +
      '  <div class="dash-picker" role="dialog" aria-modal="true">' +
      '    <div class="dash-picker-head">' +
      '      <div class="dash-picker-title">Добавить виджет</div>' +
      '      <button type="button" class="dash-picker-close" title="Закрыть">✕</button>' +
      '    </div>' +
      '    <div class="dash-picker-grid">' +
      '      <div class="dash-widget-card" data-widget="widget-clock">' +
      '        <div class="dash-widget-icon">🕒</div>' +
      '        <div><div class="dash-widget-title">Часы</div><div class="dash-widget-desc">Текущее время</div></div>' +
      '        <div class="dash-widget-action">Добавить</div>' +
      '      </div>' +
      '      <div class="dash-widget-card" data-widget="widget-calendar">' +
      '        <div class="dash-widget-icon">📅</div>' +
      '        <div><div class="dash-widget-title">Календарь</div><div class="dash-widget-desc">Заметки по датам</div></div>' +
      '        <div class="dash-widget-action">Добавить</div>' +
      '      </div>' +
      '      <div class="dash-widget-card" data-widget="widget-notes">' +
      '        <div class="dash-widget-icon">📝</div>' +
      '        <div><div class="dash-widget-title">Заметки</div><div class="dash-widget-desc">Список за день</div></div>' +
      '        <div class="dash-widget-action">Добавить</div>' +
      '      </div>' +
      '      <div class="dash-widget-card" data-widget="widget-news">' +
      '        <div class="dash-widget-icon">📰</div>' +
      '        <div><div class="dash-widget-title">Новости</div><div class="dash-widget-desc">Из сущности News</div></div>' +
      '        <div class="dash-widget-action">Добавить</div>' +
      '      </div>' +
      '      <div class="dash-widget-card" data-widget="widget-recent">' +
      '        <div class="dash-widget-icon">🕘</div>' +
      '        <div><div class="dash-widget-title">Последние</div><div class="dash-widget-desc">10 последних документов</div></div>' +
      '        <div class="dash-widget-action">Добавить</div>' +
      '      </div>' +
      '    </div>' +
      '  </div>' +
      '</div>';

    document.body.insertAdjacentHTML('beforeend', html);
    picker.backdrop = document.body.querySelector('.dash-picker-backdrop');
    if (picker.backdrop) picker.backdrop.classList.add('tezis-skin');

    // dashboard-notes-connector78.js
    // initFunctionName в XML: com_company_untitled16_web_ui_components_jscomponent_GridDashboard
    window.com_company_untitled16_web_ui_components_jscomponent_GridDashboard = function () {
      var connector = this;
      var element = connector.getElement();

      // ===== deps =====
      function $jq() { return window.jQuery || window.$; }
      function depsReady() {
        var $ = $jq();
        return !!($ && window.GridStack && $.fn && $.fn.simpleCalendar);
      }

      // ===== state =====
      var grid = null;
      var $grid = null;
      var LS_KEY = 'dash.layout.v3';

      // HTML picker
      var picker = { inited: false, backdrop: null };

      // ===== context menu (ПКМ) + lock move/resize =====
      var ctx = { el: null, isOpen: false, bound: false };
      var LS_LOCK_KEY = 'dash.locked.v1';
      var isLocked = false;
      var ctxHandlers = { onCtx: null, onDown: null, onKey: null };

      function closeContextMenu() {
        if (!ctx.el) return;
        ctx.el.style.display = 'none';
        ctx.isOpen = false;
      }

      function updateContextMenuLabels() {
        if (!ctx.el) return;
        var lockItem = ctx.el.querySelector('[data-act="lock"]');
        if (lockItem) lockItem.textContent = isLocked ? ' Разрешить перемещение' : 'Запретить перемещение';
      }

      function setLocked(lock) {
        isLocked = !!lock;
        try { localStorage.setItem(LS_LOCK_KEY, isLocked ? '1' : '0'); } catch (e) {}

        // GridStack: new
        if (grid && typeof grid.setStatic === 'function') {
          grid.setStatic(isLocked);
        } else {
          // GridStack: old fallback
          try {
            if (grid && typeof grid.enableMove === 'function') grid.enableMove(!isLocked);
            if (grid && typeof grid.enableResize === 'function') grid.enableResize(!isLocked);
          } catch (e) {}
        }

        if (element) {
          if (isLocked) element.classList.add('dash-locked');
          else element.classList.remove('dash-locked');
        }

        updateContextMenuLabels();
      }

      function restoreLockedState() {
        var v = '0';
        try { v = localStorage.getItem(LS_LOCK_KEY) || '0'; } catch (e) {}
        setLocked(v === '1');
      }

      function ensureContextMenu() {
        if (ctx.el) return;

        var div = document.createElement('div');
        div.className = 'dash-ctx';
        div.style.display = 'none';
        div.innerHTML =
          '<div class="dash-ctx-item" data-act="add">Добавить виджет</div>' +
          '<div class="dash-ctx-item" data-act="reset">Сбросить раскладку</div>' +
          '<div class="dash-ctx-sep"></div>' +
          '<div class="dash-ctx-item" data-act="lock"></div>';

        document.body.appendChild(div);
        ctx.el = div;

        div.addEventListener('click', function (ev) {
          var it = ev.target.closest ? ev.target.closest('.dash-ctx-item') : null;
          if (!it) return;

          var act = it.getAttribute('data-act');
          closeContextMenu();

          if (act === 'add') {
            openPicker();
          } else if (act === 'reset') {
            resetLayoutToDefaults();
          } else if (act === 'lock') {
            setLocked(!isLocked);
          }
        });

        updateContextMenuLabels();
      }

      function openContextMenu(x, y) {
        ensureContextMenu();
        updateContextMenuLabels();

        ctx.el.style.display = 'block';

        // чтобы не вылезало за экран
        var rect = ctx.el.getBoundingClientRect();
        var vw = window.innerWidth, vh = window.innerHeight;

        var nx = Math.min(x, vw - rect.width - 8);
        var ny = Math.min(y, vh - rect.height - 8);

        ctx.el.style.left = Math.max(8, nx) + 'px';
        ctx.el.style.top  = Math.max(8, ny) + 'px';

        ctx.isOpen = true;
      }

      function bindContextMenuOnce() {
        if (ctx.bound) return;
        ctx.bound = true;

        ctxHandlers.onCtx = function (e) {
          // стандартное меню оставляем для полей ввода/редактирования
          if (e.target && e.target.closest && e.target.closest('input,textarea,[contenteditable="true"]')) return;

          e.preventDefault();
          e.stopPropagation();
          openContextMenu(e.clientX, e.clientY);
        };

        ctxHandlers.onDown = function (e) {
          if (!ctx.isOpen) return;
          if (ctx.el && !ctx.el.contains(e.target)) closeContextMenu();
        };

        ctxHandlers.onKey = function (e) {
          if (e.key === 'Escape') closeContextMenu();
        };

        element.addEventListener('contextmenu', ctxHandlers.onCtx);
        document.addEventListener('mousedown', ctxHandlers.onDown);
        document.addEventListener('keydown', ctxHandlers.onKey);
      }

      function unbindContextMenu() {
        if (!ctx.bound) return;
        ctx.bound = false;

        try { element.removeEventListener('contextmenu', ctxHandlers.onCtx); } catch (e) {}
        try { document.removeEventListener('mousedown', ctxHandlers.onDown); } catch (e) {}
        try { document.removeEventListener('keydown', ctxHandlers.onKey); } catch (e) {}

        ctxHandlers.onCtx = null;
        ctxHandlers.onDown = null;
        ctxHandlers.onKey = null;

        if (ctx.el) {
          try { ctx.el.remove(); } catch (e) {}
          ctx.el = null;
        }

        ctx.isOpen = false;
      }

      // ===== utils =====
      function pad(n) { return n < 10 ? '0' + n : '' + n; }
      function toIso(d) {
        var y = d.getFullYear();
        var m = ('0' + (d.getMonth() + 1)).slice(-2);
        var day = ('0' + d.getDate()).slice(-2);
        return y + '-' + m + '-' + day;
      }
      function isoToDate(iso) {
        var p = String(iso || '').split('-');
        if (p.length !== 3) return new Date();
        return new Date(parseInt(p[0], 10), parseInt(p[1], 10) - 1, parseInt(p[2], 10));
      }
      function widgetExists(id) { return !!element.querySelector('#' + id); }

      function defaultRectById(id) {
        switch (id) {
          case 'widget-manager':  return { x: 0, y: 0, w: 3, h: 2 };
          case 'widget-clock':    return { x: 3, y: 0, w: 3, h: 2 };
          case 'widget-calendar': return { x: 6, y: 0, w: 6, h: 6 };
          case 'widget-notes':    return { x: 0, y: 2, w: 6, h: 6 };
          case 'widget-news':     return { x: 0, y: 8, w: 6, h: 4 };
          case 'widget-recent':   return { x: 6, y: 8, w: 6, h: 4 };
          default: return { x: 0, y: 0, w: 3, h: 3 };
        }
      }

      // ===== root HTML =====
    function setRootHtmlOnce() {
      element.style.display = 'block';
      element.style.width = '100%';
      element.style.height = '100%';
      element.classList.add('dash-root', 'tezis-skin'); // <-- добавь tezis-skin

      if (!element.querySelector('.dash-grid')) {
        element.innerHTML = '<div class="grid-stack dash-grid" style="width:100%;height:100%"></div>';
      }
    }


      // ===== tile building =====
      function makeTile(id, title, bodyHtml, canRemove) {
        var removeBtn = canRemove
          ? '<button type="button" class="dash-remove" title="Убрать">✕</button>'
          : '';

        return '' +
          '<div class="grid-stack-item" id="' + id + '">' +
          '  <div class="grid-stack-item-content widget-flex">' +
          '    <div class="widget-drag-handle dash-handle">' +
          '      <span class="dash-title">' + title + '</span>' +
          removeBtn +
          '    </div>' +
          '    <div class="dash-body">' + bodyHtml + '</div>' +
          '  </div>' +
          '</div>';
      }

      function addWidget(id, rect, fromRestore) {
        if (widgetExists(id)) return;

        rect = rect || defaultRectById(id);

        var title = id;
        var bodyHtml = '';
        var canRemove = (id !== 'widget-manager');

        if (id === 'widget-manager') {
          title = 'Панель';
          bodyHtml =
            '<div class="dash-panel">' +
            '  <button type="button" class="dash-btn dash-add">➕ Добавить виджет</button>' +
            '  <button type="button" class="dash-btn dash-reset">Сбросить раскладку</button>' +
            '  <button type="button" class="dash-btn dash-ge-test">📣 Тест GlobalEvent</button>' +
            '</div>';

        } else if (id === 'widget-clock') {
          title = 'Часы';
          bodyHtml =
            '<div class="dash-clock-wrap">' +
            '  <div class="dash-clock-time"></div>' +
            '  <div class="dash-clock-date"></div>' +
            '</div>';

        } else if (id === 'widget-calendar') {
          title = 'Календарь';
          bodyHtml = '<div class="calendar-widget-container"></div>';

        } else if (id === 'widget-notes') {
          title = 'Заметки';
          bodyHtml =
            '<div class="dash-notes-head">' +
            '  <div class="dash-notes-date"></div>' +
            '  <button type="button" class="dash-btn dash-create-note">+ Создать</button>' +
            '</div>' +
            '<div class="dash-notes-list"></div>';

        } else if (id === 'widget-news') {
          title = 'Новости';
          bodyHtml =
            '<div class="dash-news-head">' +
            '  <button type="button" class="dash-btn dash-news-refresh">Обновить</button>' +
            '</div>' +
            '<div class="dash-news-list"></div>';

        } else if (id === 'widget-recent') {
          title = 'Последние документы';
          bodyHtml =
            '<div class="dash-recent-head">' +
            '  <button type="button" class="dash-btn dash-recent-refresh">Обновить</button>' +
            '</div>' +
            '<div class="dash-recent-list"></div>';

        } else {
          bodyHtml = '<div>Unknown widget</div>';
        }

        $grid[0].insertAdjacentHTML('beforeend', makeTile(id, title, bodyHtml, canRemove));
        var el = element.querySelector('#' + id);

        el.setAttribute('gs-x', '' + rect.x);
        el.setAttribute('gs-y', '' + rect.y);
        el.setAttribute('gs-w', '' + rect.w);
        el.setAttribute('gs-h', '' + rect.h);

        grid.makeWidget(el);
        grid.update(el, rect.x, rect.y, rect.w, rect.h);

        bindTileLogic(id);

        if (!fromRestore) saveLayout();
      }

      function removeWidget(id) {
        var el = element.querySelector('#' + id);
        if (!el || !grid) return;

        if (id === 'widget-clock') stopClock();
        if (id === 'widget-calendar') resetCalendarState();

        grid.removeWidget(el);
        saveLayout();
      }

      // ===== layout persistence =====
      function saveLayout() {
        if (!grid || !grid.engine || !grid.engine.nodes) return;

        var arr = grid.engine.nodes
          .filter(function (n) { return n && n.el && n.el.id && n.el.id.indexOf('widget-') === 0; })
          .map(function (n) { return { id: n.el.id, x: n.x, y: n.y, w: n.w, h: n.h }; });

        try { localStorage.setItem(LS_KEY, JSON.stringify(arr)); } catch (e) {}
      }

      function restoreLayout() {
        var raw = null;
        try { raw = localStorage.getItem(LS_KEY); } catch (e) { raw = null; }
        if (!raw) return false;

        var arr;
        try { arr = JSON.parse(raw); } catch (e) { arr = null; }
        if (!arr || !arr.length) return false;

        var mgr = arr.filter(function (x) { return x && x.id === 'widget-manager'; })[0];
        addWidget('widget-manager',
          mgr ? { x: mgr.x || 0, y: mgr.y || 0, w: mgr.w || 3, h: mgr.h || 2 } : defaultRectById('widget-manager'),
          true
        );

        arr.forEach(function (it) {
          if (!it || !it.id || it.id === 'widget-manager') return;
          addWidget(it.id, { x: it.x || 0, y: it.y || 0, w: it.w || 3, h: it.h || 3 }, true);
        });

        return true;
      }

      function resetLayoutToDefaults() {
        try { localStorage.removeItem(LS_KEY); } catch (e) {}

        ['widget-clock', 'widget-calendar', 'widget-notes', 'widget-news', 'widget-recent'].forEach(function (x) {
          if (widgetExists(x)) removeWidget(x);
        });

        addWidget('widget-clock', defaultRectById('widget-clock'), false);
        addWidget('widget-calendar', defaultRectById('widget-calendar'), false);
        addWidget('widget-notes', defaultRectById('widget-notes'), false);
        addWidget('widget-news', defaultRectById('widget-news'), false);
        addWidget('widget-recent', defaultRectById('widget-recent'), false);

        saveLayout();
      }

      // ===== picker styles + widgets styles =====
      function injectPickerStylesOnce() {
        if (document.getElementById('dash-picker-styles')) return;

        var css = [
          '.dash-picker-backdrop{display:none;align-items:center;justify-content:center;',
          '  position:fixed;left:0;top:0;right:0;bottom:0;',
          '  background:rgba(0,0,0,.40);z-index:99999;',
          '}',
          '.dash-picker{width:min(760px,92vw);background:#fff;border-radius:14px;overflow:hidden;',
          '  box-shadow:0 12px 40px rgba(0,0,0,.25);',
          '}',
          '.dash-picker-head{display:flex;align-items:center;justify-content:space-between;gap:10px;',
          '  padding:12px 14px;border-bottom:1px solid rgba(0,0,0,.08);background:#fafafa;',
          '}',
          '.dash-picker-title{font-weight:800;font-size:16px;}',
          '.dash-picker-close{border:none;background:transparent;cursor:pointer;width:34px;height:34px;',
          '  border-radius:10px;font-size:16px;',
          '}',
          '.dash-picker-close:hover{background:rgba(0,0,0,.06);}',
          '.dash-picker-grid{display:grid;grid-template-columns:repeat(1,minmax(0,1fr));gap:10px;padding:12px;}',
          '@media (min-width:760px){.dash-picker-grid{grid-template-columns:repeat(4,minmax(0,1fr));}}',
          '.dash-widget-card{display:flex;flex-direction:column;gap:10px;',
          '  border:1px solid rgba(0,0,0,.10);border-radius:12px;padding:12px;cursor:pointer;background:#fff;',
          '}',
          '.dash-widget-card:hover{background:#fafafa;}',
          '.dash-widget-icon{font-size:28px;}',
          '.dash-widget-title{font-weight:800;}',
          '.dash-widget-desc{color:#6f6f6f;font-size:13px;margin-top:2px;}',
          '.dash-widget-action{margin-top:auto;font-weight:800;color:#333;',
          '  padding:8px 10px;border-radius:10px;border:1px solid rgba(0,0,0,.12);text-align:center;',
          '}',
          '.dash-widget-card:hover .dash-widget-action{background:#f6f6f6;}',
          '.dash-widget-card.is-disabled{opacity:.45;cursor:not-allowed;}',
          '.dash-widget-card.is-disabled:hover{background:#fff;}',
          '.dash-widget-card.is-disabled .dash-widget-action{background:#fff;}',
          'body.dash-picker-open{overflow:hidden;}',

          // layout flex
          '.widget-flex{display:flex;flex-direction:column;}',
          '.dash-body{flex:1;min-height:0;}', // важно для правильного ресайза часов/календаря
          '.dash-handle{flex:0 0 auto;}',

          // NEWS
          '.dash-news-row{padding:10px 10px;border-bottom:1px solid rgba(0,0,0,.06);cursor:pointer;}',
          '.dash-news-row:hover{background:rgba(0,0,0,.03);}',
          '.dash-news-title{font-weight:800;}',
          '.dash-news-short{font-size:13px;opacity:.85;margin-top:4px;line-height:1.25;}',

          // CLOCK (большие, занимают почти весь виджет)
          '.dash-clock-wrap{height:100%;width:100%;padding:0;box-sizing:border-box;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:2px;overflow:hidden;}',
          '.dash-clock-time{font-weight:900;line-height:1;letter-spacing:.02em;white-space:nowrap;font-size:var(--dash-clock-size, 80px);font-variant-numeric:tabular-nums;}',
          '.dash-clock-sec{font-size:.38em;opacity:.65;font-weight:800;margin-left:8px;vertical-align:baseline;}',
          '.dash-clock-date{font-size:clamp(11px,1.1vw,16px);opacity:.75;white-space:nowrap;}',

          // RECENT
          '.dash-recent-row{padding:10px 10px;border-bottom:1px solid rgba(0,0,0,.06);cursor:pointer;display:flex;gap:10px;align-items:flex-start;}',
          '.dash-recent-row:hover{background:rgba(0,0,0,.03);}',
          '.dash-recent-ico{width:22px;min-width:22px;opacity:.85;font-size:16px;line-height:1.2;margin-top:2px;}',
          '.dash-recent-main{flex:1;min-width:0;}',
          '.dash-recent-cap{font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}',
          '.dash-recent-meta{font-size:12px;opacity:.75;margin-top:3px;}',

          // BUTTONS (общие для всех .dash-btn)
          '.dash-btn{appearance:none;-webkit-appearance:none;cursor:pointer;',
          '  border:1px solid rgba(0,0,0,.14);background:#fff;color:#222;',
          '  border-radius:10px;padding:6px 10px;font-weight:700;',
          '  transition:background .15s ease, box-shadow .15s ease, transform .05s ease;',
          '}',
          '.dash-btn:hover{background:rgba(0,0,0,.06);box-shadow:0 2px 10px rgba(0,0,0,.10);}',
          '.dash-btn:active{transform:translateY(1px);box-shadow:0 1px 6px rgba(0,0,0,.10);}',
          '.dash-btn:focus-visible{outline:2px solid rgba(0,0,0,.25);outline-offset:2px;}',

          // крестик "убрать виджет"
          '.dash-remove{border:none;background:transparent;cursor:pointer;',
          '  width:30px;height:30px;border-radius:10px;',
          '  transition:background .15s ease, transform .05s ease;',
          '}',
          '.dash-remove:hover{background:rgba(0,0,0,.08);}',
          '.dash-remove:active{transform:translateY(1px);}',

          // CONTEXT MENU (ПКМ)
          '.dash-ctx{position:fixed;z-index:100000;display:none;',
          '  background:#fff;border:1px solid rgba(0,0,0,.12);border-radius:12px;',
          '  box-shadow:0 12px 40px rgba(0,0,0,.18);padding:6px;min-width:230px;',
          '}',
          '.dash-ctx-item{padding:10px 10px;border-radius:10px;cursor:pointer;user-select:none;font-weight:800;}',
          '.dash-ctx-item:hover{background:rgba(0,0,0,.06);}',
          '.dash-ctx-sep{height:1px;background:rgba(0,0,0,.08);margin:6px 4px;}',

          // LOCK MODE
          '.dash-locked .widget-drag-handle{cursor:default;}'
        ].join('\n');

        var st = document.createElement('style');
        st.id = 'dash-picker-styles';
        st.type = 'text/css';
        st.appendChild(document.createTextNode(css));
        document.head.appendChild(st);
      }

      // ===== picker HTML menu =====
      function ensurePicker() {
        if (picker.inited) return;
        picker.inited = true;

        injectPickerStylesOnce();

        var html =
          '<div class="dash-picker-backdrop" style="display:none">' +
          '  <div class="dash-picker" role="dialog" aria-modal="true">' +
          '    <div class="dash-picker-head">' +
          '      <div class="dash-picker-title">Добавить виджет</div>' +
          '      <button type="button" class="dash-picker-close" title="Закрыть">✕</button>' +
          '    </div>' +
          '    <div class="dash-picker-grid">' +
          '      <div class="dash-widget-card" data-widget="widget-clock">' +
          '        <div class="dash-widget-icon">🕒</div>' +
          '        <div><div class="dash-widget-title">Часы</div><div class="dash-widget-desc">Текущее время</div></div>' +
          '        <div class="dash-widget-action">Добавить</div>' +
          '      </div>' +
          '      <div class="dash-widget-card" data-widget="widget-calendar">' +
          '        <div class="dash-widget-icon">📅</div>' +
          '        <div><div class="dash-widget-title">Календарь</div><div class="dash-widget-desc">Заметки по датам</div></div>' +
          '        <div class="dash-widget-action">Добавить</div>' +
          '      </div>' +
          '      <div class="dash-widget-card" data-widget="widget-notes">' +
          '        <div class="dash-widget-icon">📝</div>' +
          '        <div><div class="dash-widget-title">Заметки</div><div class="dash-widget-desc">Список за день</div></div>' +
          '        <div class="dash-widget-action">Добавить</div>' +
          '      </div>' +
          '      <div class="dash-widget-card" data-widget="widget-news">' +
          '        <div class="dash-widget-icon">📰</div>' +
          '        <div><div class="dash-widget-title">Новости</div><div class="dash-widget-desc">Из сущности News</div></div>' +
          '        <div class="dash-widget-action">Добавить</div>' +
          '      </div>' +
          '      <div class="dash-widget-card" data-widget="widget-recent">' +
          '        <div class="dash-widget-icon">🕘</div>' +
          '        <div><div class="dash-widget-title">Последние</div><div class="dash-widget-desc">10 последних документов</div></div>' +
          '        <div class="dash-widget-action">Добавить</div>' +
          '      </div>' +
          '    </div>' +
          '  </div>' +
          '</div>';

        document.body.insertAdjacentHTML('beforeend', html);
        picker.backdrop = document.body.querySelector('.dash-picker-backdrop');

        picker.backdrop.addEventListener('click', function (e) {
          if (e.target === picker.backdrop) closePicker();
        });

        picker.backdrop.querySelector('.dash-picker-close').addEventListener('click', function (e) {
          e.preventDefault(); e.stopPropagation();
          closePicker();
        });

        picker.backdrop.querySelector('.dash-picker-grid').addEventListener('click', function (e) {
          var card = e.target.closest ? e.target.closest('.dash-widget-card') : null;
          if (!card) return;
          if (card.classList.contains('is-disabled')) return;

          var wid = card.getAttribute('data-widget');
          if (!wid) return;

          addWidget(wid, defaultRectById(wid), false);
          refreshPickerAvailability();
          closePicker();
        });

        document.addEventListener('keydown', function (e) {
          if (e.key === 'Escape') closePicker();
        });

        refreshPickerAvailability();
      }

      function refreshPickerAvailability() {
        if (!picker.backdrop) return;
        var cards = picker.backdrop.querySelectorAll('.dash-widget-card[data-widget]');
        for (var i = 0; i < cards.length; i++) {
          var c = cards[i];
          var id = c.getAttribute('data-widget');
          var already = widgetExists(id);

          if (already) c.classList.add('is-disabled');
          else c.classList.remove('is-disabled');

          var act = c.querySelector('.dash-widget-action');
          if (act) act.textContent = already ? 'Уже добавлен' : 'Добавить';
        }
      }

      function openPicker() {
        ensurePicker();
        refreshPickerAvailability();
        picker.backdrop.style.display = 'flex';
        document.body.classList.add('dash-picker-open');
      }

      function closePicker() {
        if (!picker.backdrop) return;
        picker.backdrop.style.display = 'none';
        document.body.classList.remove('dash-picker-open');
      }

      // ===== tile logic =====
      function bindTileLogic(id) {
        var tile = element.querySelector('#' + id);
        if (!tile) return;

        var rm = tile.querySelector('.dash-remove');
        if (rm) {
          rm.addEventListener('click', function (e) {
            e.preventDefault(); e.stopPropagation();
            removeWidget(id);
            refreshPickerAvailability();
          });
        }

        if (id === 'widget-manager') {
          var btnAdd = tile.querySelector('.dash-add');
          var btnReset = tile.querySelector('.dash-reset');
          var btnGE = tile.querySelector('.dash-ge-test');

          btnGE && btnGE.addEventListener('click', function (e) {
            e.preventDefault(); e.stopPropagation();
            if (connector.sendTestGlobalEvent) {
              connector.sendTestGlobalEvent('Пинг: ' + new Date().toLocaleString());
            } else {
              alert('sendTestGlobalEvent не реализован в Java');
            }
          });

          btnAdd && btnAdd.addEventListener('click', function (e) {
            e.preventDefault(); e.stopPropagation();
            openPicker();
          });

          btnReset && btnReset.addEventListener('click', function (e) {
            e.preventDefault(); e.stopPropagation();
            resetLayoutToDefaults();
            refreshPickerAvailability();
          });
        }

        if (id === 'widget-clock') startClock();
        if (id === 'widget-calendar') initCalendar();
        if (id === 'widget-notes') initNotesWidget();
        if (id === 'widget-news') initNewsWidget();
        if (id === 'widget-recent') initRecentWidget();
      }

      // ===== clock =====
      var clockTimer = null;
      var clockRO = null;
      var clockRAF = 0;

      function fitClock() {
        var tile = element.querySelector('#widget-clock');
        if (!tile) return;

        var body = tile.querySelector('.dash-body');
        var timeEl = tile.querySelector('.dash-clock-time');
        var dateEl = tile.querySelector('.dash-clock-date');
        if (!body || !timeEl) return;

        var availW = Math.max(0, body.clientWidth - 8);
        var availH = Math.max(0, body.clientHeight - 8);

        // место под дату
        var reserve = 0;
        if (dateEl) {
          reserve = Math.min(40, Math.max(16, dateEl.offsetHeight || 18)) + 4;
        }
        var availTimeH = Math.max(0, availH - reserve);

        // измеряем в "базовом" размере и масштабируем (чтобы точно влезало)
        var base = 100;
        timeEl.style.fontSize = base + 'px';
        timeEl.style.lineHeight = '1';

        // форсим layout
        var sw = timeEl.scrollWidth || timeEl.getBoundingClientRect().width;
        var sh = timeEl.scrollHeight || timeEl.getBoundingClientRect().height;

        timeEl.style.fontSize = ''; // обратно на CSS var

        if (!sw || !sh) return;

        var scale = Math.min(availW / sw, availTimeH / sh);
        var size = Math.floor(base * scale);

        size = Math.min(260, Math.max(18, size));
        tile.style.setProperty('--dash-clock-size', size + 'px');
      }

      function startClock() {
        stopClock(); // важно: сначала гасим старое

        var tile = element.querySelector('#widget-clock');
        if (!tile) return;

        var timeEl = tile.querySelector('.dash-clock-time');
        var dateEl = tile.querySelector('.dash-clock-date');
        if (!timeEl) return;

        function tick() {
          var now = new Date();
          var hh = pad(now.getHours()), mm = pad(now.getMinutes()), ss = pad(now.getSeconds());
          var dd = pad(now.getDate()), mon = pad(now.getMonth() + 1), yyyy = now.getFullYear();

          timeEl.innerHTML = hh + ':' + mm + '<span class="dash-clock-sec">:' + ss + '</span>';
          if (dateEl) dateEl.textContent = dd + '.' + mon + '.' + yyyy;
        }

        tick();

        clockRAF = requestAnimationFrame(function () { fitClock(); });

        if (window.ResizeObserver) {
          clockRO = new ResizeObserver(function () {
            if (clockRAF) cancelAnimationFrame(clockRAF);
            clockRAF = requestAnimationFrame(fitClock);
          });
          clockRO.observe(tile.querySelector('.dash-body') || tile);
        }

        clockTimer = setInterval(tick, 1000);
      }

      function stopClock() {
        if (clockTimer) { clearInterval(clockTimer); clockTimer = null; }
        if (clockRO) { clockRO.disconnect(); clockRO = null; }
        if (clockRAF) { cancelAnimationFrame(clockRAF); clockRAF = 0; }
      }

      // ===== calendar + notes =====
      var cal = { sc: null, $c: null, selectedIso: null };
      function resetCalendarState() { cal.sc = null; cal.$c = null; cal.selectedIso = null; }

      function renderCalendarDetails(iso, events) {
        var $ = $jq();
        if (!cal.$c || !$) return;

        var $wrapper = cal.$c.find('.event-wrapper');
        if (!$wrapper.length) return;

        $wrapper.empty();

        var $btn = $('<button type="button" class="sc-create-note">+ Создать заметку</button>');
        $btn.on('click', function (e) {
          e.preventDefault(); e.stopPropagation();
          if (connector.createNoteForDate) connector.createNoteForDate(iso);
        });
        $wrapper.append($('<div class="sc-note-actions"></div>').append($btn));

        if (events && events.length) {
          events.forEach(function (ev) {
            var $row = $('<div class="event sc-note-row"></div>');
            var $summary = $('<div class="event-summary sc-note-summary"></div>').text(ev.summary || '');
            var $del = $('<button type="button" class="sc-note-del">Удалить</button>');

            $del.on('click', function (e) {
              e.preventDefault(); e.stopPropagation();
              if (ev.noteId && connector.confirmDeleteNote) connector.confirmDeleteNote(String(ev.noteId), String(iso));
            });

            $row.on('click', function () {
              if (ev.noteId && connector.openNote) connector.openNote(String(ev.noteId));
            });

            $row.append($summary).append($del);
            $wrapper.append($row);
          });
        } else {
          $wrapper.append('<div class="sc-empty">Нет заметок</div>');
        }
      }

      function initCalendar() {
        var $ = $jq();
        var holder = element.querySelector('#widget-calendar .calendar-widget-container');
        if (!holder || !$) return;

        if ($(holder).data('scInit')) return;
        $(holder).data('scInit', true);

        $(holder).simpleCalendar({
          months: ['январь','февраль','март','апрель','май','июнь','июль','август','сентябрь','октябрь','ноябрь','декабрь'],
          days: ['вс','пн','вт','ср','чт','пт','сб'],
          displayYear: true,
          fixedStartDay: true,

          displayEvent: true,
          disableEventDetails: false,
          disableEmptyDetails: false,
          events: [],

          onInit: function () {
            cal.$c = $(holder);
            cal.sc = cal.$c.data('plugin_simpleCalendar') || cal.$c.data('simpleCalendar');

            var now = new Date();
            if (connector.requestNotesForMonth) connector.requestNotesForMonth(now.getFullYear(), now.getMonth() + 1);
          },

          onMonthChange: function (month, year) {
            var m = parseInt(month, 10);
            if (m >= 0 && m <= 11) m = m + 1;
            if (connector.requestNotesForMonth) connector.requestNotesForMonth(year, m);
          },

          onDateSelect: function (date, events) {
            var d = (date instanceof Date) ? date : new Date(date);
            var iso = toIso(d);
            cal.selectedIso = iso;

            renderCalendarDetails(iso, events || []);
            if (widgetExists('widget-notes')) requestDayNotes(iso);
          }
        });
      }

      function initNotesWidget() {
        var tile = element.querySelector('#widget-notes');
        if (!tile) return;

        var btn = tile.querySelector('.dash-create-note');
        btn && btn.addEventListener('click', function (e) {
          e.preventDefault(); e.stopPropagation();
          var iso = cal.selectedIso || toIso(new Date());
          if (connector.createNoteForDate) connector.createNoteForDate(iso);
        });

        requestDayNotes(cal.selectedIso || toIso(new Date()));
      }

      function requestDayNotes(iso) {
        var tile = element.querySelector('#widget-notes');
        if (!tile) return;

        tile.querySelector('.dash-notes-date').textContent = 'Дата: ' + iso;
        tile.querySelector('.dash-notes-list').innerHTML = '<div class="dash-muted">Загрузка...</div>';

        if (connector.requestNotesForDay) connector.requestNotesForDay(iso);
      }

      // ===== NEWS =====
      function initNewsWidget() {
        var tile = element.querySelector('#widget-news');
        if (!tile) return;

        var btn = tile.querySelector('.dash-news-refresh');
        btn && btn.addEventListener('click', function (e) {
          e.preventDefault(); e.stopPropagation();
          tile.querySelector('.dash-news-list').innerHTML = '<div class="dash-muted">Загрузка...</div>';
          if (connector.requestNews) connector.requestNews();
        });

        tile.querySelector('.dash-news-list').innerHTML = '<div class="dash-muted">Загрузка...</div>';
        if (connector.requestNews) connector.requestNews();
        else tile.querySelector('.dash-news-list').innerHTML = '<div class="dash-muted">requestNews не реализован в Java</div>';
      }

      // ===== RECENT =====
      function initRecentWidget() {
        var tile = element.querySelector('#widget-recent');
        if (!tile) return;

        var list = tile.querySelector('.dash-recent-list');
        var btn = tile.querySelector('.dash-recent-refresh');

        function load() {
          list.innerHTML = '<div class="dash-muted">Загрузка...</div>';
          if (connector.requestRecentDocs) connector.requestRecentDocs(10);
          else list.innerHTML = '<div class="dash-muted">requestRecentDocs не реализован в Java</div>';
        }

        btn && btn.addEventListener('click', function (e) {
          e.preventDefault(); e.stopPropagation();
          load();
        });

        load();
      }

      // ===== RPC from Java =====
      connector.applyCalendarEvents = function (eventsJson) {
        if (!cal.$c) return;

        var arr = [];
        try { arr = JSON.parse(eventsJson || '[]'); } catch (e) { arr = []; }

        var sc = cal.sc || (cal.$c.data('plugin_simpleCalendar') || cal.$c.data('simpleCalendar'));
        if (!sc) return;

        if (typeof sc.setEvents === 'function') sc.setEvents(arr);
        else if (sc.settings) sc.settings.events = arr;

        if (cal.selectedIso && typeof sc.getDateEvents === 'function') {
          renderCalendarDetails(cal.selectedIso, sc.getDateEvents(isoToDate(cal.selectedIso)));
        }
      };

      connector.applyDayNotes = function (iso, json) {
        var tile = element.querySelector('#widget-notes');
        if (!tile) return;

        var list = tile.querySelector('.dash-notes-list');
        var arr = [];
        try { arr = JSON.parse(json || '[]'); } catch (e) { arr = []; }

        if (!arr.length) {
          list.innerHTML = '<div class="dash-muted">Нет заметок</div>';
          return;
        }

        list.innerHTML = '';
        arr.forEach(function (it) {
          var row = document.createElement('div');
          row.className = 'dash-note-row';
          row.innerHTML =
            '<div class="dash-note-text"></div>' +
            '<button type="button" class="dash-note-del">Удалить</button>';

          row.querySelector('.dash-note-text').textContent = it.summary || '';

          row.addEventListener('click', function () {
            if (it.noteId && connector.openNote) connector.openNote(String(it.noteId));
          });

          row.querySelector('.dash-note-del').addEventListener('click', function (e) {
            e.preventDefault(); e.stopPropagation();
            if (it.noteId && connector.confirmDeleteNote) connector.confirmDeleteNote(String(it.noteId), String(iso));
          });

          list.appendChild(row);
        });
      };

      connector.refreshAfterNoteChange = function (iso) {
        var d = isoToDate(iso);
        if (connector.requestNotesForMonth) connector.requestNotesForMonth(d.getFullYear(), d.getMonth() + 1);
        if (widgetExists('widget-notes')) requestDayNotes(iso);
      };

      connector.noteDeleted = function (noteId, iso) {
        connector.refreshAfterNoteChange(String(iso || ''));
      };

      connector.applyNews = function (json) {
        var tile = element.querySelector('#widget-news');
        if (!tile) return;

        var list = tile.querySelector('.dash-news-list');
        var arr = [];
        try { arr = JSON.parse(json || '[]'); } catch (e) { arr = []; }

        if (!arr.length) {
          list.innerHTML = '<div class="dash-muted">Нет новостей</div>';
          return;
        }

        list.innerHTML = '';
        arr.forEach(function (n) {
          var row = document.createElement('div');
          row.className = 'dash-news-row';

          var t = document.createElement('div');
          t.className = 'dash-news-title';
          t.textContent = n.title || '';
          row.appendChild(t);

          if (n.shortText) {
            var st = document.createElement('div');
            st.className = 'dash-news-short';
            st.textContent = n.shortText;
            row.appendChild(st);
          }

          row.addEventListener('click', function () {
            if (n.id && connector.openNews) connector.openNews(String(n.id));
          });

          list.appendChild(row);
        });
      };

      connector.applyRecentDocs = function (json) {
        var tile = element.querySelector('#widget-recent');
        if (!tile) return;

        var list = tile.querySelector('.dash-recent-list');
        var arr = [];
        try { arr = JSON.parse(json || '[]'); } catch (e) { arr = []; }

        if (!arr.length) {
          list.innerHTML = '<div class="dash-muted">Пока пусто</div>';
          return;
        }

        list.innerHTML = '';
        arr.forEach(function (it) {
          var row = document.createElement('div');
          row.className = 'dash-recent-row';

          var ico = '📄';
          if (it.entityName === 'untitled16_Notes') ico = '📝';
          if (it.entityName === 'untitled16_News')  ico = '📰';

          var left = document.createElement('div');
          left.className = 'dash-recent-ico';
          left.textContent = ico;

          var main = document.createElement('div');
          main.className = 'dash-recent-main';

          var cap = document.createElement('div');
          cap.className = 'dash-recent-cap';
          cap.textContent = it.caption || 'Новая запись';

          var meta = document.createElement('div');
          meta.className = 'dash-recent-meta';
          var dt = it.visitedTs ? new Date(it.visitedTs) : null;
          meta.textContent = (it.entityName || '') + (dt ? (' • ' + dt.toLocaleString()) : '');

          main.appendChild(cap);
          main.appendChild(meta);

          row.appendChild(left);
          row.appendChild(main);

          row.addEventListener('click', function () {
            if (connector.openRecentDoc && it.entityName && it.entityId) {
              connector.openRecentDoc(String(it.entityName), String(it.entityId));
            }
          });

          list.appendChild(row);
        });
      };

      connector.addWidgetById = function (id) {
        addWidget(String(id || ''), defaultRectById(String(id || '')), false);
        refreshPickerAvailability();
      };

      // ===== init =====
      connector.initDashboard = function () {
        var tries = 0;

        (function tick() {
          if (!depsReady()) {
            if (tries++ < 200) return setTimeout(tick, 50);
            return;
          }

          injectPickerStylesOnce();
          setRootHtmlOnce();

          var $ = $jq();
          $grid = $('.dash-grid', element);

          if (!grid) {
            grid = GridStack.init({
              column: 12,
              cellHeight: 80,
              margin: 5,
              float: true,
              maxRow: 12,
              draggable: { handle: '.widget-drag-handle' }
            }, $grid[0]);

            if (typeof grid.on === 'function') {
              grid.on('change', function () { saveLayout(); });
            }

            $grid.off('.gsSave');
            $grid.on('dragstop.gsSave resizestop.gsSave', function () { saveLayout(); });
          }

          // ПКМ-меню + восстановление lock-режима
          bindContextMenuOnce();
          restoreLockedState();

          var ok = restoreLayout();
          var hasAny = element.querySelectorAll('.grid-stack-item[id^="widget-"]').length > 0;

          if (!ok || !hasAny) {
            addWidget('widget-manager', defaultRectById('widget-manager'), true);
            addWidget('widget-clock', defaultRectById('widget-clock'), true);
            addWidget('widget-calendar', defaultRectById('widget-calendar'), true);
            addWidget('widget-notes', defaultRectById('widget-notes'), true);
            addWidget('widget-news', defaultRectById('widget-news'), true);
            addWidget('widget-recent', defaultRectById('widget-recent'), true);
            saveLayout();
          }

          refreshPickerAvailability();
        })();
      };

      var _dashStarted = false;

      this.onStateChange = function () {
        if (_dashStarted) return;
        _dashStarted = true;
        if (connector.initDashboard) connector.initDashboard();
      };

      setTimeout(function () {
        if (_dashStarted) return;
        _dashStarted = true;
        if (connector.initDashboard) connector.initDashboard();
      }, 0);

      this.onUnregister = function () {
        stopClock();
        resetCalendarState();
        closePicker();
        closeContextMenu();
        unbindContextMenu();

        if (picker.backdrop) {
          try { picker.backdrop.remove(); } catch (e) {}
          picker.backdrop = null;
          picker.inited = false;
        }

        grid = null;
        $grid = null;
      };
    };


    picker.backdrop.addEventListener('click', function (e) {
      if (e.target === picker.backdrop) closePicker();
    });

    picker.backdrop.querySelector('.dash-picker-close').addEventListener('click', function (e) {
      e.preventDefault(); e.stopPropagation();
      closePicker();
    });

    picker.backdrop.querySelector('.dash-picker-grid').addEventListener('click', function (e) {
      var card = e.target.closest ? e.target.closest('.dash-widget-card') : null;
      if (!card) return;
      if (card.classList.contains('is-disabled')) return;

      var wid = card.getAttribute('data-widget');
      if (!wid) return;

      addWidget(wid, defaultRectById(wid), false);
      refreshPickerAvailability();
      closePicker();
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') closePicker();
    });

    refreshPickerAvailability();
  }

  function refreshPickerAvailability() {
    if (!picker.backdrop) return;
    var cards = picker.backdrop.querySelectorAll('.dash-widget-card[data-widget]');
    for (var i = 0; i < cards.length; i++) {
      var c = cards[i];
      var id = c.getAttribute('data-widget');
      var already = widgetExists(id);

      if (already) c.classList.add('is-disabled');
      else c.classList.remove('is-disabled');

      var act = c.querySelector('.dash-widget-action');
      if (act) act.textContent = already ? 'Уже добавлен' : 'Добавить';
    }
  }

  function openPicker() {
    ensurePicker();
    refreshPickerAvailability();
    picker.backdrop.style.display = 'flex';
    document.body.classList.add('dash-picker-open');
  }

  function closePicker() {
    if (!picker.backdrop) return;
    picker.backdrop.style.display = 'none';
    document.body.classList.remove('dash-picker-open');
  }

  // ===== tile logic =====
  function bindTileLogic(id) {
    var tile = element.querySelector('#' + id);
    if (!tile) return;

    var rm = tile.querySelector('.dash-remove');
    if (rm) {
      rm.addEventListener('click', function (e) {
        e.preventDefault(); e.stopPropagation();
        removeWidget(id);
        refreshPickerAvailability();
      });
    }

    if (id === 'widget-manager') {
      var btnAdd = tile.querySelector('.dash-add');
      var btnReset = tile.querySelector('.dash-reset');
      var btnGE = tile.querySelector('.dash-ge-test');

      btnGE && btnGE.addEventListener('click', function (e) {
        e.preventDefault(); e.stopPropagation();
        if (connector.sendTestGlobalEvent) {
          connector.sendTestGlobalEvent('Пинг: ' + new Date().toLocaleString());
        } else {
          alert('sendTestGlobalEvent не реализован в Java');
        }
      });

      btnAdd && btnAdd.addEventListener('click', function (e) {
        e.preventDefault(); e.stopPropagation();
        openPicker();
      });

      btnReset && btnReset.addEventListener('click', function (e) {
        e.preventDefault(); e.stopPropagation();
        resetLayoutToDefaults();
        refreshPickerAvailability();
      });
    }

    if (id === 'widget-clock') startClock();
    if (id === 'widget-calendar') initCalendar();
    if (id === 'widget-notes') initNotesWidget();
    if (id === 'widget-news') initNewsWidget();
    if (id === 'widget-recent') initRecentWidget();
  }

  // ===== clock =====
  var clockTimer = null;
  var clockRO = null;
  var clockRAF = 0;

  function fitClock() {
    var tile = element.querySelector('#widget-clock');
    if (!tile) return;

    var body = tile.querySelector('.dash-body');
    var timeEl = tile.querySelector('.dash-clock-time');
    var dateEl = tile.querySelector('.dash-clock-date');
    if (!body || !timeEl) return;

    var availW = Math.max(0, body.clientWidth - 8);
    var availH = Math.max(0, body.clientHeight - 8);

    // место под дату
    var reserve = 0;
    if (dateEl) {
      reserve = Math.min(40, Math.max(16, dateEl.offsetHeight || 18)) + 4;
    }
    var availTimeH = Math.max(0, availH - reserve);

    // измеряем в "базовом" размере и масштабируем (чтобы точно влезало)
    var base = 100;
    timeEl.style.fontSize = base + 'px';
    timeEl.style.lineHeight = '1';

    // форсим layout
    var sw = timeEl.scrollWidth || timeEl.getBoundingClientRect().width;
    var sh = timeEl.scrollHeight || timeEl.getBoundingClientRect().height;

    timeEl.style.fontSize = ''; // обратно на CSS var

    if (!sw || !sh) return;

    var scale = Math.min(availW / sw, availTimeH / sh);
    var size = Math.floor(base * scale);

    size = Math.min(260, Math.max(18, size));
    tile.style.setProperty('--dash-clock-size', size + 'px');
  }

  function startClock() {
    stopClock(); // важно: сначала гасим старое

    var tile = element.querySelector('#widget-clock');
    if (!tile) return;

    var timeEl = tile.querySelector('.dash-clock-time');
    var dateEl = tile.querySelector('.dash-clock-date');
    if (!timeEl) return;

    function tick() {
      var now = new Date();
      var hh = pad(now.getHours()), mm = pad(now.getMinutes()), ss = pad(now.getSeconds());
      var dd = pad(now.getDate()), mon = pad(now.getMonth() + 1), yyyy = now.getFullYear();

      timeEl.innerHTML = hh + ':' + mm + '<span class="dash-clock-sec">:' + ss + '</span>';
      if (dateEl) dateEl.textContent = dd + '.' + mon + '.' + yyyy;
    }

    tick();

    clockRAF = requestAnimationFrame(function () { fitClock(); });

    if (window.ResizeObserver) {
      clockRO = new ResizeObserver(function () {
        if (clockRAF) cancelAnimationFrame(clockRAF);
        clockRAF = requestAnimationFrame(fitClock);
      });
      clockRO.observe(tile.querySelector('.dash-body') || tile);
    }

    clockTimer = setInterval(tick, 1000);
  }

  function stopClock() {
    if (clockTimer) { clearInterval(clockTimer); clockTimer = null; }
    if (clockRO) { clockRO.disconnect(); clockRO = null; }
    if (clockRAF) { cancelAnimationFrame(clockRAF); clockRAF = 0; }
  }

  // ===== calendar + notes =====
  var cal = { sc: null, $c: null, selectedIso: null };
  function resetCalendarState() { cal.sc = null; cal.$c = null; cal.selectedIso = null; }

  function renderCalendarDetails(iso, events) {
    var $ = $jq();
    if (!cal.$c || !$) return;

    var $wrapper = cal.$c.find('.event-wrapper');
    if (!$wrapper.length) return;

    $wrapper.empty();

    var $btn = $('<button type="button" class="sc-create-note">+ Создать заметку</button>');
    $btn.on('click', function (e) {
      e.preventDefault(); e.stopPropagation();
      if (connector.createNoteForDate) connector.createNoteForDate(iso);
    });
    $wrapper.append($('<div class="sc-note-actions"></div>').append($btn));

    if (events && events.length) {
      events.forEach(function (ev) {
        var $row = $('<div class="event sc-note-row"></div>');
        var $summary = $('<div class="event-summary sc-note-summary"></div>').text(ev.summary || '');
        var $del = $('<button type="button" class="sc-note-del">Удалить</button>');

        $del.on('click', function (e) {
          e.preventDefault(); e.stopPropagation();
          if (ev.noteId && connector.confirmDeleteNote) connector.confirmDeleteNote(String(ev.noteId), String(iso));
        });

        $row.on('click', function () {
          if (ev.noteId && connector.openNote) connector.openNote(String(ev.noteId));
        });

        $row.append($summary).append($del);
        $wrapper.append($row);
      });
    } else {
      $wrapper.append('<div class="sc-empty">Нет заметок</div>');
    }
  }

  function initCalendar() {
    var $ = $jq();
    var holder = element.querySelector('#widget-calendar .calendar-widget-container');
    if (!holder || !$) return;

    if ($(holder).data('scInit')) return;
    $(holder).data('scInit', true);

    $(holder).simpleCalendar({
      months: ['январь','февраль','март','апрель','май','июнь','июль','август','сентябрь','октябрь','ноябрь','декабрь'],
      days: ['вс','пн','вт','ср','чт','пт','сб'],
      displayYear: true,
      fixedStartDay: true,

      displayEvent: true,
      disableEventDetails: false,
      disableEmptyDetails: false,
      events: [],

      onInit: function () {
        cal.$c = $(holder);
        cal.sc = cal.$c.data('plugin_simpleCalendar') || cal.$c.data('simpleCalendar');

        var now = new Date();
        if (connector.requestNotesForMonth) connector.requestNotesForMonth(now.getFullYear(), now.getMonth() + 1);
      },

      onMonthChange: function (month, year) {
        var m = parseInt(month, 10);
        if (m >= 0 && m <= 11) m = m + 1;
        if (connector.requestNotesForMonth) connector.requestNotesForMonth(year, m);
      },

      onDateSelect: function (date, events) {
        var d = (date instanceof Date) ? date : new Date(date);
        var iso = toIso(d);
        cal.selectedIso = iso;

        renderCalendarDetails(iso, events || []);
        if (widgetExists('widget-notes')) requestDayNotes(iso);
      }
    });
  }

  function initNotesWidget() {
    var tile = element.querySelector('#widget-notes');
    if (!tile) return;

    var btn = tile.querySelector('.dash-create-note');
    btn && btn.addEventListener('click', function (e) {
      e.preventDefault(); e.stopPropagation();
      var iso = cal.selectedIso || toIso(new Date());
      if (connector.createNoteForDate) connector.createNoteForDate(iso);
    });

    requestDayNotes(cal.selectedIso || toIso(new Date()));
  }

  function requestDayNotes(iso) {
    var tile = element.querySelector('#widget-notes');
    if (!tile) return;

    tile.querySelector('.dash-notes-date').textContent = 'Дата: ' + iso;
    tile.querySelector('.dash-notes-list').innerHTML = '<div class="dash-muted">Загрузка...</div>';

    if (connector.requestNotesForDay) connector.requestNotesForDay(iso);
  }

  // ===== NEWS =====
  function initNewsWidget() {
    var tile = element.querySelector('#widget-news');
    if (!tile) return;

    var btn = tile.querySelector('.dash-news-refresh');
    btn && btn.addEventListener('click', function (e) {
      e.preventDefault(); e.stopPropagation();
      tile.querySelector('.dash-news-list').innerHTML = '<div class="dash-muted">Загрузка...</div>';
      if (connector.requestNews) connector.requestNews();
    });

    tile.querySelector('.dash-news-list').innerHTML = '<div class="dash-muted">Загрузка...</div>';
    if (connector.requestNews) connector.requestNews();
    else tile.querySelector('.dash-news-list').innerHTML = '<div class="dash-muted">requestNews не реализован в Java</div>';
  }

  // ===== RECENT =====
  function initRecentWidget() {
    var tile = element.querySelector('#widget-recent');
    if (!tile) return;

    var list = tile.querySelector('.dash-recent-list');
    var btn = tile.querySelector('.dash-recent-refresh');

    function load() {
      list.innerHTML = '<div class="dash-muted">Загрузка...</div>';
      if (connector.requestRecentDocs) connector.requestRecentDocs(10);
      else list.innerHTML = '<div class="dash-muted">requestRecentDocs не реализован в Java</div>';
    }

    btn && btn.addEventListener('click', function (e) {
      e.preventDefault(); e.stopPropagation();
      load();
    });

    load();
  }

  // ===== RPC from Java =====
  connector.applyCalendarEvents = function (eventsJson) {
    if (!cal.$c) return;

    var arr = [];
    try { arr = JSON.parse(eventsJson || '[]'); } catch (e) { arr = []; }

    var sc = cal.sc || (cal.$c.data('plugin_simpleCalendar') || cal.$c.data('simpleCalendar'));
    if (!sc) return;

    if (typeof sc.setEvents === 'function') sc.setEvents(arr);
    else if (sc.settings) sc.settings.events = arr;

    if (cal.selectedIso && typeof sc.getDateEvents === 'function') {
      renderCalendarDetails(cal.selectedIso, sc.getDateEvents(isoToDate(cal.selectedIso)));
    }
  };

  connector.applyDayNotes = function (iso, json) {
    var tile = element.querySelector('#widget-notes');
    if (!tile) return;

    var list = tile.querySelector('.dash-notes-list');
    var arr = [];
    try { arr = JSON.parse(json || '[]'); } catch (e) { arr = []; }

    if (!arr.length) {
      list.innerHTML = '<div class="dash-muted">Нет заметок</div>';
      return;
    }

    list.innerHTML = '';
    arr.forEach(function (it) {
      var row = document.createElement('div');
      row.className = 'dash-note-row';
      row.innerHTML =
        '<div class="dash-note-text"></div>' +
        '<button type="button" class="dash-note-del">Удалить</button>';

      row.querySelector('.dash-note-text').textContent = it.summary || '';

      row.addEventListener('click', function () {
        if (it.noteId && connector.openNote) connector.openNote(String(it.noteId));
      });

      row.querySelector('.dash-note-del').addEventListener('click', function (e) {
        e.preventDefault(); e.stopPropagation();
        if (it.noteId && connector.confirmDeleteNote) connector.confirmDeleteNote(String(it.noteId), String(iso));
      });

      list.appendChild(row);
    });
  };

  connector.refreshAfterNoteChange = function (iso) {
    var d = isoToDate(iso);
    if (connector.requestNotesForMonth) connector.requestNotesForMonth(d.getFullYear(), d.getMonth() + 1);
    if (widgetExists('widget-notes')) requestDayNotes(iso);
  };

  connector.noteDeleted = function (noteId, iso) {
    connector.refreshAfterNoteChange(String(iso || ''));
  };

  connector.applyNews = function (json) {
    var tile = element.querySelector('#widget-news');
    if (!tile) return;

    var list = tile.querySelector('.dash-news-list');
    var arr = [];
    try { arr = JSON.parse(json || '[]'); } catch (e) { arr = []; }

    if (!arr.length) {
      list.innerHTML = '<div class="dash-muted">Нет новостей</div>';
      return;
    }

    list.innerHTML = '';
    arr.forEach(function (n) {
      var row = document.createElement('div');
      row.className = 'dash-news-row';

      var t = document.createElement('div');
      t.className = 'dash-news-title';
      t.textContent = n.title || '';
      row.appendChild(t);

      if (n.shortText) {
        var st = document.createElement('div');
        st.className = 'dash-news-short';
        st.textContent = n.shortText;
        row.appendChild(st);
      }

      row.addEventListener('click', function () {
        if (n.id && connector.openNews) connector.openNews(String(n.id));
      });

      list.appendChild(row);
    });
  };

  connector.applyRecentDocs = function (json) {
    var tile = element.querySelector('#widget-recent');
    if (!tile) return;

    var list = tile.querySelector('.dash-recent-list');
    var arr = [];
    try { arr = JSON.parse(json || '[]'); } catch (e) { arr = []; }

    if (!arr.length) {
      list.innerHTML = '<div class="dash-muted">Пока пусто</div>';
      return;
    }

    list.innerHTML = '';
    arr.forEach(function (it) {
      var row = document.createElement('div');
      row.className = 'dash-recent-row';

      var ico = '📄';
      if (it.entityName === 'untitled16_Notes') ico = '📝';
      if (it.entityName === 'untitled16_News')  ico = '📰';

      var left = document.createElement('div');
      left.className = 'dash-recent-ico';
      left.textContent = ico;

      var main = document.createElement('div');
      main.className = 'dash-recent-main';

      var cap = document.createElement('div');
      cap.className = 'dash-recent-cap';
      cap.textContent = it.caption || 'Новая запись';

      var meta = document.createElement('div');
      meta.className = 'dash-recent-meta';
      var dt = it.visitedTs ? new Date(it.visitedTs) : null;
      meta.textContent = (it.entityName || '') + (dt ? (' • ' + dt.toLocaleString()) : '');

      main.appendChild(cap);
      main.appendChild(meta);

      row.appendChild(left);
      row.appendChild(main);

      row.addEventListener('click', function () {
        if (connector.openRecentDoc && it.entityName && it.entityId) {
          connector.openRecentDoc(String(it.entityName), String(it.entityId));
        }
      });

      list.appendChild(row);
    });
  };

  connector.addWidgetById = function (id) {
    addWidget(String(id || ''), defaultRectById(String(id || '')), false);
    refreshPickerAvailability();
  };

  // ===== init =====
  connector.initDashboard = function () {
    var tries = 0;

    (function tick() {
      if (!depsReady()) {
        if (tries++ < 200) return setTimeout(tick, 50);
        return;
      }

      injectPickerStylesOnce();
      setRootHtmlOnce();

      var $ = $jq();
      $grid = $('.dash-grid', element);

      if (!grid) {
        grid = GridStack.init({
          column: 12,
          cellHeight: 80,
          margin: 5,
          float: true,
          maxRow: 12,
          draggable: { handle: '.widget-drag-handle' }
        }, $grid[0]);

        if (typeof grid.on === 'function') {
          grid.on('change', function () { saveLayout(); });
        }

        $grid.off('.gsSave');
        $grid.on('dragstop.gsSave resizestop.gsSave', function () { saveLayout(); });
      }

      // ПКМ-меню + восстановление lock-режима
      bindContextMenuOnce();
      restoreLockedState();

      var ok = restoreLayout();
      var hasAny = element.querySelectorAll('.grid-stack-item[id^="widget-"]').length > 0;

      if (!ok || !hasAny) {
        addWidget('widget-manager', defaultRectById('widget-manager'), true);
        addWidget('widget-clock', defaultRectById('widget-clock'), true);
        addWidget('widget-calendar', defaultRectById('widget-calendar'), true);
        addWidget('widget-notes', defaultRectById('widget-notes'), true);
        addWidget('widget-news', defaultRectById('widget-news'), true);
        addWidget('widget-recent', defaultRectById('widget-recent'), true);
        saveLayout();
      }

      refreshPickerAvailability();
    })();
  };

  var _dashStarted = false;

  this.onStateChange = function () {
    if (_dashStarted) return;
    _dashStarted = true;
    if (connector.initDashboard) connector.initDashboard();
  };

  setTimeout(function () {
    if (_dashStarted) return;
    _dashStarted = true;
    if (connector.initDashboard) connector.initDashboard();
  }, 0);

  this.onUnregister = function () {
    stopClock();
    resetCalendarState();
    closePicker();
    closeContextMenu();
    unbindContextMenu();

    if (picker.backdrop) {
      try { picker.backdrop.remove(); } catch (e) {}
      picker.backdrop = null;
      picker.inited = false;
    }

    grid = null;
    $grid = null;
  };
};
