setup:
	./gradlew bootRun

setup-win:
	.\gradlew bootRun

run-dev:
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

run-dev-win:
	SPRING_PROFILES_ACTIVE=dev .\gradlew bootRun