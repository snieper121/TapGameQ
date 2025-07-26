package com.example.tapgame.server;

interface IMyPermissionServer {
    // Проверяет, было ли разрешение сохранено (сопряжено).
    // Это персистентное состояние, которое должно сохраняться после отключения Wi-Fi отладки.
    boolean isPermissionSaved();

    // Проверяет, активно ли ADB-соединение в данный момент.
    // Это временное состояние, которое будет false, если Wi-Fi отладка отключена.
    boolean isPermissionActive();

    // Устанавливает статус сохранения разрешения.
    // Используется MyPermissionServer для записи статуса в SettingsDataStore.
    void setPermissionSaved(boolean saved);

    // Добавление метода для проверки статуса Shizuku
    boolean isShizukuActive();

    // Добавление метода для запроса разрешения Shizuku
    void requestShizukuPermission();
}


