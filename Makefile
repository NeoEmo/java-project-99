create-all-txt-java:
	find . -type f -name "*.java" -exec cat {} + > all_code.txt

setup:
	mkdir -p certs
	[ -f file:/run/secrets/private.pem ] || openssl genpkey -out certs/private.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048
	[ -f certs/public.pem ] || openssl rsa -in certs/private.pem -pubout -out certs/public.pem
	./gradlew bootRun

run-dev:
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

run-dev-win:
	SPRING_PROFILES_ACTIVE=dev .\gradlew bootRun

start:
	$(MAKE) setup
	SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun