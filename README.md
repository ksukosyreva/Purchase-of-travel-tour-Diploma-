[![Java CI with Gradle](https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/actions/workflows/gradle.yml/badge.svg)](https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-/actions/workflows/gradle.yml)

# Дипломный проект по автоматизации тестирования сервиса покупки туров (aqa-shop)
Репозиторий содержит проект автоматизации тестирования веб-сервиса по продаже туров.
Тестирование проводится на двух СУБД: MySQL и PostgreSQL.

### Предварительные требования (Prerequisites)

Перед запуском автотестов на вашем компьютере должны быть установлены:
- Git
- Java JDK 11 (или выше)
- Docker Desktop (с запущенным Docker Engine)

### Процедура запуска (Пошаговая инструкция)

1. **Клонирование репозитория**
Откройте терминал и выполните:

`git clone https://github.com/ksukosyreva/Purchase-of-travel-tour-Diploma-.git`

`cd Purchase-of-travel-tour-Diploma-`

3. **Запуск контейнеров (Базы данных и Эмулятор банка)**

Поднимите контейнеры MySQL, PostgreSQL и эмулятора банковских сервисов (gate-simulator) с помощью Docker Compose:

`docker compose up -d`

*После запуска контейнеров необходимо подождать 15–20 секунд, чтобы базы данных успешно инициализировались и были готовы принимать подключения.*

5. **Запуск тестируемого приложения (SUT)**

Приложение aqa-shop.jar находится в папке artifacts. Запустить его можно в двух конфигурациях (выберите одну):
- Для тестирования на **MySQL** (по умолчанию):
  
`java -jar ./artifacts/aqa-shop.jar`

(В этом случае приложение будет писать данные в MySQL на порт 3306).

- Для тестирования на **PostgreSQL**:
  
`java -jar ./artifacts/aqa-shop.jar --spring.datasource.url=jdbc:postgresql://localhost:5432/app`

(В этом случае приложение будет писать данные в PostgreSQL на порт 5432).

Окно терминала с запущенным приложением (SUT) закрывать нельзя. Оно должно работать в фоне, пока выполняются тесты.

4. **Запуск автотестов**

Откройте новое окно терминала (не закрывая окно с SUT) и запустите тесты, в зависимости от выбранной СУБД.
- *Запуск тестов на MySQL:*
  
`./gradlew clean test`

- *Запуск тестов на PostgreSQL:*
  
`./gradlew clean test "-Ddb.url=jdbc:postgresql://localhost:5432/app"`

## Генерация отчетов Allure

В проекте подключена система отчетности Allure. Чтобы сгенерировать и открыть отчет после прогона тестов, выполните команду:

`./gradlew allureServe`

## Скриншот отчета Allure
<img width="1920" height="894" alt="image" src="https://github.com/user-attachments/assets/ef62ec3c-2db0-494e-8e24-42190900cc75" />


## Особенности реализации и тестирования

**Поддержка двух СУБД**:
Класс SQLHelper считывает URL базы данных из системных свойств. По умолчанию используется MySQL. Для переключения на PostgreSQL передается параметр -Ddb.url.

**Кодировка**: В build.gradle и gradle.properties принудительно задана кодировка UTF-8. Это сделано во избежание появления некорректной кодировки (нечитаемых символов) в названиях тестов и логах при запуске через терминал на Windows.

**Headless-режим**: В CI (GitHub Actions) браузер запускается без графического интерфейса. Локально тесты запускаются с открытием окна браузера.

**CI/CD**: Настроен пайплайн в GitHub Actions (.github/workflows/gradle.yml), который автоматически поднимает окружение и прогоняет тесты при каждом пуше в ветку main.
