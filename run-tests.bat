@echo off

call gradlew clean test
call gradlew allureReport

call gradlew allureServe