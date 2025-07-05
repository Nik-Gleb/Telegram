### Дерево вложенности View-компонентов **ProfileActivity**

```
NestedFrameLayout  fragmentView / contentView                      ← корневой контейнер :contentReference[oaicite:0]{index=0}
├─ RecyclerListView           listView                              :contentReference[oaicite:1]{index=1}
├─ ActionBar                  actionBar                             :contentReference[oaicite:2]{index=2}
├─ FrameLayout                avatarContainer2                      :contentReference[oaicite:3]{index=3}
│  ├─ FrameLayout             avatarContainer                       :contentReference[oaicite:4]{index=4}
│  │  ├─ AvatarImageView      avatarImage                            :contentReference[oaicite:5]{index=5}
│  │  └─ RadialProgressView   avatarProgressView                    :contentReference[oaicite:6]{index=6}
│  ├─ ProfileGalleryView      avatarsViewPager                      :contentReference[oaicite:7]{index=7}
│  ├─ OverlaysView            overlaysView                          :contentReference[oaicite:8]{index=8}
│  ├─ PagerIndicatorView      avatarsViewPagerIndicatorView         :contentReference[oaicite:9]{index=9}
│  ├─ SimpleTextView[]        nameTextView (0,1)                    :contentReference[oaicite:10]{index=10}
│  ├─ SimpleTextView[]        onlineTextView (0-3)                  :contentReference[oaicite:11]{index=11}
│  ├─ AnimatedStatusView      animatedStatusView                    :contentReference[oaicite:12]{index=12}
│  ├─ ProfileStoriesView      storyView                             :contentReference[oaicite:13]{index=13}
│  ├─ ProfileGiftsView        giftsView                             :contentReference[oaicite:14]{index=14}
│  └─ ClippingTextViewSwitcher mediaCounterTextView                 :contentReference[oaicite:15]{index=15}
├─ ImageView                  timeItem                              :contentReference[oaicite:16]{index=16}
├─ ImageView                  starBgItem                            :contentReference[oaicite:17]{index=17}
├─ ImageView                  starFgItem                            :contentReference[oaicite:18]{index=18}
├─ RLottieImageView           writeButton                           :contentReference[oaicite:19]{index=19}
├─ TopView                    topView                               :contentReference[oaicite:20]{index=20}
├─ SharedMediaLayout          sharedMediaLayout (элемент списка)    :contentReference[oaicite:21]{index=21}
├─ View                       blurredView (скрим)                   :contentReference[oaicite:22]{index=22}
├─ FrameLayout                bottomButtonsContainer [опц.]         :contentReference[oaicite:23]{index=23}
│  ├─ FrameLayout  bottomButtonContainer[0] → shadow + Button       :contentReference[oaicite:24]{index=24}
│  └─ FrameLayout  bottomButtonContainer[1] → shadow + Button       :contentReference[oaicite:25]{index=25}
├─ FrameLayout                floatingButtonContainer (опц.)        :contentReference[oaicite:26]{index=26}
│  └─ RLottieImageView        floatingButton
└─ FrameLayout                frameLayout1 (бан-индикатор, опц.)    :contentReference[oaicite:27]{index=27}
```

#### Что важно знать

* **avatarContainer2** — центральный слой поверх `ActionBar` и `listView`; именно здесь размещаются аватар, имя, статус, истории и подарки.
* **listView** содержит овальные ячейки профиля *и* специальную ячейку-контейнер `SharedMediaLayout`, которая открывает вкладки “Медиа”, “Группы” и т.д. (добавляется адаптером, а не вручную).
* **bottomButtonsContainer** и **floatingButtonContainer** добавляются только в своём профиле и/или при наличии права публиковать сторис.
* Дополнительные оверлеи (**blurredView**, индикаторы звезды/таймера, скрим) отрисовываются поверх всех слоёв, когда нужны эффекты затемнения или анимации.

Таким образом, экран строится вокруг одного корневого `NestedFrameLayout`, внутри которого есть две “оси”:

1. **Содержимое** — `RecyclerListView` + `SharedMediaLayout`.
2. **Плавающая шапка** — `avatarContainer2` со всеми элементами профиля, размещённый поверх списка и синхронизируемый при скролле.

Диаграмма выше отражает полный порядок вложенности, включая условные блоки, и поможет быстро ориентироваться в коде при дальнейшем рефакторинге или отладке.

flowchart TD
A[needLayout] --> B{avatarContainer?}
B -- no --> Z[Только обновление<br/>listView.topMargin]
B -- yes --> C[diff = extraHeight / 88dp]
C --> D[glow & overscroll]
C --> W
W[writeButtonVisible?] -->|да| E[Показ/анимация<br/>writeButton, QR]
W -->|нет| F[Скрытие<br/>writeButton, QR]
E & F --> G[story/gifts coords]
G --> H[avatarX,Y<br/>name/online scale]
H --> I{h > 88dp<br/>или PulledDown}
I -- нет --> L[Сжатый заголовок]
I -- да --> J[expandProgress, avatarScale]
J --> K{allowPullingDown &&<br/>}
K -- да --> PD[Включить isPulledDown]
K -- нет --> L
L --> M[needLayoutText]
M --> N[overlays size<br/>emoji pos]
N --> Z

---

Этот Java-код создаёт и настраивает пользовательский интерфейс профиля в приложении Telegram для Android. Рассмотрим основные моменты:

1. **Создание кастомного View (`NestedFrameLayout`)**:
  - Создаётся анонимный подкласс `NestedFrameLayout`, который переопределяет методы для управления событиями касаний, измерения и отрисовки элементов интерфейса.

2. Метод `dispatchTouchEvent(MotionEvent ev)`:
- Перехватывает события касаний.
- Если активен режим масштабирования (`pinchToZoomHelper.isInOverlayMode()`), передаёт события в обработчик масштабирования.
- Если идёт быстрая прокрутка или жест масштабирования в `sharedMediaLayout`, события передаются туда.
- Иначе события обрабатываются стандартным образом.

3. Метод `onMeasure(int widthMeasureSpec, int heightMeasureSpec)`:
- Измеряет размеры и устанавливает отступы для элементов интерфейса (`listView`, `searchListView`).
- Вычисляет высоту контента списка (`listContentHeight`), измеряя каждый элемент списка.
- Устанавливает отступы и прокрутку списка в зависимости от состояния интерфейса (например, открыта ли анимация профиля, ориентация экрана).

3. Метод `onLayout(boolean changed, int left, int top, int right, int bottom)`:
- Вызывается при изменении размеров или позиции элементов.
- Сбрасывает сохранённую позицию прокрутки и проверяет необходимость обновления прокрутки.

4. Метод `dispatchDraw(Canvas canvas)`:
- Отвечает за отрисовку фона и элементов списка.
- Сортирует дочерние элементы списка по вертикальной позиции.
- Рисует фоновые прямоугольники (`whitePaint`, `grayPaint`) между элементами списка в зависимости от их состояния и прозрачности.
- Также обрабатывает специальные состояния, такие как анимации переходов и размытие (`blurredView`).

4. Метод `drawChild(Canvas canvas, View child, long drawingTime)`:
- Пропускает отрисовку некоторых элементов (например, при активном режиме масштабирования или для размытого вида).

4. Методы `onAttachedToWindow()` и `onDetachedFromWindow()`:
- Управляют жизненным циклом элементов интерфейса, таких как эмодзи-статусы и значки верификации ботов, прикрепляя и открепляя их при добавлении и удалении из окна.

В целом, этот код отвечает за сложную логику отображения и взаимодействия с профилем пользователя, включая обработку жестов, анимаций, прокрутки и отрисовки элементов интерфейса в приложении Telegram.

(float) Math.floor(avatarY) + AndroidUtilities.dp(1.3f) + AndroidUtilities.dp(7f) + avatarContainer.getMeasuredHeight() * (avatarScale - (42f + 18f) / 42f) / 2f;
(float) Math.floor(avatarY) + AndroidUtilities.dp(1.3f) + AndroidUtilities.dp(7f) * diff
newTop + extraHeight - AndroidUtilities.dpf2(38f) - nameTextView[1].getBottom();


