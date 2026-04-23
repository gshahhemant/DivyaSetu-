package com.swadhyaydata.app.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AppListener implements ApplicationListener<ApplicationReadyEvent> {

	public void onApplicationEvent(ApplicationReadyEvent arg0) {

		System.out.println("##############   Swadyay Data APP working fine #################");

	}

}
