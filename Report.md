# Отчет о проведенном тестировании

## Краткое описание

В рамках дипломного проекта проводилось тестирование сервиса по покупке туров (aqa-shop.jar), взаимодействующего с СУБД и эмулятором банковских сервисов (gate-simulator).

*Что тестировалось*:

- **UI-сценарии**: Покупка тура дебетовой картой и в кредит (позитивные и негативные сценарии, валидация полей формы).

- **Интеграция с БД**: Проверка корректности сохранения статусов платежей и способов оплаты на двух СУБД (MySQL и PostgreSQL).

- **API-сценарии**: Проверка ответов эмулятора банка (Gate Simulator) на запросы с APPROVED и DECLINED картами для уточнения причины бага маршрутизации, при котором кредитный платеж записывается в дебетовую таблицу.

## Инструменты:

- *Автоматизация*: Java 11, Selenide (UI), REST-assured (API), JDBC (БД).

- *Сборка и отчетность*: Gradle, Allure.

- *Окружение*: Docker (MySQL, PostgreSQL, gate-simulator), GitHub Actions (CI).

## Статистика тестирования
- Всего тест-кейсов: 52

- Успешных тест-кейсов: 43 (82.69%)

- Неуспешных тест-кейсов: 9 (17.31%)

С подробной статистикой, графиками и списком упавших тестов можно ознакомиться в сгенерированном Allure-отчете (папка build/reports/allure-report).
![allure report](https://private-user-images.githubusercontent.com/273953380/694901414-ef62ec3c-2db0-494e-8e24-42190900cc75.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3OTQ0MDcxNTcsIm5iZiI6MTc5NDQwNjg1NywicGF0aCI6Ii8yNzM5NTMzODAvNjk0OTAxNDE0LWVmNjJlYzNjLTJkYjAtNDk0ZS04ZTI0LTQyMTkwOTAwY2M3NS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjYxMTExJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MTExMVQxNDIwNTdaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT03YjgwNWIwZGY4MzIxOTBkOWRjMWJjMWI5YmJmMWNkNmIyYTY3YjU0ZGQ1YmEwMjgyYjU0OTQyYWEzOTM4YTljJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZyZXNwb25zZS1jb250ZW50LXR5cGU9aW1hZ2UlMkZwbmcifQ.ChvejoCuswmImkQVgCSVMUWz8LKmPCPRafj_wr4BgzA)


## Найденные дефекты

Все 9 упавших тестов связаны с реальными дефектами тестируемого приложения (SUT). Подробное описание, шаги воспроизведения и скриншоты задокументированы в разделе Issues данного репозитория. Всего в ходе тестирования было выявлено и задокументировано 10 дефектов:

**Ошибки валидации поля «Владелец»**:

- Форма принимает имя на кириллице и отправляет в БД (Issue #7).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/7

- Поле принимает цифры и отправляет данные в БД (Issue #8).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/8

- Поле принимает спецсимволы и отправляет данные в БД (Issue #10).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/10

- Отсутствует ограничение максимальной длины поля «Владелец» (Issue #9).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/9

**Ошибки валидации срока действия карты**:

- Форма принимает невалидное значение 00 в поле «Месяц» и отправляет данные в БД (Issue #4).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/4

- Несогласованное отображение ошибки валидации при вводе карты с истекшим сроком действия (ошибка "прыгает" по полям и меняет текст) (Issue #6).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/6

**Ошибки бизнес-логики и маршрутизации (Backend)**:

- При покупке тура в кредит картой со статусом APPROVED, приложение записывает способ оплаты в БД в дебетовой таблице (Issue #1).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/1

- При покупке тура в кредит картой со статусом DECLINED, приложение также записывает способ оплаты в дебетовую таблицу (Issue #5).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/5

**Ошибки отображения статусов (UI)**:

- Ложное сообщение об успехе при оплате дебетовым способом отклоненной картой (DECLINED) (Issue #3).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/3

- Ложное сообщение об успехе при оплате в кредит отклоненной картой (DECLINED) (Issue #2).
  https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/issues/2

## Общие рекомендации
По итогам тестирования разработчикам рекомендуется обратить внимание на следующие области:

**Валидация полей (Frontend/Backend)**:

- Внедрить строгую валидацию поля «Владелец». Также добавить ограничения длины.

- Добавить проверки на невалидные значения в поля «Месяц» и «Год» (например, отсекать значения < 01 и > 12 для месяца).

**Стабильность UI (UX)**:

- Устранить непостоянство расположения и текста ошибки при вводе просроченной карты. Сообщение должно быть единым и отображаться строго под конкретным полем.

- Синхронизировать время ответа БД и отображения сообщения об успехе (добавить спиннер или блокировку кнопки «Продолжить» до фактического завершения транзакции). Это устранит «flakiness» (нестабильность) автотестов.

**Логика работы (Backend)**:

- Исправить баг маршрутизации в БД (поле payment_id заполняется при кредитной заявке).

- Убрать показ сообщения об успехе, если банк вернул отказ (статус DECLINED).

**Безопасность**:

- Внедрить очистку входящих данных для предотвращения потенциальных XSS-атак и SQL-инъекций.