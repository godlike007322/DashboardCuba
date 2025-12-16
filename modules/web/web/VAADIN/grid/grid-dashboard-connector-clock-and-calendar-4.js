com_company_project_web_ui_components_jscomponent_GridDashboard = function () {
    var connector = this;
    var element = connector.getElement();

    element.innerHTML = '<div class="grid-stack"></div>';
    var gridInitialized = false;

    function initGrid() {
        if (gridInitialized) return;

        var $grid = $('.grid-stack', element);
        if ($grid.length === 0 || typeof GridStack === 'undefined' || typeof $.fn.simpleCalendar === 'undefined') {
            setTimeout(initGrid, 200);
            return;
        }

        // === 1. СОЗДАЁМ ПЛИТКИ ===

        // 1) Плитка с календарём
        var calItem = document.createElement('div');
        calItem.className = 'grid-stack-item';
        calItem.setAttribute('gs-x', 0);
        calItem.setAttribute('gs-y', 0);
        calItem.setAttribute('gs-w', 10);
        calItem.setAttribute('gs-h', 6);

        var calContent = document.createElement('div');
        calContent.className = 'grid-stack-item-content calendar-widget-container';
        calItem.appendChild(calContent);
        $grid[0].appendChild(calItem);

        // 2) Плитка “другой виджет”
        var infoItem = document.createElement('div');
        infoItem.className = 'grid-stack-item';
        infoItem.setAttribute('gs-x', 3);
        infoItem.setAttribute('gs-y', 0);
        infoItem.setAttribute('gs-w', 2);
        infoItem.setAttribute('gs-h', 2);

   // 3) Плитка с заметками
        var notesItem = document.createElement('div');
        notesItem.className = 'grid-stack-item';
        notesItem.setAttribute('gs-x', 2);
        notesItem.setAttribute('gs-y', 3);
        notesItem.setAttribute('gs-w', 3);
        notesItem.setAttribute('gs-h', 3);

        var notesContent = document.createElement('div');
        notesContent.className = 'grid-stack-item-content notes-widget-container';

        // Внутренний HTML виджета заметок
        notesContent.innerHTML =
            '<div class="notes-widget">' +
              '<div class="notes-header">Заметки</div>' +
              '<div class="notes-list"></div>' +
              '<div class="notes-input">' +
                '<textarea class="notes-text" rows="3" placeholder="Введите заметку..."></textarea>' +
                '<button class="notes-add-btn">Добавить</button>' +
              '</div>' +
            '</div>';

        notesItem.appendChild(notesContent);
        $grid[0].appendChild(notesItem);



        var infoContent = document.createElement('div');
        infoContent.className = 'grid-stack-item-content';
        infoContent.style.border = '1px solid #ccc';
        infoContent.style.background = '#fff';
        infoContent.style.display = 'flex';
        infoContent.style.alignItems = 'center';
        infoContent.style.justifyContent = 'center';
        infoContent.innerText = 'Другой виджет';
        infoItem.appendChild(infoContent);
        $grid[0].appendChild(infoItem);

        // 3) Плитка с часами
        var clockItem = document.createElement('div');
        clockItem.className = 'grid-stack-item';
        clockItem.setAttribute('gs-x', 0);
        clockItem.setAttribute('gs-y', 3);
        clockItem.setAttribute('gs-w', 2);
        clockItem.setAttribute('gs-h', 1);

        var clockContent = document.createElement('div');
        clockContent.className = 'grid-stack-item-content clock-widget-container';
        clockItem.appendChild(clockContent);
        $grid[0].appendChild(clockItem);





        // === 2. ИНИЦИАЛИЗИРУЕМ GRIDSTACK ===

        var grid = GridStack.init({
            cellHeight: 80,
            margin: 5,
            float: true
        }, $grid[0]);


        // === 5. ЛОГИКА ВИДЖЕТА ЗАМЕТОК ===

        var notes = []; // пока просто в памяти, потом можно будет грузить/сохранять через сервер

        function renderNotes() {
            var $list = $('.notes-widget-container .notes-list', element);
            $list.empty();

            if (notes.length === 0) {
                $list.append('<div class="notes-empty">Нет заметок</div>');
                return;
            }

            notes.forEach(function (n, idx) {
                var $row = $('<div class="notes-row"></div>');
                var $text = $('<div class="notes-text-view"></div>').text(n.text);
                var $del = $('<button class="notes-del-btn" type="button">×</button>');

                $del.on('click', function () {
                    notes.splice(idx, 1);
                    renderNotes();
                    sendNotesToServer();
                });

                $row.append($text).append($del);
                $list.append($row);
            });
        }

        function sendNotesToServer() {
            if (connector.notesChanged) {
                // передадим JSON массив заметок на сервер
                connector.notesChanged(JSON.stringify(notes));
            }
        }

        // обработчик кнопки "Добавить"
        var $notesContainer = $('.notes-widget-container', element);
        var $addBtn = $('.notes-add-btn', $notesContainer);
        var $textarea = $('.notes-text', $notesContainer);

        $addBtn.on('click', function () {
            var text = $textarea.val().trim();
            if (!text) {
                return;
            }

            notes.push({
                id: Date.now(),     // временный id, потом можно заменить на id сущности
                text: text
            });

            $textarea.val('');
            renderNotes();
            sendNotesToServer();
        });

        renderNotes();

        // === 3. КАЛЕНДАРЬ ВНУТРИ ПЕРВОЙ ПЛИТКИ ===

        var $calContainer = $('.calendar-widget-container', element);
        $calContainer.simpleCalendar({
            months: ['январь','февраль','март','апрель','май','июнь','июль','август','сентябрь','октябрь','ноябрь','декабрь'],
            days: ['воскресенье','понедельник','вторник','среда','четверг','пятница','суббота'],
            displayYear: true,
            fixedStartDay: true,
            displayEvent: true,
            disableEventDetails: false,
            disableEmptyDetails: false,
            events: [],
            onDateSelect: function (date, events) {
                console.log('Выбрана дата в дашборд-календаре:', date);
            }
        });

        // === 4. ЧАСЫ ВНУТРИ СВОЕЙ ПЛИТКИ ===

        function pad(n) { return n < 10 ? '0' + n : '' + n; }

       function updateClock() {
           var now = new Date();
           var h = pad(now.getHours());
           var m = pad(now.getMinutes());
           var s = pad(now.getSeconds());
           var text = h + ':' + m + ':' + s;

           var $clock = $('.clock-widget-container', element);
           $clock.text(text);

           var height = $clock.height();
           var width = $clock.width();

           if (height && width) {
               // Оцениваем максимальный размер шрифта по высоте и по ширине
               var sizeByHeight = height * 0.6;              // чтобы было место сверху/снизу
               var charCount = text.length;                  // обычно 8 символов "HH:MM:SS"
               var sizeByWidth = width / (charCount * 0.7);  // грубая оценка ширины

               var fontSize = Math.min(sizeByHeight, sizeByWidth);
               fontSize = Math.max(14, fontSize);           // не меньше 14px

               $clock.css('font-size', fontSize + 'px');
               $clock.css('line-height', height + 'px');    // чтобы по вертикали по центру
           }
       }


        updateClock();
        setInterval(updateClock, 1000);


        // === 5. ИЗМЕНЕНИЯ ЛЕЙАУТА ===

$grid.on('change', function (event, items) {
    // твой существующий код с layoutChanged, оставляем
    if (items) {
        // после изменения размеров пересчитаем шрифт
        updateClock();
    }

    if (connector.layoutChanged && items) {
        var layout = items.map(function (item) {
            return {
                id: item.el.getAttribute('id'),
                x: item.x,
                y: item.y,
                w: item.width,
                h: item.height
            };
        });
        connector.layoutChanged(JSON.stringify(layout));
    }
});

        gridInitialized = true;
    }

    connector.initGrid = function () {
        initGrid();
    };

    this.onStateChange = function () {
        initGrid();
    };
};
