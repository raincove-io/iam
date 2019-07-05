package io.github.erfangc.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
public class IamApplication {

	private static final Logger logger = LoggerFactory.getLogger(IamApplication.class);

	public static void main(String[] args) {
		//
		// validate environment
		//
		Set<String> requiredEnvVars = new HashSet<>();
		requiredEnvVars.add("REDIS_HOST");
		requiredEnvVars.add("ISSUER");
		requiredEnvVars.add("CLIENT_ID");
		requiredEnvVars.add("CLIENT_SECRET");
		requiredEnvVars.add("AUDIENCE");
		requiredEnvVars.add("CALLBACK");
		requiredEnvVars.add("ROOT_USERS");

		Set<String> missingEnvVars = new HashSet<>();
		Map<String, String> envs = System.getenv();
		for (String envVar : requiredEnvVars) {
			if (!envs.containsKey(envVar)) {
				missingEnvVars.add(envVar);
			}
		}

		if (!missingEnvVars.isEmpty()) {
			logger.error("Missing environment variables: {}", missingEnvVars);
			System.exit(1);
		}

		SpringApplication.run(IamApplication.class, args);
	}

}
