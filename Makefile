.PHONY: start keys setup lint

create-all-txt-java:
	find . -type f -name "*.java" -exec cat {} + > all_code.txt

# Генерация ключей на Linux
keys:
	mkdir -p /run/secrets
	if [ ! -s /run/secrets/jwt_private_key ] || [ ! -s /run/secrets/jwt_public_key ]; then \
		openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out /run/secrets/jwt_private_key; \
		openssl pkey -in /run/secrets/jwt_private_key -pubout -out /run/secrets/jwt_public_key; \
	fi
	chmod 600 /run/secrets/jwt_private_key
	chmod 644 /run/secrets/jwt_public_key

# запуск в dev режиме на Linux
run-dev:
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# запуск в dev режиме на windows
run-dev-win:
	SPRING_PROFILES_ACTIVE=dev .\gradlew bootRun

# Нету ключей, запуск возможен, но приложение будет частично сломано
setup:
	SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun

# Linux only (sudo only)
start:
	$(MAKE) keys
	SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun

lint:
	./gradlew checkstyleMain
	./gradlew checkstyleTest
	
lint-win:
	.\gradlew checkstyleMain
	.\gradlew checkstyleTest