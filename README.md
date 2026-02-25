<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="128" height="128" />
  <h1>My Schedule</h1>
  <p>
    <b>Умный планировщик для сложных учебных графиков.</b><br>
    Android • Wear OS • Widgets
  </p>
  
  <a href="https://github.com/l-Mel-l/MySchedule/releases/tag/v1.2">
    <img src="https://img.shields.io/badge/Скачать-APK-black?style=for-the-badge&logo=android" height="40" />
  </a>
</div>

---

## О проекте

**My Schedule** решает проблему нестандартных расписаний, с которой не справляются обычные календари. 
Приложение спроектировано так, чтобы быть максимально быстрым: запуск за доли секунды, работа без интернета и мгновенный доступ к информации через виджеты и смарт-часы.

### Основные возможности

| Гибкость | Экосистема | Удобство |
| :--- | :--- | :--- |
| Поддержка **различного количества недель** с авто-переключением. | Полноценная версия для **Wear OS** с поддержкой Tiles. | **Виджеты** на рабочий стол с актуальным статусом. |
| Режим **"Семестр"** для уникального расписания на каждую неделю. | Синхронизация данных между телефоном и часами без облака. | **Импорт/Экспорт** расписания файлом (JSON). |

---

## Обзор интерфейса

<table>
  <tr>
    <th width="33%">Главный экран</th>
    <th width="33%">Управление неделями</th>
    <th width="33%">Редактирование</th>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/3a82ba42-7a0a-4932-9d72-4140ff376b9a" width="100%" alt="Timer Animation"/></td>
    <td><img src="https://github.com/user-attachments/assets/c2c4ad76-1544-42e0-b7f8-d142ffa2debb" width="100%" alt="Semester Mode"/></td>
    <td><img src="https://github.com/user-attachments/assets/bfcefb5b-edbf-45eb-9779-d89fe9f5e6c7" width="100%" alt="Edit Lesson"/></td>
  </tr>
  <tr>
    <td align="center"><i>Визуальный таймер и статус</i></td>
    <td align="center"><i>Поддержка 20+ недель</i></td>
    <td align="center"><i>Цветовые метки и заметки</i></td>
  </tr>
</table>

### 🌗 Адаптивный дизайн (Dark & Light)

Приложение автоматически подстраивается под системную тему, сохраняя читаемость и фирменный стиль.

<table>
  <tr>
    <th width="33%">Главная (Light)</th>
    <th width="33%">Список (Light)</th>
    <th width="33%">Настройки (Light)</th>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/94153f00-47e7-425f-a1f6-2abf2b9ed7a5" width="100%" alt="Light Theme Home"/></td>
    <td><img src="https://github.com/user-attachments/assets/b3e9d3d2-3111-48fa-aa5b-9cc95c6bf242" width="100%" alt="Light Theme List"/></td>
    <td><img src="https://github.com/user-attachments/assets/83cceb61-f24a-475b-b62f-112f6a7ca9d3" width="100%" alt="Light Theme Settings"/></td>
  </tr>
</table>

### Wear OS

Приложение адаптировано для экосистемы Android. Вы можете узнать аудиторию, просто взглянув на часы или рабочий стол.

<table>
  <tr>
    <td width="50%" align="center">
      <img src="https://github.com/user-attachments/assets/3e9b4af8-e8da-4c71-8d5c-1cfc387b21c1" width="100%" alt="Phone Widgets"/>
      <br><b>Приложение Wear OS</b>
    </td>
    <td width="50%" align="center">
      <img src="https://github.com/user-attachments/assets/30316e46-b93d-48e3-acc5-11304893eca4" width="100%" alt="Wear OS Tile"/>
      <br><b>Плитка Wear OS с таймером</b>
    </td>
  </tr>
</table>

---

## Техническая реализация

Проект построен на современном стеке Android разработки, с упором на чистую архитектуру и производительность.

*   **UI:** Jetpack Compose, Material Design 3.
*   **Wear OS:** Wear Compose, Horologist, ProtoLayout (Tiles).
*   **Architecture:** Single Activity, MVVM, Unidirectional Data Flow.
*   **Data Layer:** Kotlin Serialization (JSON), FileProvider для шаринга.
*   **Integration:** Jetpack Glance (AppWidgets), Wearable Data Layer API.

---

## Установка

1.  Перейдите в раздел [Releases](https://github.com/l-Mel-l/MySchedule/releases/tag/v1.2).
2.  Скачайте файл `MySchedule_Phone_v1.1` для смартфона.
3.  *(Опционально)* Скачайте `MySchedule_Watch_v1.1` для установки на часы через ADB или Bugjaeger.

---

<div align="center">
  
  <sub>Разработано с вниманием к деталям. 2026.</sub>

</div>

*Created for fun by [Mel](https://github.com/l-Mel-l)*
