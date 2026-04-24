package com.kronostt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KronosTtApplication {

	public static void main(String[] args) {
		SpringApplication.run(KronosTtApplication.class, args);
	}

}
// TODO: Multiple teachers into same session (Some pre primary cases include multiple teachers for same class, usually 2)
// TODO: Multiple batches into same session - due to merging, or, splitting then merging
// TODO: Refactor the algorithms so that in case of Labs we actually get the preferredRoom - IMPORTANT