package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class NavigationSeeder implements ApplicationRunner {

	@Autowired
	INavigationService navigationService;

	@Override
	public void run(ApplicationArguments args) {
		navigationService.seedDefaults();
	}
}
