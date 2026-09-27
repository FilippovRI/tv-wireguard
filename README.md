# TV WireGuard

Android TV клиент [WireGuard](https://www.wireguard.com/): весь трафик телевизора через домашний Keenetic, UI под пульт, автозапуск при включении ТВ и кнопка «YouTube через VPN».

## Скачать APK

Готовый APK — на странице [Releases](https://github.com/FilippovRI/tv-wireguard/releases):

1. Откройте последний релиз
2. Скачайте `tv-wireguard-*.apk`
3. Установите на Android TV:

```bat
adb connect TV_IP:5555
adb install -r tv-wireguard-1.0.0.apk
```

APK debug-подписан (для sideload). Для магазина приложений нужна отдельная release-подпись.

## Возможности

- Full tunnel WireGuard (`AllowedIPs = 0.0.0.0/0`)
- Интерфейс под D-pad / пульт Android TV
- Автоподключение после включения ТВ
- Кнопка **YouTube через VPN** (поднять туннель → открыть YouTube TV)
- Поддержка Always-on VPN в настройках системы
- Импорт peer-конфига `.conf`

## Как это работает

Это **не** «открыть IP:порт и Keenetic куда-то перенаправит HTTP».

```
ТВ (другая квартира)
  └─ WireGuard UDP → ВНЕШНИЙ_IP:ПОРТ
        └─ Keenetic (встроенный WG-сервер или UDP-проброс на ПК)
              └─ туннель поднят → трафик ТВ выходит с домашнего IP
```

На Keenetic нужен **WireGuard server** (лучше встроенный в прошивку) либо WG на ПК + UDP port forward.

## Keenetic (сервер)

1. Интернет → Другие подключения → WireGuard → добавить сервер.
2. Создать peer для телевизора, скачать/скопировать клиентский `.conf`.
3. В peer для ТВ:
   - `Endpoint = ваш.ddns.или.ip:порт` (внешний адрес дома)
   - `AllowedIPs = 0.0.0.0/0` (весь трафик)
   - `PersistentKeepalive = 25` (NAT у соседей)
4. Если WG на ПК: в Keenetic пробросьте **UDP** порт на IP ПК.
5. Не блокируйте UDP-порт файрволом / провайдером.

DDNS на Keenetic желателен при плавающем внешнем IP.

Шаблон: [`docs/sample-peer.conf`](docs/sample-peer.conf).

## Первый запуск на ТВ

1. Откройте **TV WireGuard** и разрешите VPN.
2. **Конфиг WireGuard** → вставьте peer `.conf` → Сохранить.
3. Включите «Автозапуск VPN при включении ТВ».
4. (Рекомендуется) Настройки Android TV → Сеть → VPN → Always-on.
5. **YouTube через VPN** — подключение и запуск YouTube TV.

## Сборка из исходников

Нужны JDK 17 и Android SDK.

```bat
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Релизный APK собирается GitHub Actions при теге `v*` или через workflow **Build and Release**.

## Лицензия

Код приложения — для личного/домашнего использования. Библиотека туннеля WireGuard — Apache 2.0.
