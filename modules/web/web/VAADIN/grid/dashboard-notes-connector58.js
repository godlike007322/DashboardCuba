// initFunctionName в XML: com_company_untitled16_web_ui_components_jscomponent_GridDashboard
com_company_untitled16_web_ui_components_jscomponent_GridDashboard = function () {
  var connector = this;
  var element = connector.getElement();
  element.style.display = "none";

  var clockTimerId = null;

  var grid = null;
  var $grid = null;
  var gridHostEl = null;

  var MAX_ROW = 12;
  var loadedNoteIds = {}; // noteId -> true

  function $jq() { return window.jQuery || window.$; }

  function gridDepsReady() {
    var $ = $jq();
    return !!($ && window.GridStack);
  }

  function calDepsReady() {
    var $ = $jq();
    return !!($ && $.fn && $.fn.simpleCalendar);
  }

  function inGrid(sel) {
    var $ = $jq();
    if (!$) return null;
    return ($grid && $grid.length) ? $grid.find(sel) : $(sel);
  }

  function pad(n) { return n < 10 ? '0' + n : '' + n; }

  function normMonth(m) {
    m = parseInt(m, 10);
    if (m >= 0 && m <= 11) return m + 1;
    return m;
  }

  // безопасный парсер ISO yyyy-mm-dd без сдвигов timezone
  function isoToDate(iso) {
    var p = String(iso || '').split('-');
    if (p.length !== 3) return new Date();
    return new Date(parseInt(p[0], 10), parseInt(p[1], 10) - 1, parseInt(p[2], 10));
  }

  function toIso(d) {
    var y = d.getFullYear();
    var m = ('0' + (d.getMonth() + 1)).slice(-2);
    var day = ('0' + d.getDate()).slice(-2);
    return y + '-' + m + '-' + day;
  }

 function isModalOpen() {
   // во время анимации curtain может исчезнуть раньше окна — поэтому проверяем и само окно
   return !!document.querySelector('.v-window-modalitycurtain, .v-window-modal, .v-window');
 }
var _modalObs = null;
var _modalOffTimer = null;

function syncModalBodyFlag() {
  if (isModalOpen()) {
    if (_modalOffTimer) { clearTimeout(_modalOffTimer); _modalOffTimer = null; }
    document.body.classList.add('gs-modal-open');
  } else {
    // держим блокировку чуть дольше, чем длится fade-out окна
    if (_modalOffTimer) clearTimeout(_modalOffTimer);
    _modalOffTimer = setTimeout(function () {
      document.body.classList.remove('gs-modal-open');
    }, 300);
  }
}

function installModalObserverOnce() {
  if (_modalObs) return;
  syncModalBodyFlag();
  _modalObs = new MutationObserver(syncModalBodyFlag);
  _modalObs.observe(document.body, { childList: true, subtree: true });
}


  function runAfterModalClosed(fn) {
    var tries = 0;
    (function tick() {
      if (isModalOpen()) {
        if (tries++ < 80) return setTimeout(tick, 25); // ~2 сек
        return;
      }
      requestAnimationFrame(fn);
    })();
  }

  // ===== CLOCK =====
  function updateClock() {
    var $ = $jq();
    if (!$) return;

    var $clock = inGrid('.clock-widget-container');
    if (!$clock || !$clock.length) return;

    var now = new Date();
    var text = pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds());
    $clock.text(text);

    var height = $clock.height();
    var width = $clock.width();

    if (height && width) {
      var sizeByHeight = height * 0.6;
      var sizeByWidth = width / (text.length * 0.7);
      var fontSize = Math.max(14, Math.min(sizeByHeight, sizeByWidth));

      $clock.css({
        'font-size': fontSize + 'px',
        'line-height': height + 'px',
        'text-align': 'center'
      });
    }
  }

  function initClockOnce() {
    updateClock();
    if (clockTimerId) return;
    clockTimerId = setInterval(updateClock, 1000);
  }

  // ===== CALENDAR: helper render =====
  function renderWrapperWithEvents($c, iso, events) {
    var $ = $jq();
    if (!$) return;

    var $wrapper = $c.find('.event-wrapper');
    if (!$wrapper.length) return;

    $wrapper.empty();

    // кнопка "Создать"
    var $btn = $('<button type="button" class="sc-create-note">+ Создать заметку</button>');
    $btn.on('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      if (connector.createNoteForDate) connector.createNoteForDate(iso);
    });
    $wrapper.append($('<div class="sc-note-actions"></div>').append($btn));

    if (events && events.length) {
      events.forEach(function (ev) {
        var $row = $('<div class="event sc-note-row"></div>').data('event', ev);

        var $summary = $('<div class="event-summary sc-note-summary"></div>').text(ev.summary || '');

        var $del = $('<button type="button" class="sc-note-del">Удалить</button>');
        $del.on('click', function (e) {
          e.preventDefault();
          e.stopPropagation();
          if (ev.noteId && connector.confirmDeleteNote) {
            connector.confirmDeleteNote(String(ev.noteId), String(iso));
          }
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

  function updateHasEventMark($c, sc, iso) {
    if (!sc || typeof sc.getDateEvents !== 'function') return;

    var dayDate = isoToDate(iso);
    var cnt = sc.getDateEvents(dayDate).length;

    // day элементы хранят data-date, ищем нужный
    var $day = $c.find('.day').filter(function () {
      var d = new Date($jq()(this).data('date'));
      return toIso(d) === iso;
    }).first();

    if ($day.length) {
      if (cnt > 0) $day.addClass('has-event');
      else $day.removeClass('has-event');
    }
  }

  function whenCalendarReady(cb) {
    var tries = 0;
    (function tick() {
      if (!calDepsReady()) {
        if (tries++ < 200) return setTimeout(tick, 50);
        return;
      }

      ensureCalendar();

      var $cals = inGrid('.calendar-widget-container');
      if (!$cals || !$cals.length) {
        if (tries++ < 200) return setTimeout(tick, 50);
        return;
      }

      var ok = false;
      $cals.each(function () {
        var $c = $jq()(this);
        var sc = $c.data('plugin_simpleCalendar') || $c.data('simpleCalendar');
        if (sc && sc.addEvent) ok = true;
      });

      if (!ok) {
        if (tries++ < 200) return setTimeout(tick, 50);
        return;
      }

      cb($cals);
    })();
  }

  // ===== CALENDAR RPC from Java =====

  connector.applyCalendarEvents = function (eventsJson) {
    var arr = [];
    try { arr = JSON.parse(eventsJson || '[]'); } catch (e) { arr = []; }

    whenCalendarReady(function ($cals) {
      $cals.each(function () {
        var $c = $jq()(this);
        var sc = $c.data('plugin_simpleCalendar') || $c.data('simpleCalendar');
        if (!sc || !sc.addEvent) return;

        for (var i = 0; i < arr.length; i++) {
          var ev = arr[i];
          if (!ev || !ev.noteId) continue;

          // дедуп по noteId
          if (loadedNoteIds[ev.noteId]) continue;
          loadedNoteIds[ev.noteId] = true;

          sc.addEvent(ev);
        }
      });
    });
  };

  connector.removeCalendarEvent = function (noteId, iso) {
    noteId = String(noteId || '');
    if (loadedNoteIds[noteId]) delete loadedNoteIds[noteId];

    whenCalendarReady(function ($cals) {
      $cals.each(function () {
        var $c = $jq()(this);
        var sc = $c.data('plugin_simpleCalendar') || $c.data('simpleCalendar');
        if (!sc) return;

        if (sc.settings && Array.isArray(sc.settings.events)) {
          sc.settings.events = sc.settings.events.filter(function (ev) {
            return String(ev.noteId || '') !== noteId;
          });
        }

        // если сейчас открыта эта дата — просто перерисуем список БЕЗ клика
        if (iso && String($c.data('selectedIso') || '') === String(iso) && typeof sc.getDateEvents === 'function') {
          renderWrapperWithEvents($c, iso, sc.getDateEvents(isoToDate(iso)));
        }

        if (iso) updateHasEventMark($c, sc, iso);
      });
    });
  };

  // ✅ Главное: после добавления НЕ делаем trigger('click'), а обновляем “окошко” без анимации
  connector.applyEventsAndRefresh = function (eventsJson, iso) {
    iso = String(iso || '');

    runAfterModalClosed(function () {
      connector.applyCalendarEvents(eventsJson);

      whenCalendarReady(function ($cals) {
        $cals.each(function () {
          var $c = $jq()(this);
          var sc = $c.data('plugin_simpleCalendar') || $c.data('simpleCalendar');
          if (!sc) return;

          // если окно по этой дате открыто — перерисуем его без клика
          if (iso && String($c.data('selectedIso') || '') === iso && typeof sc.getDateEvents === 'function') {
            renderWrapperWithEvents($c, iso, sc.getDateEvents(isoToDate(iso)));
          }

          if (iso) updateHasEventMark($c, sc, iso);
        });
      });
    });
  };

  // ===== GRID =====

  function placeWidget(selectorInsideWidget, pos) {
    if (!grid || !$grid || !$grid.length) return;

    var el = $grid[0].querySelector(selectorInsideWidget);
    if (!el) return;

    var item = el.closest('.grid-stack-item');
    if (!item) return;

    grid.makeWidget(item);
    grid.update(item, pos);
  }

  function clampAll() {
    if (!grid || !grid.engine || !grid.engine.nodes) return;
    var nodes = grid.engine.nodes;

    for (var i = 0; i < nodes.length; i++) {
      var n = nodes[i];
      if (!n || !n.el) continue;
      if (n.y + n.h > MAX_ROW) {
        var y = Math.max(0, MAX_ROW - n.h);
        grid.update(n.el, { y: y });
      }
    }
  }

  function bindHandlers() {
    if (!$grid || !$grid.length) return;

    // блокируем drag при модалке (capture)
    var gridElem = $grid[0];
    if (gridElem._gsModalBlockHandler) {
      gridElem.removeEventListener('mousedown', gridElem._gsModalBlockHandler, true);
    }
gridElem._gsModalBlockHandler = function (e) {
  if (document.body.classList.contains('gs-modal-open')) {
    e.stopPropagation();
    e.preventDefault();
  }
};

    gridElem.addEventListener('mousedown', gridElem._gsModalBlockHandler, true);

    // клики по vaadin-кнопкам не должны запускать drag
    $grid.off('.gsButtons');
    $grid.on('mousedown.gsButtons touchstart.gsButtons', '.grid-stack-item-content .v-button', function (e) {
      e.stopPropagation();
    });

    // clamp после drag/resize
    $grid.off('.gsClamp');
    $grid.on('dragstop.gsClamp resizestop.gsClamp', function () {
      clampAll();
      updateClock();
    });
  }

  function ensureGrid(force) {
    force = !!force;

    if (!gridDepsReady()) return setTimeout(function () { ensureGrid(force); }, 200);
    installModalObserverOnce();


    var $ = $jq();
    $grid = $('.grid-stack');
    if (!$grid.length) return setTimeout(function () { ensureGrid(force); }, 200);

    // ✅ если уже поднято на этом DOM — не трогаем
    if (grid && gridHostEl === $grid[0]) {
      clampAll();
      initClockOnce();
      ensureCalendar();
      updateClock();
      return;
    }

    // ✅ если gridstack уже есть в data — подхватываем
    var existing = $grid.data('_gsInstance');
    if (existing) {
      grid = existing;
      gridHostEl = $grid[0];
      bindHandlers();
      initClockOnce();
      ensureCalendar();
      updateClock();
      return;
    }

    // первый запуск
    gridHostEl = $grid[0];

    grid = GridStack.init({
      column: 12,
      cellHeight: 80,
      margin: 5,
      float: true,
      staticGrid: false,
      maxRow: MAX_ROW,
      draggable: { handle: '.widget-drag-handle' }
    }, $grid[0]);

    $grid.data('_gsInstance', grid);

    $('.grid-stack-item', $grid).each(function (idx, el) {
      grid.makeWidget(el);
    });

    grid.batchUpdate();

    placeWidget('.clock-widget-container', { x: 0, y: 0, w: 2, h: 1 });
    placeWidget('.c-table-composition-has-top-panel', { x: 3, y: 0, w: 3, h: 5 });
    placeWidget('.calendar-widget-container', { x: 6, y: 0, w: 3, h: 6 });
    placeWidget('.news-widget-header', { x: 0, y: 1, w: 3, h: 4 });

    grid.batchUpdate(false);

    bindHandlers();
    initClockOnce();
    ensureCalendar();
    updateClock();
  }

  // ===== CALENDAR INIT =====

  function ensureCalendar() {
    var $ = $jq();
    if (!calDepsReady()) return setTimeout(ensureCalendar, 200);

    var $cals = inGrid('.calendar-widget-container');
    if (!$cals || !$cals.length) return;

    $cals.each(function () {
      var $c = $(this);
      if ($c.data('scInit')) return;

      $c.data('scInit', true);
      $c.empty();

      $c.simpleCalendar({
        months: ['январь','февраль','март','апрель','май','июнь','июль','август','сентябрь','октябрь','ноябрь','декабрь'],
        days: ['вс','пн','вт','ср','чт','пт','сб'],
        displayYear: true,
        fixedStartDay: true,

        displayEvent: true,
        disableEventDetails: false,
        disableEmptyDetails: false,

        events: [],

        onInit: function () {
          var now = new Date();
          if (connector.requestNotesForMonth) {
            connector.requestNotesForMonth(now.getFullYear(), now.getMonth() + 1);
          }
        },

        onMonthChange: function (month, year) {
          if (connector.requestNotesForMonth) {
            connector.requestNotesForMonth(year, normMonth(month));
          }
        },

        onEventSelect: function () {
          var ev = $(this).data('event');
          if (ev && ev.noteId && connector.openNote) connector.openNote(String(ev.noteId));
        },

        onDateSelect: function (date, events) {
          var d = (date instanceof Date) ? date : new Date(date);
          var iso = toIso(d);

          $c.data('selectedIso', iso);

          // Рисуем “окошко” как ты и хотел, это и есть “анимация” плагина (его внутренняя)
          renderWrapperWithEvents($c, iso, events || []);
        }
      });
    });
  }

  // ===== CUBA lifecycle =====
  connector.initGrid = function () {
    ensureGrid(true);
  };




  this.onStateChange = function () {
    if (!grid) ensureGrid(false);
  };

  this.onUnregister = function () {
    if (clockTimerId) {
      clearInterval(clockTimerId);
      clockTimerId = null;
    }

    if ($grid && $grid.length) {
      $grid.off('.gsButtons').off('.gsClamp');
      var gridElem = $grid[0];
      if (gridElem && gridElem._gsModalBlockHandler) {
        gridElem.removeEventListener('mousedown', gridElem._gsModalBlockHandler, true);
      }
    }

    grid = null;
    $grid = null;
    gridHostEl = null;
    if (_modalObs) { _modalObs.disconnect(); _modalObs = null; }
    if (_modalOffTimer) { clearTimeout(_modalOffTimer); _modalOffTimer = null; }
    document.body.classList.remove('gs-modal-open');

  };
};
