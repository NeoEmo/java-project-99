create-all-txt-java:
	find . -type f -name "*.java" -exec cat {} + > all_code.txt

setup:
	./gradlew bootRun

setup-win:
	.\gradlew bootRun

run-dev:
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

run-dev-win:
	SPRING_PROFILES_ACTIVE=dev .\gradlew bootRun

start:
	./gradlew bootRun SPRING_PROFILES_ACTIVE=prod