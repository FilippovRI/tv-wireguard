# TV WireGuard

Android TV клиент WireGuard: full tunnel через домашний Keenetic, UI под пульт, автозапуск при включении ТВ и кнопка «YouTube через VPN».

## Как это работает (важно)

Это **не** «открыть IP:порт и Keenetic куда-то перенаправит HTTP».

Схема такая:

```
ТВ (другая квартира)
  └─ WireGuard UDP → ВНЕШНИЙ_IP:ПОРТ
        └─ Keenetic (проброс UDP или встроенный WG-сервер)
              └─ туннель поднят → весь трафик ТВ выходит с домашнего IP
```

На Keenetic нужен **WireGuard server** (встроенный в прошивку — лучший вариант) либо WG на ПК + UDP port forward на этот ПК.

## Keenetic (сервер)

1. Интернет → Другие подключения → WireWireGuard / WireGuard → добавить сервер.
2. Создать peer для телевизора, скачать/скопировать клиентский `.conf`.
3. В peer для ТВ:
   - `Endpoint = ваш.ddns.или.ip:порт` (внешний адрес дома)
   - `AllowedIPs = 0.0.0.0/0` (весь трафик)
   - `PersistentKeepalive = 25` (NAT у соседей)
4. Если WG крутится на ПК, а не на Keenetic: в Keenetic пробросьте **UDP** порт на IP ПК.
5. Откройте UDP-порт на внешнем интерфейсе / не блокируйте его файрволом провайдера.

DDNS на Keenetic желателен, если внешний IP домашний плавающий.

## Сборка APK

Нужны Android Studio (Ladybug+) и JDK 17.

1. Откройте папку `tv-wireguard` в Android Studio.
2. Дождитесь Gradle Sync.
3. Build → Build Bundle(s) / APK(s) → Build APK(s).
4. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Установка на ТВ:

```bat
adb connect TV_IP:5555
adb install -r app-debug.apk
```

## Первый запуск на ТВ

1. Откройте **TV WireGuard**.
2. Разрешите VPN.
3. **Конфиг WireGuard** → вставьте peer `.conf` → Сохранить.
4. Включите «Автозапуск VPN при включении ТВ».
5. (Рекомендуется) Настройки Android TV → Сеть → VPN → Always-on для этого приложения.
6. Кнопка **YouTube через VPN**: поднимает туннель и открывает YouTube TV.

Вставку конфига удобнее сделать так: скопировать текст на телефон/ПК с клавиатурой, либо временно вставить через `adb`:

```bat
adb shell
run-as com.tvwireguard.app
```

Либо просто вставить пультом/клавиатурой в поле конфига.

## Шаблон конфига

См. `docs/sample-peer.conf`.
